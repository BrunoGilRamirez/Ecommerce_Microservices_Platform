package com.aspiresys.fp_micro_userservice.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Aspecto específico para operaciones del dominio de usuarios.
 * 
 * <p>Este aspecto proporciona funcionalidades transversales específicas para
 * el servicio de usuarios, incluyendo logging detallado, métricas de negocio
 * y manejo de casos especiales del dominio.</p>
 * 
 * <h3>Funcionalidades:</h3>
 * <ul>
 *   <li>Logging especializado para operaciones de usuarios</li>
 *   <li>Métricas de negocio (usuarios creados, eliminados, etc.)</li>
 *   <li>Validaciones específicas del dominio</li>
 *   <li>Alertas para operaciones críticas</li>
 *   <li>Seguimiento de patrones de uso</li>
 * </ul>
 * 
 * @author bruno.gil
 * @since 1.0
 */
@Aspect
@Component
@Slf4j
public class UserOperationAspect {

    /**
     * Intercepta operaciones de guardado de usuarios para logging especializado.
     * 
     * @param joinPoint información del punto de unión
     * @return resultado de la operación
     * @throws Throwable si ocurre error durante la ejecución
     */
    @Around("execution(* com.aspiresys.fp_micro_userservice.user.UserServiceImpl.saveUser(..))")
    public Object logUserSaveOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Object[] args = joinPoint.getArgs();
        
        // Log estructurado multilínea - inicio
        StringBuilder startLog = new StringBuilder();
        startLog.append("\n[USER-SAVE-OPERATION-START] ").append(timestamp);
        startLog.append("\n|- Operation: SAVE_USER");
        startLog.append("\n|- Method: ").append(joinPoint.getSignature().getName());
        startLog.append("\n|- Class: ").append(joinPoint.getTarget().getClass().getSimpleName());
        startLog.append("\n|- Arguments count: ").append(args.length);
        if (args.length > 0 && args[0] != null) {
            startLog.append("\n|- User data: ").append(formatUserData(args[0]));
        }
        
        log.info(startLog.toString());
        
