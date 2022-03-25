package com.github.windsekirun.playwithksp.base

import com.github.windsekirun.playwithksp.generated.validator.Validators
import kotlin.contracts.contract

object RequestValidator {

    inline fun <reified T> validate(request: T) {
        contract {
            returns() implies (request != null)
        }
        val validator = Validators.find<T>(T::class.qualifiedName.orEmpty())
        val result = validator.validate(request)
        require(result != null)
    }
}