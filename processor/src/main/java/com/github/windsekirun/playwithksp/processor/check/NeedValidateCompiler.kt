package com.github.windsekirun.playwithksp.processor.check

import com.github.windsekirun.playwithksp.annotation.NeedValidate
import com.github.windsekirun.playwithksp.annotation.OptionalValue
import com.github.windsekirun.playwithksp.processor.extensions.asNullable
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

internal class NeedValidateCompiler(
    private val codeGenerator: CodeGenerator,
    @Suppress("unused") private val logger: KSPLogger
) : SymbolProcessor {
    private val packageName = "$GENERATED_PACKAGE.validator"
    private val actionList = mutableListOf<Action>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // find validate symbols which annotated with @CheckParam
        val symbols = resolver.getSymbolsWithAnnotation(NeedValidate::class.qualifiedName.orEmpty())
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }

        if (!symbols.iterator().hasNext()) return emptyList()

        symbols.forEach { it.accept(Visitor(), Unit) }

        if (actionList.isNotEmpty()) {
            actionList.forEach { generateValidator(it) }
            generateValidators()
        }

        // pass non-validate symbols which doesn't processed on this round
        return symbols.filterNot { it.validate() }.toList()
    }

    private fun generateValidator(action: Action) {
        // build validate method
        val originModelClass = ClassName.bestGuess(action.originQualifiedName)
        val validateBuilder = FunSpec.builder("validate")
            .returns(originModelClass)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(ParameterSpec.builder("request", originModelClass.asNullable).build())

        // check model is null (after this check, request can't be null)
        validateBuilder.addStatement(
            "if??(request??==??null)??throw??%T(\"request is null\")",
            RuntimeException::class
        )

        action.needCheckList.forEach { (type, name) ->
            val statement = when (type) {
                "String" -> "if??(request.$name.isNullOrEmpty())??throw??%T(\"$name??is??null/empty\")"
                else -> "if??(request.$name??==??null)??throw??%T(\"$name??is??null\")"
            }
            validateBuilder.addStatement(statement, RuntimeException::class)
        }

        if (action.hasValidateCondition) {
            validateBuilder.addStatement(
                "if??(!request.isValidate)??throw??%T(%P)",
                RuntimeException::class,
                "\${request.nonValidateMessage}"
            )
        }

        validateBuilder.addStatement("return request")

        val validatorInterface = Validator::class.asClassName().plusParameter(originModelClass)

        // build object class contains 'validate' function
        val typeSpec = TypeSpec.classBuilder(action.validatorSimpleName)
            .addFunction(validateBuilder.build())
            .addSuperinterface(validatorInterface)
            .build()

        // write file
        FileSpec.builder(packageName, action.validatorSimpleName)
            .addType(typeSpec)
            .addComment("[NeedValidateCompiler] This file is the generated file. DO NOT MODIFY THIS FILE.")
            .build()
            .writeTo(codeGenerator, Dependencies(true, requireNotNull(action.file)))
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
                ClassName.bestGuess("$packageName.$validatorName"),
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
        FileSpec.builder(packageName, "Validators")
            .addType(typeSpec.build())
            .build()
            .writeTo(codeGenerator, Dependencies(true, *files))
    }

    private inner class Visitor : KSVisitorVoid() {

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val simpleName = classDeclaration.simpleName.asString()

            // add parent's name when target class is nested class
            val className = if (classDeclaration.parentDeclaration != null) {
                val parentClassName =
                    requireNotNull(classDeclaration.parentDeclaration).simpleName.asString()
                "${parentClassName}${simpleName}Validator"
            } else {
                "${simpleName}Validator"
            }

            val originQualifiedName = classDeclaration.qualifiedName?.asString().orEmpty()

            val hasCondition =
                classDeclaration.superTypes.any {
                    it.resolve().toClassName() == ValidateCondition::class.asClassName()
                }

            // exclude @OptionalValue attached property (doesn't check in validator)
            val needCheckList =
                classDeclaration.getAllProperties()
                    .filterNot {
                        hasCondition &&
                                (it.simpleName.asString() == "isValidate" ||
                                        it.simpleName.asString() == "nonValidateMessage")
                    }
                    .filterNot { it.isAnnotationPresent(OptionalValue::class) }
                    .map { property ->
                        val type = property.type.resolve().toString()
                        val name = property.simpleName.asString()
                        type to name
                    }
                    .toList()

            actionList += Action(
                originQualifiedName,
                className,
                needCheckList,
                hasCondition,
                classDeclaration.containingFile
            )
        }
    }

    private data class Action(
        val originQualifiedName: String,
        val validatorSimpleName: String,
        val needCheckList: List<Pair<String, String>>,
        val hasValidateCondition: Boolean,
        val file: KSFile?
    )

    companion object {
        private const val GENERATED_PACKAGE = "com.github.windsekirun.playwithksp.generated"
    }
}