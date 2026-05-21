package com.example.demo.auth.config

import com.example.demo.auth.AuthConstants
import com.example.demo.auth.jwt.JwtProperties
import com.example.demo.auth.jwt.JwtTokenType
import com.example.demo.auth.revocation.RevokedTokenChecker
import com.example.demo.poc.inspection.PocInspectionRoutes
import com.example.demo.user.auth.UserAuthRoutes
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Configuration
class SecurityConfig {
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(
        http: HttpSecurity,
        unauthorizedResponseWriter: JsonUnauthorizedResponseWriter,
        jwtSecretKey: SecretKey,
        revokedTokenChecker: RevokedTokenChecker,
    ): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }.exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint(
                    AuthenticationEntryPoint { _, response, _ ->
                        unauthorizedResponseWriter.write(response)
                    },
                )
            }.authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(HttpMethod.POST, UserAuthRoutes.REGISTER_PATH)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, UserAuthRoutes.LOGIN_PATH)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, UserAuthRoutes.LOGOUT_PATH)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, UserAuthRoutes.REFRESH_PATH)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, PocInspectionRoutes.USERS_PATH)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, PocInspectionRoutes.REVOKED_TOKENS_PATH)
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }.oauth2ResourceServer { oauth2 ->
                oauth2
                    .bearerTokenResolver(userAuthBearerTokenResolver())
                    .authenticationEntryPoint(
                        AuthenticationEntryPoint { _, response, _ ->
                            response.setHeader(
                                HttpHeaders.WWW_AUTHENTICATE,
                                AuthConstants.BEARER_AUTHENTICATION_SCHEME,
                            )
                            unauthorizedResponseWriter.write(response)
                        },
                    ).jwt { jwt ->
                        jwt.decoder(
                            accessTokenJwtDecoder(
                                jwtSecretKey,
                                revokedTokenChecker,
                            ),
                        )
                    }
            }.build()

    @Bean
    fun jwtSecretKey(jwtProperties: JwtProperties): SecretKey =
        SecretKeySpec(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8), MacAlgorithm.HS256.toString())

    @Bean
    fun jwtEncoder(jwtSecretKey: SecretKey): JwtEncoder =
        NimbusJwtEncoder
            .withSecretKey(jwtSecretKey)
            .algorithm(MacAlgorithm.HS256)
            .build()

    @Bean
    fun jwtDecoder(jwtSecretKey: SecretKey): JwtDecoder =
        NimbusJwtDecoder
            .withSecretKey(jwtSecretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build()

    private fun accessTokenJwtDecoder(
        jwtSecretKey: SecretKey,
        revokedTokenChecker: RevokedTokenChecker,
    ): JwtDecoder {
        val jwtDecoder =
            NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build()
        jwtDecoder.setJwtValidator(
            DelegatingOAuth2TokenValidator(
                JwtValidators.createDefault(),
                accessTokenTypeValidator(),
                accessTokenRevocationValidator(revokedTokenChecker),
            ),
        )
        return jwtDecoder
    }

    private fun userAuthBearerTokenResolver(): BearerTokenResolver {
        val delegate: BearerTokenResolver = DefaultBearerTokenResolver()
        return BearerTokenResolver { request: HttpServletRequest ->
            val requestUri = request.requestURI
            val contextPath = request.contextPath
            if ((contextPath + UserAuthRoutes.LOGOUT_PATH) == requestUri ||
                (contextPath + UserAuthRoutes.REFRESH_PATH) == requestUri ||
                (contextPath + PocInspectionRoutes.USERS_PATH) == requestUri ||
                (contextPath + PocInspectionRoutes.REVOKED_TOKENS_PATH) == requestUri
            ) {
                return@BearerTokenResolver null
            }
            delegate.resolve(request)
        }
    }

    private fun accessTokenTypeValidator(): OAuth2TokenValidator<Jwt> {
        return OAuth2TokenValidator { jwt: Jwt ->
            if (JwtTokenType.ACCESS.value() == jwt.getClaimAsString(JwtTokenType.CLAIM)) {
                return@OAuth2TokenValidator OAuth2TokenValidatorResult.success()
            }
            val error =
                OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    ACCESS_TOKEN_REQUIRED_ERROR_MESSAGE,
                    null,
                )
            OAuth2TokenValidatorResult.failure(error)
        }
    }

    private fun accessTokenRevocationValidator(revokedTokenChecker: RevokedTokenChecker): OAuth2TokenValidator<Jwt> {
        return OAuth2TokenValidator { jwt: Jwt ->
            val tokenId = jwt.id
            if (tokenId == null || tokenId.isBlank() || revokedTokenChecker.isRevoked(tokenId)) {
                val error =
                    OAuth2Error(
                        OAuth2ErrorCodes.INVALID_TOKEN,
                        ACCESS_TOKEN_REVOKED_ERROR_MESSAGE,
                        null,
                    )
                return@OAuth2TokenValidator OAuth2TokenValidatorResult.failure(error)
            }
            OAuth2TokenValidatorResult.success()
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun userDetailsService(): UserDetailsService =
        UserDetailsService { username: String ->
            throw UsernameNotFoundException(username)
        }

    companion object {
        private const val ACCESS_TOKEN_REQUIRED_ERROR_MESSAGE = "Token must be an access token."
        private const val ACCESS_TOKEN_REVOKED_ERROR_MESSAGE = "Token has been revoked."
    }
}
