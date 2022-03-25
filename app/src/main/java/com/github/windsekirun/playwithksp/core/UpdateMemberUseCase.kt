package com.github.windsekirun.playwithksp.core

import com.github.windsekirun.playwithksp.annotation.NeedValidate
import com.github.windsekirun.playwithksp.annotation.OptionalValue
import com.github.windsekirun.playwithksp.base.RequestValidator
import com.github.windsekirun.playwithksp.base.UseCase
import javax.inject.Inject

class UpdateMemberUseCase @Inject constructor() : UseCase<UpdateMemberUseCase.Request, Unit>() {

    override suspend fun run(model: Request?) {
        RequestValidator.validate(model)

//        UpdateMemberUseCaseRequestValidator.validate(model)
    }

    @NeedValidate
    data class Request(
        val id: String,
        val pw: String,
        @OptionalValue val age: String,
        @OptionalValue val gender: String
    )
}