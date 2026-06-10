package com.aspiresys.fp_micro_productservice.aop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.aspiresys.fp_micro_productservice.aop.annotation.ExecutionTime;

import lombok.extern.java.Log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Aspect for measuring method execution time.
 * Provides performance metrics and performance alerts.
 * 
 * @author bruno.gil
 */
@Aspect
@Component
@Log
public class ExecutionTimeAspect {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * Wraps method execution to measure time.
     * Logs the start and end time, execution duration, and status.
     */
    @Around("@annotation(executionTime)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint, ExecutionTime executionTime) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String operationName = executionTime.operation().isEmpty() ? methodName : executionTime.operation();
        
        // Registrar inicio
        long startTime = System.currentTimeMillis();
        String startTimestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        
        if (executionTime.detailed()) {
            log.info(String.format("[PRODUCT-PERFORMANCE-START] %s - Operation: %s (%s.%s)", 
                    startTimestamp, operationName, className, methodName));
        }
        
        Object result = null;
        boolean success = true;
        Throwable exception = null;
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable throwable) {
            success = false;
            exception = throwable;
            throw throwable;
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTimeMs = endTime - startTime;
            String endTimestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            
            StringBuilder perfLog = new StringBuilder();
            perfLog.append("\n[PRODUCT-PERFORMANCE-REPORT] ").append(endTimestamp);
            perfLog.append("\n|- Operation: ").append(operationName);
            perfLog.append("\n|- Class: ").append(className);
            perfLog.append("\n|- Method: ").append(methodName);
            perfLog.append("\n|- Execution Time: ").append(executionTimeMs).append(" ms");
            perfLog.append("\n|- Status: ").append(success ? "SUCCESS" : "ERROR");
            
            if (executionTime.detailed()) {
                perfLog.append("\n|- Start Time: ").append(startTimestamp);
                perfLog.append("\n|- End Time: ").append(endTimestamp);
                if (!success && exception != null) {
                    perfLog.append("\n|- Exception: ").append(exception.getClass().getSimpleName());
                }
            }
            
            if (executionTimeMs > executionTime.warningThreshold()) {
                perfLog.append("\n|_ WARNING: Execution time exceeded threshold (")
                       .append(executionTime.warningThreshold()).append(" ms)");
                log.warning(perfLog.toString());
            } else {
                perfLog.append("\n|_ Performance: NORMAL");
                log.info(perfLog.toString());
            }
            
            logMetrics(operationName, className, methodName, executionTimeMs, success);
        }
    }
    
    /**
     * Logs performance metrics for the operation.
     * This could be integrated with monitoring systems like Prometheus or Micrometer.
     */
    private void logMetrics(String operation, String className, String methodName, 
                           long executionTime, boolean success) {
        
        String metricsLog = String.format(
            "PRODUCT_METRICS|operation=%s|class=%s|method=%s|execution_time_ms=%d|success=%s|timestamp=%s",
            operation, className, methodName, executionTime, success, 
            LocalDateTime.now().format(TIMESTAMP_FORMAT)
        );
        
        log.info("[PRODUCT-METRICS] " + metricsLog);
    }
}
