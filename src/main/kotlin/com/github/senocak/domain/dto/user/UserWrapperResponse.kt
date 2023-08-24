package com.github.senocak.domain.dto.user

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.senocak.domain.dto.BaseDto

@JsonPropertyOrder("user", "token", "refreshToken")
class UserWrapperResponse(
    @JsonProperty("user")
    var userResponse: UserResponse,
    var token: String? = null,
    var refreshToken: String? = null
): BaseDto()