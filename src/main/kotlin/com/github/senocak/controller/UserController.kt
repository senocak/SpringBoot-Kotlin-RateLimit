package com.github.senocak.controller

import com.github.senocak.domain.User
import com.github.senocak.domain.dto.user.UpdateUserDto
import com.github.senocak.domain.dto.user.UserWrapperResponse
import com.github.senocak.exception.ServerException
import com.github.senocak.service.Extensions.convertEntityToDto
import com.github.senocak.service.UserService
import com.github.senocak.util.OmaErrorMessageType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import org.springframework.security.access.prepost.PreAuthorize

@RestController
@PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
@RequestMapping(UserController.URL)
class UserController(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder
): BaseController() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Throws(ServerException::class)
    @GetMapping("/me")
    fun me(): UserWrapperResponse = UserWrapperResponse(userResponse = userService.loggedInUser()!!.convertEntityToDto())

    @PatchMapping("/me")
    @Throws(ServerException::class)
    fun patchMe(request: HttpServletRequest, @Validated @RequestBody userDto: UpdateUserDto, resultOfValidation: BindingResult): MutableMap<String, String> {
        validate(resultOfValidation = resultOfValidation)
        val user: User? = userService.loggedInUser()
        val name: String? = userDto.name
        if (!name.isNullOrEmpty())
            user!!.name = name
        val password: String? = userDto.password
        val passwordConfirmation: String? = userDto.password_confirmation
        if (!password.isNullOrEmpty()) {
            if (passwordConfirmation.isNullOrEmpty()) {
                val message = "Password confirmation not provided"
                log.error(message)
                throw ServerException(OmaErrorMessageType.BASIC_INVALID_INPUT, arrayOf(message), HttpStatus.BAD_REQUEST)
            }
            if (passwordConfirmation != password) {
                val message = "Password and confirmation not matched"
                log.error(message)
                throw ServerException(OmaErrorMessageType.BASIC_INVALID_INPUT, arrayOf(message), HttpStatus.BAD_REQUEST)
            }
            user!!.password = passwordEncoder.encode(password)
        }
        userService.save(user = user!!)
        return HashMap<String, String>().also { it["message"] = "User updated." }
    }

    companion object {
        const val URL = "/api/v1/user"
    }
}
