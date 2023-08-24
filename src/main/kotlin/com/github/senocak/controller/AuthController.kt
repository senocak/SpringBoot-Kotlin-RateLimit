package com.github.senocak.controller

import com.github.senocak.domain.Role
import com.github.senocak.domain.User
import com.github.senocak.domain.dto.auth.LoginRequest
import com.github.senocak.domain.dto.auth.RefreshTokenRequest
import com.github.senocak.domain.dto.auth.RegisterRequest
import com.github.senocak.domain.dto.auth.RoleResponse
import com.github.senocak.domain.dto.user.UserResponse
import com.github.senocak.domain.dto.user.UserWrapperResponse
import com.github.senocak.exception.ServerException
import com.github.senocak.security.JwtTokenProvider
import com.github.senocak.service.Extensions.convertEntityToDto
import com.github.senocak.service.RoleService
import com.github.senocak.service.UserService
import com.github.senocak.util.OmaErrorMessageType
import com.github.senocak.util.RoleName
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.stream.Collectors

@RestController
@RequestMapping(AuthController.URL)
class AuthController(
    private val userService: UserService,
    private val roleService: RoleService,
    private val tokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager
): BaseController() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/login")
    @Throws(ServerException::class)
    fun login(@Validated @RequestBody loginRequest: LoginRequest, resultOfValidation: BindingResult): UserWrapperResponse {
        validate(resultOfValidation = resultOfValidation)
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
        )
        val user: User? = userService.findByUsername(username = loginRequest.username!!)
        return generateUserWrapperResponse(user!!.convertEntityToDto())
    }

    @PostMapping("/register")
    @Throws(ServerException::class)
    fun register(@Validated @RequestBody signUpRequest: RegisterRequest, resultOfValidation: BindingResult): ResponseEntity<UserWrapperResponse> {
        validate(resultOfValidation = resultOfValidation)
        if (userService.existsByUsername(username = signUpRequest.username!!))
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.JSON_SCHEMA_VALIDATOR, variables = arrayOf("Username is already taken!"))
                    .also { log.error("Username: ${signUpRequest.username} is already taken!") }
        if (userService.existsByEmail(email = signUpRequest.email!!))
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.JSON_SCHEMA_VALIDATOR, variables = arrayOf("Email Address already in use!"))
                    .also { log.error("Email Address: ${signUpRequest.email} is already taken!") }
        val userRole: Role = roleService.findByName(roleName = RoleName.ROLE_USER)
                ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.MANDATORY_INPUT_MISSING, variables = arrayOf("User Role is not found"))
                        .also { log.error("User Role is not found") }
        val user = User(name = signUpRequest.name!!, username = signUpRequest.username!!, email = signUpRequest.email!!,
            password = passwordEncoder.encode(signUpRequest.password), roles = setOf(userRole))
        val result: User = userService.save(user = user)
            .also { log.info("User created. User: $it") }
        val `object`: UserWrapperResponse? = try {
            login(loginRequest = LoginRequest(username = result.username, password = result.password), resultOfValidation = resultOfValidation)
        } catch (e: Exception) {
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR, statusCode = HttpStatus.INTERNAL_SERVER_ERROR,
                variables = arrayOf("Error occurred for generating jwt attempt", HttpStatus.INTERNAL_SERVER_ERROR.toString()))
                    .also { log.error("Exception: ${e.message}") }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(`object`)
    }

    @PostMapping("/refresh")
    @Throws(ServerException::class)
    fun refresh(@Validated @RequestBody refreshTokenRequest: RefreshTokenRequest, resultOfValidation: BindingResult): UserWrapperResponse {
        validate(resultOfValidation = resultOfValidation)
        val userNameFromJWT: String = tokenProvider.getUserNameFromJWT(token = refreshTokenRequest.token!!)
        val user: User = userService.findByUsername(username = userNameFromJWT)!!
        return generateUserWrapperResponse(userResponse = user.convertEntityToDto())
    }

    /**
     * Generate UserWrapperResponse with given UserResponse
     * @param userResponse -- UserResponse that contains user data
     * @return UserWrapperResponse
     */
    private fun generateUserWrapperResponse(userResponse: UserResponse): UserWrapperResponse {
        val roles: List<String> = userResponse.roles.stream().map { r: RoleResponse -> RoleName.fromString(r = r.name!!.name)!!.name }
            .collect(Collectors.toList())
        val jwtToken: String = tokenProvider.generateJwtToken(username = userResponse.username, roles = roles)
        val refreshToken: String = tokenProvider.generateRefreshToken(username = userResponse.username, roles = roles)
        return UserWrapperResponse(userResponse = userResponse, token = jwtToken, refreshToken = refreshToken)
            .also { log.info("UserWrapperResponse is generated. UserWrapperResponse: $it") }
    }

    companion object {
        const val URL = "/api/v1/auth"
    }
}