package com.github.windsekirun.playwithksp.processor.check.compiler.metadata

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

internal class NeedValidateMetadataProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment) =
        NeedValidateMetadataCompiler(environment.codeGenerator, environment.logger)

}