package com.aspiresys.fp_micro_userservice.config;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.java.Log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filtro para validar peticiones internas entre microservicios.
 * Solo permite acceso al endpoint /users/find desde servicios internos autorizados.
 */
@Component
@Log
public class InternalServiceFilter extends OncePerRequestFilter {

    // Lista de User-Agents permitidos para servicios internos
    private static final List<String> ALLOWED_INTERNAL_SERVICES = Arrays.asList(
        "order-service", 
        "internal-microservice",
        "ReactorNetty" // WebClient de Spring usa este User-Agent
    );

    // Header personalizado para identificar servicios internos
    private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service";
    private static final String INTERNAL_SERVICE_SECRET = "internal-secret-key-2024"; // En producción, usar configuración externa

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // Solo aplicar filtro al endpoint interno
        if (requestPath.equals("/users/find")) {
            if (!isInternalServiceRequest(request)) {
                log.warning("Unauthorized access attempt to internal endpoint: " + requestPath + 
                           " from IP: " + request.getRemoteAddr());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\": \"Access denied - Internal service only\"}");
                response.setContentType("application/json");
                return;
            }
            log.info("Internal service access granted to: " + requestPath);
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Verifica si la petición proviene de un servicio interno autorizado
     */
    private boolean isInternalServiceRequest(HttpServletRequest request) {
        // Verificar header personalizado
        String internalHeader = request.getHeader(INTERNAL_SERVICE_HEADER);
        if (INTERNAL_SERVICE_SECRET.equals(internalHeader)) {
            return true;
        }
        
        // Verificar User-Agent como respaldo
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            return ALLOWED_INTERNAL_SERVICES.stream()
                    .anyMatch(service -> userAgent.contains(service));
        }
        
        // Verificar IP local (localhost)
        String remoteAddr = request.getRemoteAddr();
        return "127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr);
    }
}
