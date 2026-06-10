package com.aspiresys.fp_micro_userservice.config;

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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.extern.java.Log;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Configuración de seguridad para el User Service.
 * 
 * Esta configuración:
 * - Configura el servicio como OAuth2 Resource Server con JWT
 * - Define las reglas de autorización para los endpoints
 * - Configura CORS para permitir acceso desde el frontend
 * - Convierte los claims del JWT en authorities de Spring Security
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize y @PostAuthorize
@Log
public class SecurityConfig {
    @Value("${service.env.frontend.server}")
    private String frontendUrl;

    @Value("${service.env.gateway.server}")
    private String gatewayUrl;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    private final InternalServiceFilter internalServiceFilter;

    public SecurityConfig(InternalServiceFilter internalServiceFilter) {
        this.internalServiceFilter = internalServiceFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Desactiva CSRF para APIs REST
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Habilita CORS
                // Agregar filtro personalizado para endpoints internos
                .addFilterBefore(internalServiceFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authz -> authz
                        // Endpoints públicos para comunicación interna entre microservicios
                        .requestMatchers(HttpMethod.GET, "/users/hello").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/find/**").permitAll() // Endpoint interno para validación de usuarios
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        
                        // Specific endpoints for user management
                        .requestMatchers(HttpMethod.GET, "/users/me").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/users/me/create").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/users/me/update").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/users/me/delete").hasRole("USER")
                        // Allow access to current user info for both USER and ADMIN roles
                        .requestMatchers(HttpMethod.GET, "/users/me/current").hasAnyRole("USER", "ADMIN")
                        // Any other request requires authentication
                        .anyRequest().authenticated()
                )
                // Configurar como servidor de recursos OAuth2 con JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .build();
    }

    /**
     * Configuración del convertidor de autenticación JWT para extraer roles/authorities.
     * Extrae los roles del claim 'authorities', 'roles' o 'scope' del JWT.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Intentar obtener authorities desde diferentes claims posibles
            Collection<String> authorities = null;
            
            // Primero intentar con 'authorities'
            if (jwt.hasClaim("authorities")) {
                authorities = jwt.getClaimAsStringList("authorities");
            }
            // Si no existe, intentar con 'roles'
            else if (jwt.hasClaim("roles")) {
                authorities = jwt.getClaimAsStringList("roles");
            }
            // Si no existe, intentar con 'scope' (separado por espacios)
            else if (jwt.hasClaim("scope")) {
                String scope = jwt.getClaimAsString("scope");
                authorities = Arrays.asList(scope.split(" "));
            }
            
            log.info("Authorities extracted from JWT: " + authorities);
            log.info("JWT Claims: " + jwt.getClaims());
            // Convertir a SimpleGrantedAuthority y asegurar prefijo ROLE_
            if (authorities != null) {
                return authorities.stream()
                        .map(authority -> authority.startsWith("ROLE_") ? authority : "ROLE_" + authority)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }
            
            return Arrays.asList(); // Retornar lista vacía si no hay authorities
        });
        
        return converter;
    }

    /**
     * Configuración CORS para permitir el acceso desde el frontend.
     * Permite requests desde http://localhost:3000 (React frontend) y desde el gateway.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir el origen del frontend y del gateway
        configuration.setAllowedOrigins(Arrays.asList(
            frontendUrl,  // Frontend React
            gatewayUrl    // Gateway
        ));
        
        // Permitir todos los métodos HTTP necesarios
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Permitir todos los headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permitir cookies y credenciales
        configuration.setAllowCredentials(true);
        
        // Configurar para todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * Bean JwtDecoder para decodificar tokens JWT.
     * Este bean es necesario para que Spring Security pueda validar los tokens JWT.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}
