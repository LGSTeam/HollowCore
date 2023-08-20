package ru.hollowhorizon.hc.common.scripting.coroutines

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

abstract class Persistor : AbstractCoroutineContextElement(Key) {
    abstract suspend fun persist()

    companion object Key : CoroutineContext.Key<Persistor>, suspend () -> Unit {
        override suspend fun invoke() {
            coroutineContext[Key]!!.persist()
        }
    }

    override val key: CoroutineContext.Key<*> = Key
}

interface Wrapper {
    suspend operator fun <T> invoke(block: suspend () -> T): T
}

suspend fun persist() {
    coroutineContext[Persistor.Key]!!.persist()
}