package com.github.senocak.domain.dto.user

import com.github.senocak.domain.dto.BaseDto
import com.github.senocak.util.validation.PasswordMatches
import javax.validation.constraints.Size

@PasswordMatches
data class UpdateUserDto(
    @field:Size(min = 4, max = 40)
    var name: String? = null,

    @field:Size(min = 6, max = 20)
    var password: String? = null,

    @field:Size(min = 6, max = 20)
    var password_confirmation: String? = null
): BaseDto()