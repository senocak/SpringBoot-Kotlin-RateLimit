package com.github.senocak.domain.dto.auth

import com.github.senocak.domain.dto.BaseDto
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class RefreshTokenRequest(
    @field:NotBlank
    @field:Size(min = 4, max = 40)
    var token: String? = null
): BaseDto()