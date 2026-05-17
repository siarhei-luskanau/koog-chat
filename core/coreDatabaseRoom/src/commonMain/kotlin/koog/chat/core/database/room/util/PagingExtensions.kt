package koog.chat.core.database.room.util

import androidx.paging.PagingSource
import androidx.paging.PagingState

fun <Key : Any, Value : Any, NextValue : Any> PagingSource<Key, Value>.map(transform: (Value) -> NextValue): PagingSource<Key, NextValue> {
    val inner = this
    return object : PagingSource<Key, NextValue>() {
        init {
            inner.registerInvalidatedCallback { invalidate() }
        }

        override fun getRefreshKey(state: PagingState<Key, NextValue>): Key? = null

        override suspend fun load(params: LoadParams<Key>): LoadResult<Key, NextValue> =
            when (val result = inner.load(params)) {
                is LoadResult.Page -> {
                    LoadResult.Page(
                        data = result.data.map(transform),
                        prevKey = result.prevKey,
                        nextKey = result.nextKey,
                        itemsBefore = result.itemsBefore,
                        itemsAfter = result.itemsAfter,
                    )
                }

                is LoadResult.Error -> {
                    LoadResult.Error(result.throwable)
                }

                is LoadResult.Invalid -> {
                    LoadResult.Invalid()
                }
            }
    }
}
