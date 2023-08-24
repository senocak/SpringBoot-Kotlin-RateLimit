package com.github.senocak.service

import com.github.senocak.domain.Role
import com.github.senocak.domain.User
import com.github.senocak.exception.ServerException
import com.github.senocak.repository.UserRepository
import com.github.senocak.util.RoleName
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Collectors

@Service
class UserService(private val userRepository: UserRepository): UserDetailsService {

    /**
     * @param username -- string username to find in db
     * @return -- Optional User object
     */
    fun findByUsername(username: String): User =
        userRepository.findByUsername(username).orElseThrow { UsernameNotFoundException("User not found with email") }

    /**
     * @param username -- string username to find in db
     * @return -- Optional User object
     */
    fun existsByUsername(username: String): Boolean =
        userRepository.existsByUsername(username = username)

    /**
     * @param email -- string email to find in db
     * @return -- true or false
     */
    fun existsByEmail(email: String): Boolean =
        userRepository.existsByEmail(email = email)

    /**
     * @param email -- string email to find in db
     * @return -- User object
     * @throws UsernameNotFoundException -- throws UsernameNotFoundException
     */
    @Throws(UsernameNotFoundException::class)
    fun findByEmail(email: String): User =
        userRepository.findByEmail(email = email).orElseThrow { UsernameNotFoundException("User not found with email") }

    /**
     * @param user -- User object to persist to db
     * @return -- User object that is persisted to db
     */
    fun save(user: User): User = userRepository.save(user)

    /**
     * @param username -- username
     * @return -- Spring User object
     */
    @Transactional
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): org.springframework.security.core.userdetails.User {
        val user: User = findByUsername(username = username)
        val authorities: List<GrantedAuthority> = user.roles.stream()
            .map { r: Role -> SimpleGrantedAuthority(RoleName.fromString(r = r.name.toString())!!.name) }
            .collect(Collectors.toList())
        return org.springframework.security.core.userdetails.User(user.username, user.password, authorities)
    }

    /**
     * @return -- User entity that is retrieved from db
     * @throws ServerException -- throws ServerException
     */
    @Throws(ServerException::class)
    fun loggedInUser(): User? {
        val username: String = (SecurityContextHolder.getContext().authentication.principal as org.springframework.security.core.userdetails.User).username
        return findByUsername(username = username)
    }
}
