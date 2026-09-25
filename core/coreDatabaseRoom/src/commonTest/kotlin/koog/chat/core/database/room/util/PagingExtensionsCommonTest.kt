package koog.chat.core.database.room.util

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class PagingExtensionsCommonTest {
    private class FakePagingSource(
        private val result: LoadResult<Int, Int>,
    ) : PagingSource<Int, Int>() {
        override fun getRefreshKey(state: PagingState<Int, Int>): Int? = null

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Int> = result
    }

    private fun refreshParams(key: Int? = null) = PagingSource.LoadParams.Refresh(key = key, loadSize = 20, placeholdersEnabled = false)

    private fun emptyState() =
        PagingState<Int, String>(
            pages = emptyList(),
            anchorPosition = null,
            config = PagingConfig(10),
            leadingPlaceholderCount = 0,
        )

    @Test
    fun map_shouldTransformDataAndPreserveKeys_whenPage() =
        runTest {
            val page =
                PagingSource.LoadResult.Page(
                    data = listOf(1, 2, 3),
                    prevKey = 0,
                    nextKey = 4,
                    itemsBefore = 5,
                    itemsAfter = 6,
                )
            val mapped = FakePagingSource(page).map { it.toString() }

            val result = mapped.load(refreshParams()) as PagingSource.LoadResult.Page

            assertEquals(listOf("1", "2", "3"), result.data)
            assertEquals(0, result.prevKey)
            assertEquals(4, result.nextKey)
            assertEquals(5, result.itemsBefore)
            assertEquals(6, result.itemsAfter)
        }

    @Test
    fun map_shouldPropagateThrowable_whenError() =
        runTest {
            val throwable = IllegalStateException("boom")
            val error = PagingSource.LoadResult.Error<Int, Int>(throwable)
            val mapped = FakePagingSource(error).map { it.toString() }

            val result = mapped.load(refreshParams()) as PagingSource.LoadResult.Error

            assertSame(throwable, result.throwable)
        }

    @Test
    fun map_shouldReturnInvalid_whenInnerReturnsInvalid() =
        runTest {
            val invalid = PagingSource.LoadResult.Invalid<Int, Int>()
            val mapped = FakePagingSource(invalid).map { it.toString() }

            val result = mapped.load(refreshParams())

            assertTrue(result is PagingSource.LoadResult.Invalid)
        }

    @Test
    fun getRefreshKey_shouldReturnNull() {
        val page = PagingSource.LoadResult.Page<Int, Int>(data = emptyList(), prevKey = null, nextKey = null)
        val mapped = FakePagingSource(page).map { it.toString() }

        assertNull(mapped.getRefreshKey(emptyState()))
    }

    @Test
    fun map_shouldInvalidateMappedSource_whenInnerSourceInvalidated() {
        val page = PagingSource.LoadResult.Page<Int, Int>(data = emptyList(), prevKey = null, nextKey = null)
        val inner = FakePagingSource(page)
        val mapped = inner.map { it.toString() }

        inner.invalidate()

        assertTrue(mapped.invalid)
    }
}
