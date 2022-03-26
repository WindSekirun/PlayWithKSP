package com.github.windsekirun.playwithksp.processor.check

interface ValidateCondition {
    val isValidate: Boolean
    val nonValidateMessage: String
        get() = "Not matched condition"
}