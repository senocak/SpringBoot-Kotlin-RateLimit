package com.github.senocak

import ch.qos.logback.classic.Logger
import com.github.senocak.domain.Package
import com.github.senocak.domain.Role
import com.github.senocak.domain.User
import com.github.senocak.service.RoleService
import com.github.senocak.service.UserService
import com.github.senocak.util.AppConstants
import com.github.senocak.util.AppConstants.getLogger
import com.github.senocak.util.RoleName
import java.util.concurrent.TimeUnit
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.event.EventListener
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.repository.config.BootstrapMode
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional

@EnableAsync
@SpringBootApplication
@EnableJpaRepositories(bootstrapMode = BootstrapMode.LAZY)
class RateLimit(
    private val roleService: RoleService,
    private val userService: UserService,
){
    val logger: Logger = getLogger()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Transactional
    @EventListener(value = [ApplicationReadyEvent::class])
    fun init() {
        logger.debug("[ApplicationReadyEvent]: app is ready")
        val roleUser: Role = roleService.save(role = Role(name = RoleName.ROLE_USER))
        val roleAdmin: Role = roleService.save(role = Role(name = RoleName.ROLE_ADMIN))

        val package1 = Package(name = "10R1M", limitPer = 10, type = TimeUnit.MINUTES.name)
        val package2 = Package(name = "10R1H", limitPer = 10, type = TimeUnit.HOURS.name)
        val package3 = Package(name = "100R1H", limitPer = 100, type = TimeUnit.HOURS.name)

        var user1 = User(name = "anil1", username = "anilsenocak1", email = "anil1@senocak.com",
            password = passwordEncoder().encode("anilsenocak1"), roles = setOf(roleUser),
            `package` = package1
        )
        user1 = userService.save(user = user1)
        var user2 = User(name = "anil2", username = "anilsenocak2", email = "anil2@senocak.com",
            password = passwordEncoder().encode("anilsenocak2"), roles = setOf(roleUser, roleAdmin),
            `package` = package2
        )
        user2 = userService.save(user = user2)
    }
}

fun main(args: Array<String>) {
    runApplication<RateLimit>(args = args).also { AppConstants.setLevel("info") }
}