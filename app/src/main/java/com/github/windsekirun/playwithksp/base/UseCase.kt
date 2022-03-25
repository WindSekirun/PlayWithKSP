package com.github.windsekirun.playwithksp.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class UseCase<T, R>(private val executionDispatchers: CoroutineDispatcher = Dispatchers.IO) {
    open suspend operator fun invoke(): R = withContext(executionDispatchers) { run(null) }
    open suspend operator fun invoke(model: T): R = withContext(executionDispatchers) { run(model) }
    abstract suspend fun run(model: T?): R
}