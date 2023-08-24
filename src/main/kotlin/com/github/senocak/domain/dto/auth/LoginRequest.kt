package com.github.senocak.domain.dto.auth

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.senocak.domain.dto.BaseDto
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class LoginRequest(
    @JsonProperty("username")
    @field:NotBlank
    @field:Size(min = 3, max = 50)
    var username: String? = null,

    @field:NotBlank
    @field:Size(min = 6, max = 20)
    var password: String? = null
): BaseDto()