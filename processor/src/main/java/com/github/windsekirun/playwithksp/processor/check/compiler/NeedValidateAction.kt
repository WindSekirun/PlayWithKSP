package com.github.windsekirun.playwithksp.processor.check.compiler

import com.google.devtools.ksp.symbol.KSFile

data class NeedValidateAction(
    val originQualifiedName: String,
    val validatorSimpleName: String,
    val needCheckList: List<Pair<String, String>>,
    val hasValidateCondition: Boolean,
    val file: KSFile?
)