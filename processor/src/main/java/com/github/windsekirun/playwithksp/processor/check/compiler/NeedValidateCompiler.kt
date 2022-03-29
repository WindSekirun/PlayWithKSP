package com.github.windsekirun.playwithksp.processor.check.compiler

import com.github.windsekirun.playwithksp.annotation.NeedValidate
import com.github.windsekirun.playwithksp.processor.check.Validator
import com.github.windsekirun.playwithksp.processor.extensions.asNullable
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.ksp.writeTo

internal class NeedValidateCompiler(
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
            actionList.forEach { generateValidator(it) }
        }

        // pass non-validate symbols which doesn't processed on this round
        return symbols.filterNot { it.validate() }.toList()
    }

    private fun generateValidator(needValidateAction: NeedValidateAction) {
        // build validate method
        val originModelClass = ClassName.bestGuess(needValidateAction.originQualifiedName)
        val validateBuilder = FunSpec.builder("validate")
            .returns(originModelClass)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(ParameterSpec.builder("request", originModelClass.asNullable).build())

        // check model is null (after this check, request can't be null)
        validateBuilder.addStatement(
            "if·(request·==·null)·throw·%T(\"request is null\")",
            RuntimeException::class
        )

        needValidateAction.needCheckList.forEach { (type, name) ->
            val statement = when (type) {
                "String" -> "if·(request.$name.isNullOrEmpty())·throw·%T(\"$name·is·null/empty\")"
                else -> "if·(request.$name·==·null)·throw·%T(\"$name·is·null\")"
            }
            validateBuilder.addStatement(statement, RuntimeException::class)
        }

        if (needValidateAction.hasValidateCondition) {
            validateBuilder.addStatement(
                "if·(!request.isValidate)·throw·%T(%P)",
                RuntimeException::class,
                "\${request.nonValidateMessage}"
            )
        }

        validateBuilder.addStatement("return request")

        val validatorInterface = Validator::class.asClassName().plusParameter(originModelClass)

        // build object class contains 'validate' function
        val typeSpec = TypeSpec.classBuilder(needValidateAction.validatorSimpleName)
            .addFunction(validateBuilder.build())
            .addSuperinterface(validatorInterface)
            .build()

        // write file
        FileSpec.builder(GENERATED_PACKAGE, needValidateAction.validatorSimpleName)
            .addType(typeSpec)
            .addComment("[NeedValidateCompiler] This file is the generated file. DO NOT MODIFY THIS FILE.")
            .build()
            .writeTo(codeGenerator, Dependencies(true, requireNotNull(needValidateAction.file)))
    }

    companion object {
        const val GENERATED_PACKAGE = "com.github.windsekirun.playwithksp.generated.validator"
    }
}