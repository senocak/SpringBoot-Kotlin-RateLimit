package com.github.senocak.domain.dto.auth

import com.github.senocak.domain.dto.BaseDto
import com.github.senocak.util.RoleName

class RoleResponse : BaseDto() {
    var name: RoleName? = null
}