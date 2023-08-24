package com.github.senocak.domain.dto.user

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.senocak.domain.dto.BaseDto
import com.github.senocak.domain.dto.auth.RoleResponse

@JsonPropertyOrder("name", "username", "email", "roles")
class UserResponse(
    @JsonProperty("name")
    var name: String,
    var email: String,
    var username: String,
    var roles: Set<RoleResponse>,
    var `package`: PackageDto?
): BaseDto()