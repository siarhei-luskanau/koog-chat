package template.core.pref

import android.content.Context
import androidx.datastore.core.Serializer
import androidx.datastore.core.Storage
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.tink.AeadSerializer
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeystore
import kotlinx.coroutines.runBlocking
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.sink
import okio.source
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import template.core.common.DispatcherSet
import java.io.InputStream
import java.io.OutputStream

@Single
internal class AppStorageProviderAndroid(
    private val context: Context,
    @Provided private val dispatcherSet: DispatcherSet,
) : StorageProvider {
    private val aead: Aead by lazy {
        try {
            if (!AndroidKeystore.hasKey(KEY_ALIAS)) {
                AndroidKeystore.generateNewAes256GcmKey(KEY_ALIAS)
            }
            AndroidKeystore.getAead(KEY_ALIAS)
        } catch (e: Exception) {
            AeadConfig.register()
            KeysetHandle
                .generateNew(KeyTemplates.get("AES256_GCM"))
                .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
        }
    }

    override fun getStorage(): Storage<PrefData> =
        OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = PrefSerializer().withAeadEncryption(aead),
            producePath = {
                runBlocking(dispatcherSet.ioDispatcher()) {
                    context.filesDir
                        .resolve("app.pref.json")
                        .absolutePath
                        .toPath()
                }
            },
        )

    companion object {
        private const val KEY_ALIAS = "template_datastore_aead_key"
    }
}

private fun <T> OkioSerializer<T>.withAeadEncryption(aead: Aead): OkioSerializer<T> {
    val aeadSerializer =
        AeadSerializer(
            aead = aead,
            wrappedSerializer =
                object : Serializer<T> {
                    override val defaultValue = this@withAeadEncryption.defaultValue

                    override suspend fun readFrom(input: InputStream): T = readFrom(input.source().buffer())

                    override suspend fun writeTo(
                        t: T,
                        output: OutputStream,
                    ) {
                        val bufferedSink = output.sink().buffer()
                        writeTo(t, bufferedSink)
                        bufferedSink.flush()
                    }
                },
        )
    return object : OkioSerializer<T> {
        override val defaultValue = aeadSerializer.defaultValue

        override suspend fun readFrom(source: BufferedSource): T = aeadSerializer.readFrom(source.inputStream())

        override suspend fun writeTo(
            t: T,
            sink: BufferedSink,
        ) = aeadSerializer.writeTo(t, sink.outputStream())
    }
}
