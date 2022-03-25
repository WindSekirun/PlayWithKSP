package com.github.windsekirun.playwithksp.processor.extensions

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

internal val ClassName.asNullable get() = this.copy(nullable = true)

internal val TypeName.asNullable get() = this.copy(nullable = true)