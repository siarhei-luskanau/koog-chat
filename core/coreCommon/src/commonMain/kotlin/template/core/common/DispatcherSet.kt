package template.core.common

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherSet {
    fun defaultDispatcher(): CoroutineDispatcher

    fun ioDispatcher(): CoroutineDispatcher

    fun mainDispatcher(): CoroutineDispatcher
}
