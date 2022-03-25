package com.github.windsekirun.playwithksp.processor.check

interface Validator<T> {
    fun validate(request: T?): T
}