        try {
            Object result = joinPoint.proceed();
            
            // Log estructurado multilínea - éxito
            StringBuilder successLog = new StringBuilder();
            successLog.append("\n[USER-SAVE-OPERATION-SUCCESS] ").append(timestamp);
            successLog.append("\n|- Operation: SAVE_USER");
            successLog.append("\n|- Result type: ").append(getResultType(result));
            successLog.append("\n|- Status: SUCCESS");
            successLog.append("\n|_ Execution completed successfully");
            
            log.info(successLog.toString());
            
            // Log de métricas de negocio
            logBusinessMetric("USER_CREATED", args.length > 0 ? args[0] : null);
            
            return result;
            
        } catch (Exception e) {
            // Log estructurado multilínea - error
            StringBuilder errorLog = new StringBuilder();
            errorLog.append("\n[USER-SAVE-OPERATION-ERROR] ").append(timestamp);
            errorLog.append("\n|- Operation: SAVE_USER");
            errorLog.append("\n|- Exception: ").append(e.getClass().getSimpleName());
            errorLog.append("\n|- Message: ").append(e.getMessage());
            errorLog.append("\n|- Status: ERROR");
            errorLog.append("\n|_ Operation failed");
            
            log.error(errorLog.toString());
            throw e;
        }
    }

    /**
     * Intercepta operaciones de eliminación de usuarios.
     * 
     * @param joinPoint información del punto de unión
     * @return resultado de la operación
     * @throws Throwable si ocurre error durante la ejecución
     */
    @Around("execution(* com.aspiresys.fp_micro_userservice.user.UserServiceImpl.deleteUser*(..))")
    public Object logUserDeleteOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        // Log estructurado multilínea - inicio
        StringBuilder startLog = new StringBuilder();
        startLog.append("\n[USER-DELETE-OPERATION-START] ").append(timestamp);
        startLog.append("\n|- Operation: ").append(methodName.toUpperCase());
        startLog.append("\n|- Method: ").append(methodName);
        startLog.append("\n|- Arguments: ").append(formatParameters(args));
        
        log.info(startLog.toString());
        
        try {
            Object result = joinPoint.proceed();
            
            // Log estructurado multilínea - éxito
            StringBuilder successLog = new StringBuilder();
            successLog.append("\n[USER-DELETE-OPERATION-SUCCESS] ").append(timestamp);
            successLog.append("\n|- Operation: ").append(methodName.toUpperCase());
            successLog.append("\n|- Result: ").append(result);
            successLog.append("\n|- Status: SUCCESS");
            successLog.append("\n|_ Deletion completed");
            
            log.info(successLog.toString());
            
            // Alertar sobre eliminaciones (operación crítica)
            if (Boolean.TRUE.equals(result)) {
                StringBuilder alertLog = new StringBuilder();
                alertLog.append("\n[USER-DELETE-ALERT] ").append(timestamp);
                alertLog.append("\n|- Type: USER_DELETED");
                alertLog.append("\n|- Method: ").append(methodName);
                alertLog.append("\n|- Parameters: ").append(formatParameters(args));
                alertLog.append("\n|_ Critical operation: User has been deleted");
                
                log.warn(alertLog.toString());
            }
            
            return result;
            
        } catch (Exception e) {
            // Log estructurado multilínea - error
            StringBuilder errorLog = new StringBuilder();
            errorLog.append("\n[USER-DELETE-OPERATION-ERROR] ").append(timestamp);
            errorLog.append("\n|- Operation: ").append(methodName.toUpperCase());
            errorLog.append("\n|- Exception: ").append(e.getClass().getSimpleName());
            errorLog.append("\n|- Message: ").append(e.getMessage());
            errorLog.append("\n|- Status: ERROR");
            errorLog.append("\n|_ Delete operation failed");
            
            log.error(errorLog.toString());
            throw e;
        }
    }

    /**
     * Intercepta operaciones de búsqueda de usuarios para métricas.
     * 
     * @param joinPoint información del punto de unión
     * @param result resultado de la operación
     */
    @AfterReturning(pointcut = "execution(* com.aspiresys.fp_micro_userservice.user.UserServiceImpl.getUser*(..))", returning = "result")
    public void logUserSearchOperation(JoinPoint joinPoint, Object result) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String methodName = joinPoint.getSignature().getName();
        
        String resultInfo = getSearchResultInfo(result);
        
        // Log estructurado multilínea - búsqueda
        StringBuilder searchLog = new StringBuilder();
        searchLog.append("\n[USER-SEARCH-OPERATION-SUCCESS] ").append(timestamp);
        searchLog.append("\n|- Operation: ").append(methodName.toUpperCase());
        searchLog.append("\n|- ").append(resultInfo);
        searchLog.append("\n|- Status: SUCCESS");
        searchLog.append("\n|_ Search completed");
        
        log.info(searchLog.toString());
        
        // Métricas de búsqueda
        if (methodName.equals("getAllUsers") && result instanceof List) {
            int userCount = ((List<?>) result).size();
            logBusinessMetric("USERS_LISTED", userCount);
        }
    }

    /**
     * Intercepta operaciones de actualización de usuarios.
     * 
     * @param joinPoint información del punto de unión
     * @param result resultado de la operación
     */
    @AfterReturning(pointcut = "execution(* com.aspiresys.fp_micro_userservice.user.UserServiceImpl.updateUser(..))", returning = "result")
    public void logUserUpdateOperation(JoinPoint joinPoint, Object result) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        // Log estructurado multilínea - actualización
        StringBuilder updateLog = new StringBuilder();
        updateLog.append("\n[USER-UPDATE-OPERATION-SUCCESS] ").append(timestamp);
        updateLog.append("\n|- Operation: UPDATE_USER");
        updateLog.append("\n|- Result type: ").append(getResultType(result));
        updateLog.append("\n|- Status: SUCCESS");
        updateLog.append("\n|_ Update completed");
        
        log.info(updateLog.toString());
        
        logBusinessMetric("USER_UPDATED", result);
    }

    /**
     * Maneja errores en operaciones de usuarios.
     * 
     * @param joinPoint información del punto de unión
     * @param exception excepción lanzada
     */
    @AfterThrowing(pointcut = "execution(* com.aspiresys.fp_micro_userservice.user.UserServiceImpl.*(..))", throwing = "exception")
    public void handleUserOperationError(JoinPoint joinPoint, Exception exception) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String methodName = joinPoint.getSignature().getName();
        
        // Log estructurado multilínea - error general
        StringBuilder errorLog = new StringBuilder();
        errorLog.append("\n[USER-OPERATION-ERROR] ").append(timestamp);
        errorLog.append("\n|- Method: ").append(methodName);
        errorLog.append("\n|- Exception: ").append(exception.getClass().getSimpleName());
        errorLog.append("\n|- Message: ").append(exception.getMessage());
        errorLog.append("\n|_ Operation failed with exception");
        
        log.error(errorLog.toString());
        
        // Alertas para errores críticos
        if (exception instanceof IllegalArgumentException && 
            (methodName.equals("saveUser") || methodName.equals("updateUser"))) {
            StringBuilder alertLog = new StringBuilder();
            alertLog.append("\n[USER-VALIDATION-ALERT] ").append(timestamp);
            alertLog.append("\n|- Type: VALIDATION_ERROR");
            alertLog.append("\n|- Method: ").append(methodName);
            alertLog.append("\n|- Error: ").append(exception.getMessage());
            alertLog.append("\n|_ Validation failed for critical operation");
            
            log.warn(alertLog.toString());
        }
    }

    /**
     * Determina el tipo de resultado para logging.
     * 
     * @param result resultado del método
     * @return descripción del tipo de resultado
     */
    private String getResultType(Object result) {
        if (result == null) {
            return "null";
        }
        
        String className = result.getClass().getSimpleName();
        
        if (className.equals("ResponseEntity")) {
            return "ResponseEntity";
        }
        
        if (result instanceof List) {
            return String.format("List[%d]", ((List<?>) result).size());
        }
        
        return className;
    }

    /**
     * Obtiene información sobre el resultado de búsquedas.
     * 
     * @param result resultado de la búsqueda
     * @return información formateada del resultado
     */
    private String getSearchResultInfo(Object result) {
        if (result == null) {
            return "result=null";
        }
        
        if (result instanceof List) {
            int size = ((List<?>) result).size();
            return String.format("result_type=List|count=%d", size);
        }
        
        return String.format("result_type=%s|found=true", result.getClass().getSimpleName());
    }

    /**
     * Formatea parámetros para logging seguro.
     * 
     * @param args parámetros del método
     * @return string formateado de parámetros
     */
    private String formatParameters(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        StringBuilder params = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) params.append(", ");
            
            if (args[i] == null) {
                params.append("null");
            } else if (args[i] instanceof String && args[i].toString().contains("@")) {
                // Enmascarar emails parcialmente para privacidad
                String email = args[i].toString();
                int atIndex = email.indexOf("@");
                if (atIndex > 2) {
                    params.append(email.substring(0, 2)).append("***@").append(email.substring(atIndex + 1));
                } else {
                    params.append("***@").append(email.substring(atIndex + 1));
                }
            } else {
                params.append(args[i].getClass().getSimpleName());
            }
        }
        params.append("]");
        return params.toString();
    }

    /**
     * Formatea datos de usuario para logging seguro.
     * 
     * @param userData datos del usuario
     * @return string formateado de datos de usuario
     */
    private String formatUserData(Object userData) {
        if (userData == null) {
            return "null";
        }
        
        try {
            // Usar reflection de forma segura para obtener información básica
            String className = userData.getClass().getSimpleName();
            String toString = userData.toString();
            
            // Si contiene información sensible, enmascararla
            if (toString.contains("@")) {
                toString = toString.replaceAll("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b", "***@***.***");
            }
            
            return String.format("%s: %s", className, toString.length() > 100 ? 
                toString.substring(0, 100) + "..." : toString);
                
        } catch (Exception e) {
            return userData.getClass().getSimpleName() + ": [data masked for security]";
        }
    }

    /**
     * Registra métricas de negocio para análisis posterior.
     * 
     * @param metric tipo de métrica
     * @param data datos asociados
     */
    private void logBusinessMetric(String metric, Object data) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        // Log estructurado multilínea - métrica de negocio
        StringBuilder metricsLog = new StringBuilder();
        metricsLog.append("\n[USER-BUSINESS-METRIC] ").append(timestamp);
        metricsLog.append("\n|- Metric: ").append(metric);
        if (data instanceof Number) {
            metricsLog.append("\n|- Value: ").append(data);
        } else if (data != null) {
            metricsLog.append("\n|- Data Type: ").append(data.getClass().getSimpleName());
        }
        metricsLog.append("\n|_ Business metric recorded");
        
        log.info(metricsLog.toString());
    }
}
