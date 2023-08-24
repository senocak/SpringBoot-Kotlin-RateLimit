package com.github.senocak.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.domain.User
import com.github.senocak.exception.RestExceptionHandler
import com.github.senocak.exception.ServerException
import com.github.senocak.service.Extensions.getReadableTime
import com.github.senocak.service.RateLimitInfo
import com.github.senocak.service.RateLimitingService
import com.github.senocak.service.UserService
import com.github.senocak.util.AppConstants.TOKEN_HEADER_NAME
import com.github.senocak.util.AppConstants.TOKEN_PREFIX
import com.github.senocak.util.OmaErrorMessageType
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val tokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val objectMapper: ObjectMapper,
    private val authenticationManager: AuthenticationManager,
    private val rateLimitingService: RateLimitingService
): OncePerRequestFilter() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Guaranteed to be just invoked once per request within a single request thread.
     *
     * @param request -- Request information for HTTP servlets.
     * @param response -- It is where the servlet can write information about the data it will send back.
     * @param filterChain -- An object provided by the servlet container to the developer giving a view into the invocation chain of a filtered request for a resource.
     * @throws ServletException -- Throws ServletException
     * @throws IOException -- Throws IOException
     */
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try {
            val bearerToken: String? = request.getHeader(TOKEN_HEADER_NAME)
            if (bearerToken != null && bearerToken.startsWith(prefix = TOKEN_PREFIX)) {
                val jwt: String = bearerToken.substring(startIndex = 7)
                tokenProvider.validateToken(token = jwt)
                val userName: String = tokenProvider.getUserNameFromJWT(token = jwt)
                val findByUsername: User = userService.findByUsername(username = userName)
                /*
                val tokenBucket: Bucket = rateLimitingService.resolveBucket(userId = findByUsername?.`package`?.id!!)
                val probe: ConsumptionProbe = tokenBucket.tryConsumeAndReturnRemaining(1)
                log.info("user: $userName, remainingTokens: ${probe.remainingTokens}")
                if (!probe.isConsumed) {
                    val readableTime: String = getReadableTime(nanos = probe.nanosToWaitForRefill)
                    val serverException = ServerException(omaErrorMessageType = OmaErrorMessageType.RATE_LIMIT,
                        variables = arrayOf(readableTime), statusCode = HttpStatus.TOO_MANY_REQUESTS)
                    val responseEntity: ResponseEntity<Any> = RestExceptionHandler().handleServerException(ex = serverException)
                    response.writer.write(objectMapper.writeValueAsString(responseEntity.body))
                    response.status = serverException.statusCode.value()
                    response.contentType = "application/json"
                    log.error("Rate Limit: $readableTime")
                    return
                }
                */
                val rateLimitResponse: RateLimitInfo = rateLimitingService.getRateLimitResponse(
                    userId = findByUsername.id!!,
                    packageId = findByUsername.`package`?.id!!)
                response.contentType = "application/json"
                response.setHeader("X-RATE-LIMIT-DURATION", rateLimitResponse.duration.toString())
                response.setHeader("X-RATE-LIMIT-TIMEUNIT", rateLimitResponse.timeUnit.name)
                response.setHeader("X-RATE-LIMIT-COUNTER", rateLimitResponse.counter.toString())
                response.setHeader("X-RATE-LIMIT-LIMIT", rateLimitResponse.limit.toString())
                log.info("user: $userName, remainingTokens: $rateLimitResponse")
                if (rateLimitResponse.counter.toLong() > rateLimitResponse.limit) {
                    val readableTime: String = rateLimitResponse.resetTime.getReadableTime()
                    val serverException = ServerException(omaErrorMessageType = OmaErrorMessageType.RATE_LIMIT,
                        variables = arrayOf(readableTime), statusCode = HttpStatus.TOO_MANY_REQUESTS)
                    val responseEntity: ResponseEntity<Any> = RestExceptionHandler().handleServerException(ex = serverException)
                    response.writer.write(objectMapper.writeValueAsString(responseEntity.body))
                    response.status = serverException.statusCode.value()
                    log.error("$rateLimitResponse Rate Limit: $readableTime")
                    return
                }

                val userDetails: UserDetails = userService.loadUserByUsername(username = userName)
                UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities)
                    .also { it.details = WebAuthenticationDetailsSource().buildDetails(request) }
                    .also { authenticationManager.authenticate(it) }
                    .also { log.trace("SecurityContext created") }
            }
        } catch (ex: Exception) {
            val responseEntity: ResponseEntity<Any> = RestExceptionHandler().handleUnAuthorized(ex = RuntimeException(ex.message))
            response.writer.write(objectMapper.writeValueAsString(responseEntity.body))
            response.status = responseEntity.statusCode.value()
            log.error("Could not set user authentication in security context. Error: ${ex.message}")
            return
        }
        filterChain.doFilter(request, response)
    }
}