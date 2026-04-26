package koog.chat.core.common

import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Single

@Single
internal class DispatcherSetIos : DispatcherSet {
    override fun defaultDispatcher() = Dispatchers.Default

    override fun ioDispatcher() = Dispatchers.Default

    override fun mainDispatcher() = Dispatchers.Main
}
