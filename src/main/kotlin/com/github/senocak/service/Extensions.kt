package com.github.senocak.service

import com.github.senocak.domain.Package
import com.github.senocak.domain.User
import com.github.senocak.domain.Role
import com.github.senocak.domain.dto.auth.RoleResponse
import com.github.senocak.domain.dto.user.PackageDto
import com.github.senocak.domain.dto.user.UserResponse
import java.util.stream.Collectors

object Extensions {
    fun User.convertEntityToDto(): UserResponse  = UserResponse(name = this.name, email = this.email, username = this.username,
        roles = this.roles.stream().map { r: Role -> r.convertEntityToDto() }.collect(Collectors.toSet()),
        `package` = this.`package`?.convertEntityToDto())

    fun Role.convertEntityToDto(): RoleResponse = RoleResponse().also { it.name = this.name }

    fun Package.convertEntityToDto(): PackageDto = PackageDto(name = this.name, limitPerHour = this.limitPer, type = this.type)

    fun Long.getReadableTime(): String {
        val tempSec: Long = this / 1000
        val sec: Long = tempSec % 60
        val min: Long = tempSec / 60 % 60
        val hour: Long = tempSec / (60 * 60) % 24
        val day: Long = tempSec / (24 * 60 * 60) % 24
        return String.format("%dd %dh %dm %ds", day, hour, min, sec)
    }
}