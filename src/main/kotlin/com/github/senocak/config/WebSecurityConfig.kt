package com.github.senocak.config

import com.github.senocak.security.JwtAuthenticationEntryPoint
import com.github.senocak.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebSecurityConfig(
    private val unauthorizedHandler: JwtAuthenticationEntryPoint,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    /**
     * Configures the security settings for the application.
     *
     * @param http The HttpSecurity instance to modify.
     * @return A SecurityFilterChain instance that encapsulates the security configuration.
     *
     * The function configures the following security settings:
     * - Enables CORS (Cross-Origin Resource Sharing)
     * - Disables CSRF (Cross-Site Request Forgery)
     * - Sets the authentication entry point to a custom handler
     * - Sets the session creation policy to STATELESS
     * - Defines URL patterns that are accessible without authentication
     * - Requires authentication for any other request
     * - Adds a custom JWT authentication filter before the default UsernamePasswordAuthenticationFilter
     */
    @Bean
    fun configure(http: HttpSecurity): SecurityFilterChain =
        http.cors()
            .and()
                .csrf()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
            .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .authorizeRequests()
                .antMatchers(
                    "/",
                    "/*.html",
                    "/favicon.ico",
                    "/**/*.png",
                    "/**/*.gif",
                    "/**/*.svg",
                    "/**/*.jpg",
                    "/**/*.html",
                    "/**/*.css",
                    "/**/*.js",
                    "/assets/**",
                    "/api/v1/auth/**",
                    "/api/v1/shared/**"
                ).permitAll()
                .anyRequest().authenticated()
            .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
}
