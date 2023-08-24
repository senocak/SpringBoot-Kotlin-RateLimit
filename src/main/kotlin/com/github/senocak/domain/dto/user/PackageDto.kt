package com.github.senocak.domain.dto.user

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.senocak.domain.dto.BaseDto
import java.util.concurrent.TimeUnit

@JsonPropertyOrder("name", "limitPerHour")
class PackageDto(
    var name: String,
    var limitPerHour: Long,
    var type: String = TimeUnit.MINUTES.name,
): BaseDto()