package com.github.senocak.repository

import com.github.senocak.domain.Package
import com.github.senocak.domain.Role
import com.github.senocak.domain.User
import com.github.senocak.util.RoleName
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PackageRepository: PagingAndSortingRepository<Package, String>

@Repository
interface RoleRepository: PagingAndSortingRepository<Role, Long> {
    fun findByName(roleName: RoleName): Optional<Role>
}

@Repository
interface UserRepository: PagingAndSortingRepository<User, String> {
    fun findByEmail(email: String): Optional<User>
    fun findByUsername(username: String): Optional<User>
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
}