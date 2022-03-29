package com.github.windsekirun.playwithksp.core.usecase

import com.github.windsekirun.playwithksp.annotation.NeedValidate
import com.github.windsekirun.playwithksp.annotation.OptionalValue
import com.github.windsekirun.playwithksp.base.RequestValidator
import com.github.windsekirun.playwithksp.base.UseCase
import com.github.windsekirun.playwithksp.core.repository.PreferenceRepository
import com.github.windsekirun.playwithksp.processor.check.ValidateCondition
import javax.inject.Inject

class UpdateMemberUseCase2 @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) : UseCase<UpdateMemberUseCase2.Request, String>() {

    override suspend fun run(model: Request?): String {
        RequestValidator.validate(model)
        preferenceRepository.setString("member", "${model.id}-${model.pw}-${model.age}")
        return "Updated ${model.id}"
    }

    @NeedValidate
    data class Request(
        val id: String,
        val pw: String,
        @OptionalValue val age: String
    ): ValidateCondition {
        override val isValidate: Boolean
            get() = pw.length >= 8
    }
}