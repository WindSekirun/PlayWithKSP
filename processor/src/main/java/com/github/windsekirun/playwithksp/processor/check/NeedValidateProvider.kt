package com.github.windsekirun.playwithksp.processor.check

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

internal class NeedValidateProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment) =
        NeedValidateCompiler(environment.codeGenerator, environment.logger)

}