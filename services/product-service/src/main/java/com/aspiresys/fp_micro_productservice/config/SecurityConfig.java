package com.aspiresys.fp_micro_productservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Security configuration for the Product Service.
 *
 * This configuration:
 * - Sets up the service as an OAuth2 Resource Server using JWT
 * - Defines authorization rules for the endpoints
 * - Configures CORS to allow access from the frontend
 * - Converts JWT claims into Spring Security authorities
 * - Restricts create, update, and delete operations to ADMIN role
 * - Allows public access to product read endpoints
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${service.env.frontend.server}")
    private String frontendUrl;

    @Value("${service.env.gateway.server}")
    private String gatewayUrl;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for REST APIs
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers(HttpMethod.GET, "/products").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // Endpoints requiring ADMIN role
                        .requestMatchers(HttpMethod.POST, "/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/products/**").hasRole("ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .build();
    }

    /**
     * Configures a JWT authentication converter to extract roles/authorities.
     * Attempts to read from the 'authorities', 'roles', or 'scope' claim in the JWT.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<String> authorities = null;

            if (jwt.hasClaim("authorities")) {
                authorities = jwt.getClaimAsStringList("authorities");
            } else if (jwt.hasClaim("roles")) {
                authorities = jwt.getClaimAsStringList("roles");
            } else if (jwt.hasClaim("scope")) {
                String scope = jwt.getClaimAsString("scope");
                authorities = Arrays.asList(scope.split(" "));
            }

            if (authorities != null) {
                return authorities.stream()
                        .map(auth -> auth.startsWith("ROLE_") ? auth : "ROLE_" + auth)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }

            return Arrays.asList();
        });

        return converter;
    }

    /**
     * CORS configuration to allow access from the frontend and gateway.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow the frontend and gateway origins
        configuration.setAllowedOrigins(Arrays.asList(frontendUrl, gatewayUrl));

        // Allow all necessary HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Allow credentials (cookies)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * JwtDecoder bean to decode and validate JWT tokens.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}
