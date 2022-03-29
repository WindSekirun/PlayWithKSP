package com.github.windsekirun.playwithksp.processor.check.compiler

import com.github.windsekirun.playwithksp.annotation.OptionalValue
import com.github.windsekirun.playwithksp.processor.check.ValidateCondition
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName

class NeedValidateVisitor(private val block: (NeedValidateAction) -> Unit) :
    KSVisitorVoid() {

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

                    property.type.resolve().isMarkedNullable

                    type to name
                }
                .toList()

        block(
            NeedValidateAction(
                originQualifiedName,
                className,
                needCheckList,
                hasCondition,
                classDeclaration.containingFile
            )
        )
    }
}

