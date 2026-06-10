package com.aspiresys.fp_micro_userservice.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aspiresys.fp_micro_userservice.aop.annotation.Auditable;
import com.aspiresys.fp_micro_userservice.config.AopProperties;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Aspecto para auditoría automática de operaciones en el servicio de usuarios.
 * 
 * <p>Este aspecto intercepta métodos anotados con {@link Auditable} y registra
 * información detallada sobre la ejecución, incluyendo timestamp, operación,
 * parámetros, resultados y estado de la ejecución.</p>
 * 
 * <h3>Funcionalidades:</h3>
 * <ul>
 *   <li>Auditoría de operaciones exitosas</li>
 *   <li>Auditoría de operaciones con errores</li>
 *   <li>Logging estructurado para herramientas de monitoreo</li>
 *   <li>Filtrado automático de información sensible</li>
 *   <li>Configuración flexible via properties</li>
 * </ul>
 * 
 * @author bruno.gil
 * @see Auditable
 * @since 1.0
 */
@Aspect
@Component
@Slf4j
public class AuditAspect {

    @Autowired
    private AopProperties aopProperties;

    /**
     * Audita la ejecución exitosa de métodos anotados con @Auditable.
     * 
     * @param joinPoint información del punto de unión
     * @param auditable anotación con configuración de auditoría
     * @param result resultado de la ejecución del método
     */
    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void auditSuccessfulOperation(JoinPoint joinPoint, Auditable auditable, Object result) {
        if (!aopProperties.getAudit().isEnabled()) {
            return;
        }

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Object[] args = joinPoint.getArgs();

        StringBuilder auditLog = new StringBuilder();
        auditLog.append("\n[USER-AUDIT-SUCCESS] ").append(timestamp);
        auditLog.append("\n|- Operation: ").append(auditable.operation());
        auditLog.append("\n|- Entity: ").append(auditable.entityType());
        auditLog.append("\n|- Class: ").append(className);
        auditLog.append("\n|- Method: ").append(methodName);
        auditLog.append("\n|- Status: SUCCESS");

        // Registrar parámetros si está habilitado
        if (auditable.logParameters() && aopProperties.getAudit().isIncludeParameters()) {
            auditLog.append("\n|- Parameters: ").append(formatParameters(args));
        }

        // Registrar resultado si está habilitado
        if (auditable.logResult() && aopProperties.getAudit().isIncludeReturnValue()) {
            auditLog.append("\n|_ Result: ").append(formatResult(result));
        } else {
            auditLog.append("\n|_ Execution completed successfully");
        }

        log.info(auditLog.toString());
        
        // Log adicional para métricas
        logAuditMetrics(timestamp, auditable.operation(), className, methodName, "SUCCESS", args.length);
    }

    /**
     * Audita errores en la ejecución de métodos anotados con @Auditable.
     * 
     * @param joinPoint información del punto de unión
     * @param auditable anotación con configuración de auditoría
     * @param exception excepción lanzada durante la ejecución
     */
    @AfterThrowing(pointcut = "@annotation(auditable)", throwing = "exception")
    public void auditFailedOperation(JoinPoint joinPoint, Auditable auditable, Exception exception) {
        if (!aopProperties.getAudit().isEnabled()) {
            return;
        }

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Object[] args = joinPoint.getArgs();

        StringBuilder auditLog = new StringBuilder();
        auditLog.append("\n[USER-AUDIT-ERROR] ").append(timestamp);
        auditLog.append("\n|- Operation: ").append(auditable.operation());
        auditLog.append("\n|- Entity: ").append(auditable.entityType());
        auditLog.append("\n|- Class: ").append(className);
        auditLog.append("\n|- Method: ").append(methodName);
        auditLog.append("\n|- Status: ERROR");
        auditLog.append("\n|- Exception: ").append(exception.getClass().getSimpleName());
        auditLog.append("\n|- Error Message: ").append(exception.getMessage());

        // Registrar parámetros si está habilitado (útil para debugging)
        if (auditable.logParameters() && aopProperties.getAudit().isIncludeParameters()) {
            auditLog.append("\n|- Parameters: ").append(formatParameters(args));
        }

        auditLog.append("\n|_ Operation failed");

        log.error(auditLog.toString());
        
        // Log adicional para métricas de errores
        logAuditMetrics(timestamp, auditable.operation(), className, methodName, "ERROR", args.length);
    }

    /**
     * Formatea los parámetros para logging, enmascarando información sensible.
     * 
     * @param args parámetros del método
     * @return string formateado con los parámetros
     */
    private String formatParameters(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        StringBuilder params = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) params.append(", ");
            
            Object parameter = args[i];
            if (parameter == null) {
                params.append("null");
                continue;
            }

            String className = parameter.getClass().getSimpleName();
            if (className.toLowerCase().contains("password") || 
                className.toLowerCase().contains("credential") ||
                className.toLowerCase().contains("secret")) {
                params.append("***MASKED***");
                continue;
            }

            if (parameter.getClass().isArray()) {
                params.append("Array[").append(java.lang.reflect.Array.getLength(parameter)).append("]");
            } else if (parameter.toString().length() > 100) {
                params.append(className).append("(truncated...)");
            } else {
                params.append(parameter.toString());
            }
        }
        params.append("]");
        return params.toString();
    }

    /**
     * Formatea el resultado para logging, filtrando información sensible.
     * 
     * @param result resultado del método
     * @return string formateado del resultado
     */
    private String formatResult(Object result) {
        if (result == null) {
            return "null";
        }

        String className = result.getClass().getSimpleName();
        if (result.toString().length() > 200) {
            return className + "(large object - content truncated)";
        }

        return result.toString();
    }

    /**
     * Registra métricas de auditoría en formato estructurado.
     * 
     * @param timestamp timestamp de la operación
     * @param operation nombre de la operación
     * @param className clase donde se ejecutó
     * @param methodName método ejecutado
     * @param status estado de la ejecución
     * @param paramCount número de parámetros
     */
    private void logAuditMetrics(String timestamp, String operation, String className, 
                                String methodName, String status, int paramCount) {
        StringBuilder metricsLog = new StringBuilder();
        metricsLog.append("\n[USER-AUDIT-METRICS] ").append(timestamp);
        metricsLog.append("\n|- Operation: ").append(operation);
        metricsLog.append("\n|- Class: ").append(className);
        metricsLog.append("\n|- Method: ").append(methodName);
        metricsLog.append("\n|- Status: ").append(status);
        metricsLog.append("\n|- Param Count: ").append(paramCount);
        metricsLog.append("\n|_ Metrics recorded");
        
        log.info(metricsLog.toString());
    }
}
