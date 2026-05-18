package com.example.demo.auth.config;

import com.example.demo.auth.AuthConstants;
import com.example.demo.auth.jwt.JwtProperties;
import com.example.demo.auth.jwt.JwtTokenType;
import com.example.demo.auth.revocation.RevokedTokenChecker;
import com.example.demo.poc.inspection.PocInspectionRoutes;
import com.example.demo.user.auth.UserAuthRoutes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    private static final String ACCESS_TOKEN_REQUIRED_ERROR_MESSAGE = "Token must be an access token.";
    private static final String ACCESS_TOKEN_REVOKED_ERROR_MESSAGE = "Token has been revoked.";

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JsonUnauthorizedResponseWriter unauthorizedResponseWriter,
            SecretKey jwtSecretKey,
            RevokedTokenChecker revokedTokenChecker
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                        (request, response, authException) -> unauthorizedResponseWriter.write(response)
                ))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, UserAuthRoutes.REGISTER_PATH).permitAll()
                        .requestMatchers(HttpMethod.POST, UserAuthRoutes.LOGIN_PATH).permitAll()
                        .requestMatchers(HttpMethod.POST, UserAuthRoutes.LOGOUT_PATH).permitAll()
                        .requestMatchers(HttpMethod.POST, UserAuthRoutes.REFRESH_PATH).permitAll()
                        .requestMatchers(HttpMethod.GET, PocInspectionRoutes.USERS_PATH).permitAll()
                        .requestMatchers(HttpMethod.GET, PocInspectionRoutes.REVOKED_TOKENS_PATH).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(userAuthBearerTokenResolver())
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, AuthConstants.BEARER_AUTHENTICATION_SCHEME);
                            unauthorizedResponseWriter.write(response);
                        })
                        .jwt(jwt -> jwt.decoder(accessTokenJwtDecoder(jwtSecretKey, revokedTokenChecker)))
                )
                .build();
    }

    @Bean
    SecretKey jwtSecretKey(JwtProperties jwtProperties) {
        return new SecretKeySpec(jwtProperties.secret().getBytes(StandardCharsets.UTF_8), MacAlgorithm.HS256.toString());
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return NimbusJwtEncoder.withSecretKey(jwtSecretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    private JwtDecoder accessTokenJwtDecoder(SecretKey jwtSecretKey, RevokedTokenChecker revokedTokenChecker) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                accessTokenTypeValidator(),
                accessTokenRevocationValidator(revokedTokenChecker)
        ));
        return jwtDecoder;
    }

    private BearerTokenResolver userAuthBearerTokenResolver() {
        BearerTokenResolver delegate = new DefaultBearerTokenResolver();
        return request -> {
            String requestUri = request.getRequestURI();
            String contextPath = request.getContextPath();
            if ((contextPath + UserAuthRoutes.LOGOUT_PATH).equals(requestUri)
                    || (contextPath + UserAuthRoutes.REFRESH_PATH).equals(requestUri)
                    || (contextPath + PocInspectionRoutes.USERS_PATH).equals(requestUri)
                    || (contextPath + PocInspectionRoutes.REVOKED_TOKENS_PATH).equals(requestUri)) {
                return null;
            }
            return delegate.resolve(request);
        };
    }

    private OAuth2TokenValidator<Jwt> accessTokenTypeValidator() {
        return jwt -> {
            if (JwtTokenType.ACCESS.value().equals(jwt.getClaimAsString(JwtTokenType.CLAIM))) {
                return OAuth2TokenValidatorResult.success();
            }
            OAuth2Error error = new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    ACCESS_TOKEN_REQUIRED_ERROR_MESSAGE,
                    null
            );
            return OAuth2TokenValidatorResult.failure(error);
        };
    }

    private OAuth2TokenValidator<Jwt> accessTokenRevocationValidator(RevokedTokenChecker revokedTokenChecker) {
        return jwt -> {
            String tokenId = jwt.getId();
            if (tokenId == null || tokenId.isBlank() || revokedTokenChecker.isRevoked(tokenId)) {
                OAuth2Error error = new OAuth2Error(
                        OAuth2ErrorCodes.INVALID_TOKEN,
                        ACCESS_TOKEN_REVOKED_ERROR_MESSAGE,
                        null
                );
                return OAuth2TokenValidatorResult.failure(error);
            }
            return OAuth2TokenValidatorResult.success();
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException(username);
        };
    }
}
