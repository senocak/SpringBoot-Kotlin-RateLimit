package com.github.senocak.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.SignatureException
import io.jsonwebtoken.UnsupportedJwtException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtTokenProvider(
    @Value("\${app.jwtSecret}") jSecret: String,
    @Value("\${app.jwtExpirationInMs}") jExpirationInMs: String,
    @Value("\${app.refreshExpirationInMs}") rExpirationInMs: String
) {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)
    private var jwtSecret: String = jSecret
    private var jwtExpirationInMs: String = jExpirationInMs
    private var refreshExpirationInMs: String = rExpirationInMs

    /**
     * Generating the jwt token
     * @param username -- userName
     */
    fun generateJwtToken(username: String, roles: List<String?>): String =
        generateToken(subject = username, roles = roles, expirationInMs = jwtExpirationInMs.toLong())

    /**
     * Generating the refresh token
     * @param username -- userName
     */
    fun generateRefreshToken(username: String, roles: List<String?>): String =
        generateToken(subject = username, roles = roles, expirationInMs = refreshExpirationInMs.toLong())

    /**
     * Generating the token
     * @param subject -- userName
     */
    private fun generateToken(subject: String, roles: List<String?>, expirationInMs: Long): String {
        val now = Date()
        val claims: MutableMap<String, Any> = HashMap<String, Any>()
                .also {
                    it["roles"] = roles
                }
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + expirationInMs))
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact()
    }

    /**
     * Get the jws claims
     * @param token -- jwt token
     * @return -- expiration date
     */
    private fun getJwsClaims(token: String): Jws<Claims?> = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token)

    /**
     * @param token -- jwt token
     * @return -- userName from jwt
     */
    fun getUserNameFromJWT(token: String): String = getJwsClaims(token).body!!.subject

    /**
     * @param token -- jwt token
     */
    fun validateToken(token: String) {
        try {
            getJwsClaims(token = token)
        } catch (ex: SignatureException) {
            log.error("Invalid JWT signature")
            throw AccessDeniedException("Invalid JWT signature")
        } catch (ex: MalformedJwtException) {
            log.error("Invalid JWT token")
            throw AccessDeniedException("Invalid JWT token")
        } catch (ex: ExpiredJwtException) {
            log.error("Expired JWT token")
            throw AccessDeniedException("Expired JWT token")
        } catch (ex: UnsupportedJwtException) {
            log.error("Unsupported JWT token")
            throw AccessDeniedException("Unsupported JWT token")
        } catch (ex: IllegalArgumentException) {
            log.error("JWT claims string is empty.")
            throw AccessDeniedException("JWT claims string is empty.")
        }
    }
}