package com.github.senocak.domain.dto.auth

import com.github.senocak.domain.dto.BaseDto
import com.github.senocak.util.validation.ValidEmail
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank
    @field:Size(min = 4, max = 40)
    var name: String? = null,

    @field:NotBlank
    @field:Size(min = 3, max = 15)
    var username: String? = null,

    @field:ValidEmail
    var email: String? = null,

    @field:NotBlank
    @field:Size(min = 6, max = 20)
    var password: String? = null
): BaseDto()