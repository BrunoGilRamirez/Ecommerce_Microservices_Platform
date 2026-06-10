package com.aspiresys.fp_micro_userservice.aop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aspiresys.fp_micro_userservice.aop.annotation.ExecutionTime;
import com.aspiresys.fp_micro_userservice.config.AopProperties;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Aspecto para medición automática de tiempo de ejecución en el servicio de usuarios.
 * 
 * <p>Este aspecto intercepta métodos anotados con {@link ExecutionTime} y mide
 * el tiempo de ejecución, generando logs de rendimiento y alertas cuando se
 * superan los umbrales configurados.</p>
 * 
 * <h3>Funcionalidades:</h3>
 * <ul>
 *   <li>Medición precisa de tiempo de ejecución</li>
 *   <li>Alertas automáticas para operaciones lentas</li>
 *   <li>Logging estructurado para monitoreo</li>
 *   <li>Manejo de errores durante la medición</li>
 *   <li>Configuración flexible de umbrales</li>
 * </ul>
 * 
 * @author bruno.gil
 * @see ExecutionTime
 * @since 1.0
 */
@Aspect
@Component
@Slf4j
public class ExecutionTimeAspect {

    @Autowired
    private AopProperties aopProperties;

    /**
     * Intercepta métodos anotados con @ExecutionTime y mide su tiempo de ejecución.
     * 
     * @param joinPoint información del punto de unión
     * @param executionTime anotación con configuración de medición
     * @return resultado del método interceptado
     * @throws Throwable si ocurre error durante la ejecución
     */
    @Around("@annotation(executionTime)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint, ExecutionTime executionTime) throws Throwable {
        if (!aopProperties.getPerformance().isEnabled()) {
            return joinPoint.proceed();
        }

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String operationName = executionTime.operation().isEmpty() ? methodName : executionTime.operation();
        
        long startTime = System.currentTimeMillis();
        String startTimestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        log.info(String.format("USER_PERFORMANCE|START|timestamp=%s|operation=%s|class=%s|method=%s",
                startTimestamp, operationName, className, methodName));

        Object result = null;
        Exception exception = null;
        boolean success = true;

        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            exception = e;
            success = false;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTimeMs = endTime - startTime;
            
            logExecutionTime(operationName, className, methodName, executionTimeMs, success, 
                           executionTime, exception, startTimestamp);
        }

        return result;
    }

    /**
     * Registra información detallada sobre el tiempo de ejecución.
     * 
     * @param operationName nombre de la operación
     * @param className clase donde se ejecutó
     * @param methodName método ejecutado
     * @param executionTimeMs tiempo de ejecución en milisegundos
     * @param success indica si la ejecución fue exitosa
     * @param executionTime anotación con configuración
     * @param exception excepción si hubo error
     * @param startTimestamp timestamp de inicio
     */
    private void logExecutionTime(String operationName, String className, String methodName, 
                                 long executionTimeMs, boolean success, ExecutionTime executionTime,
                                 Exception exception, String startTimestamp) {
        
        StringBuilder perfLog = new StringBuilder();
        perfLog.append("USER_PERFORMANCE|");
        perfLog.append("operation=").append(operationName);
        perfLog.append("|timestamp=").append(startTimestamp);
        
        if (executionTime.detailed() || aopProperties.getPerformance().isIncludeDetails()) {
            perfLog.append("\n|- Class: ").append(className);
            perfLog.append("\n|- Method: ").append(methodName);
            perfLog.append("\n|- Execution Time: ").append(executionTimeMs).append("ms");
            perfLog.append("\n|- Success: ").append(success);
            
            if (!success && exception != null) {
                perfLog.append("\n|- Exception: ").append(exception.getClass().getSimpleName());
                perfLog.append("\n|- Error: ").append(exception.getMessage());
            }
            
            // Verificar umbral de advertencia
            if (executionTimeMs > executionTime.warningThreshold()) {
                perfLog.append("\n|- ⚠️  WARNING: Execution time exceeded threshold (")
                       .append(executionTime.warningThreshold()).append("ms)");
            }
            
            perfLog.append("\n|_ Measurement completed");
        }

        // Log principal
        if (executionTimeMs > executionTime.warningThreshold() && 
            aopProperties.getPerformance().isLogSlowOperations()) {
            log.warn(perfLog.toString());
        } else {
            log.info(perfLog.toString());
        }

        // Log de métricas estructurado
        logMetrics(operationName, className, methodName, executionTimeMs, success);
    }

    /**
     * Registra métricas en formato estructurado para herramientas de monitoreo.
     * 
     * @param operation nombre de la operación
     * @param className clase ejecutada
     * @param methodName método ejecutado
     * @param executionTime tiempo de ejecución
     * @param success estado de ejecución
     */
    private void logMetrics(String operation, String className, String methodName, 
                           long executionTime, boolean success) {
        StringBuilder metricsLog = new StringBuilder();
        metricsLog.append("\n[USER-PERFORMANCE-METRICS] ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        metricsLog.append("\n|- Operation: ").append(operation);
        metricsLog.append("\n|- Class: ").append(className);
        metricsLog.append("\n|- Method: ").append(methodName);
        metricsLog.append("\n|- Execution Time: ").append(executionTime).append("ms");
        metricsLog.append("\n|- Success: ").append(success);
        metricsLog.append("\n|_ Performance metrics recorded");
        
        log.info(metricsLog.toString());
    }
}
