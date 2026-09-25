package koog.chat.core.pref

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import koog.chat.core.common.DispatcherSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeDispatcherSet : DispatcherSet {
    override fun defaultDispatcher() = Dispatchers.IO

    override fun ioDispatcher() = Dispatchers.IO

    override fun mainDispatcher() = Dispatchers.IO
}

@RunWith(RobolectricTestRunner::class)
internal class AppStorageProviderAndroidHostTest {
    @Test
    fun getStorage_shouldPersistAndEncryptKey_whenWrittenThroughPrefService() =
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val provider = AppStorageProviderAndroid(context, FakeDispatcherSet())
            val service = PrefServiceDataStore(provider)

            service.cleanStorage()
            service.setKey("top-secret-value")

            assertEquals("top-secret-value", service.getKey().first())

            val prefFile = context.filesDir.resolve("koog_chat_app.pref.json")
            assertTrue(prefFile.exists())
            val rawContent = prefFile.readText()
            assertFalse(rawContent.contains("top-secret-value"))
        }
}
