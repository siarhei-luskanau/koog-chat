package koog.chat.core.pref

import kotlinx.coroutines.test.runTest
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PrefSerializerCommonTest {
    private val serializer = PrefSerializer()

    @Test
    fun writeToAndReadFrom_shouldRoundTripPrefData_whenDataIsValid() =
        runTest {
            val buffer = Buffer()
            val data = PrefData(key = "test-value")

            serializer.writeTo(data, buffer)
            val result = serializer.readFrom(buffer)

            assertEquals(data, result)
        }

    @Test
    fun readFrom_shouldReturnDefaultValue_whenContentIsCorruptJson() =
        runTest {
            val buffer = Buffer()
            buffer.writeUtf8("not json")

            val result = serializer.readFrom(buffer)

            assertEquals(PrefData.DEFAULT, result)
        }

    @Test
    fun defaultValue_shouldEqualPrefDataDefault() {
        assertEquals(PrefData.DEFAULT, serializer.defaultValue)
    }
}
