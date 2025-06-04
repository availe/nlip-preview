package io.availe.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile

class SuspendLazy<T>(private val initializer: suspend () -> T) {
    @Volatile
    private var _value: T? = null
    private val mutex = Mutex()

    suspend fun get(): T {
        _value?.let { return it }
        return mutex.withLock {
            _value?.let { return it }
            val value = initializer()
            _value = value
            value
        }
    }
}
