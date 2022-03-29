package com.github.windsekirun.playwithksp.processor.check.compiler.metadata

import com.github.windsekirun.playwithksp.annotation.NeedValidate
import com.github.windsekirun.playwithksp.processor.check.Validator
import com.github.windsekirun.playwithksp.processor.check.compiler.NeedValidateAction
import com.github.windsekirun.playwithksp.processor.check.compiler.NeedValidateVisitor
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.ksp.writeTo

internal class NeedValidateMetadataCompiler(
    private val codeGenerator: CodeGenerator,
    @Suppress("unused") private val logger: KSPLogger
) : SymbolProcessor {
    private val actionList = mutableListOf<NeedValidateAction>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // find validate symbols which annotated with @CheckParam
        val symbols = resolver.getSymbolsWithAnnotation(NeedValidate::class.qualifiedName.orEmpty())
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }

        if (!symbols.iterator().hasNext()) return emptyList()

        symbols.forEach {
            val visitor = NeedValidateVisitor { action -> actionList += action }
            it.accept(visitor, Unit)
        }

        if (actionList.isNotEmpty()) {
            generateValidators()
        }

        // pass non-validate symbols which doesn't processed on this round
        return symbols.filterNot { it.validate() }.toList()
    }

    private fun generateValidators() {
        val validatorTypeVariable = Validator::class.asClassName()
            .plusParameter(TypeVariableName.invoke("T"))

        val funSpec = FunSpec.builder("find")
            .addParameter("qualifiedName", String::class)
            .returns(validatorTypeVariable)
            .addTypeVariable(TypeVariableName.Companion.invoke("T"))

        funSpec.beginControlFlow("return when (qualifiedName)")
        actionList.forEach { (originQualifiedName, validatorName) ->
            funSpec.addStatement(
                "\"$originQualifiedName\" -> %T() as %T",
                ClassName.bestGuess("$GENERATED_PACKAGE.$validatorName"),
                validatorTypeVariable
            )
        }
        funSpec.addStatement(
            "else -> throw %T(\"Can't find any validators\")",
            RuntimeException::class
        )
        funSpec.endControlFlow()

        val typeSpec = TypeSpec.objectBuilder("Validators")
            .addFunction(funSpec.build())
            .addAnnotation(
                AnnotationSpec.builder(Suppress::class)
                    .addMember("%S", "UNCHECKED_CAST")
                    .build()
            )

        val files = actionList.mapNotNull { it.file }.toTypedArray()
        FileSpec.builder(GENERATED_PACKAGE, "Validators")
            .addType(typeSpec.build())
            .build()
            .writeTo(codeGenerator, Dependencies(true, *files))
    }

    companion object {
        const val GENERATED_PACKAGE = "com.github.windsekirun.playwithksp.generated.validator"
    }
}