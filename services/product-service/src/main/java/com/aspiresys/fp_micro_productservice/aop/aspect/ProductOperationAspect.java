package com.aspiresys.fp_micro_productservice.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.aspiresys.fp_micro_productservice.product.Product;

import lombok.extern.java.Log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Specific aspect for product operations.
 * Provides specialized logging, security validations and business metrics.
 * 
 * Pointcuts are annotations to define where the aspect should apply.
 * This aspect includes:
 * - Logging before and after operations in the controller
 * - Logging and additional validations for product modification operations
 * - Logging after successful operations in the service
 * - Logging when errors occur in the service
 * - Additional specific validations for product operations
 * 
 * @author bruno.gil
 */
@Aspect
@Component
@Log
public class ProductOperationAspect {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Pointcut para todos los métodos del ProductController
     */
    @Pointcut("execution(* com.aspiresys.fp_micro_productservice.product.ProductController.*(..))")
    public void productControllerMethods() {}
    
    /**
     * Pointcut para todos los métodos del ProductService
     */
    @Pointcut("execution(* com.aspiresys.fp_micro_productservice.product.ProductService.*(..))")
    public void productServiceMethods() {}
    
    /**
     * Pointcut para métodos que modifican productos (save, delete)
     */
    @Pointcut("execution(* com.aspiresys.fp_micro_productservice.product.ProductService.saveProduct(..)) || " +
              "execution(* com.aspiresys.fp_micro_productservice.product.ProductService.deleteProduct(..))")
    public void productModificationMethods() {}
    
    /**
     * Log antes de operaciones en el controller
     */
    @Before("productControllerMethods()")
    public void logBeforeProductController(JoinPoint joinPoint) {
        String userEmail = getCurrentUserEmail();
        String methodName = joinPoint.getSignature().getName();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        
        log.info(String.format("[PRODUCT-CONTROLLER] %s - User: %s - Method: %s - Args: %d", 
                timestamp, userEmail, methodName, joinPoint.getArgs().length));
    }
    
    /**
     * Log y validaciones adicionales para operaciones que modifican productos
     */
    @Around("productModificationMethods()")
    public Object logProductModifications(ProceedingJoinPoint joinPoint) throws Throwable {
        String userEmail = getCurrentUserEmail();
        String methodName = joinPoint.getSignature().getName();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        long startTime = System.currentTimeMillis();
        
        // Log inicio de operación crítica
        log.info(String.format("\n[PRODUCT-MODIFICATION-START] %s\n|- User: %s\n|- Operation: %s\n└─ Critical product operation initiated", 
                timestamp, userEmail, methodName));
        
        try {
            // Validaciones adicionales para productos
            validateProductOperation(joinPoint);
            
            // Ejecutar método original
            Object result = joinPoint.proceed();
            
            // Log éxito
            long executionTime = System.currentTimeMillis() - startTime;
            log.info(String.format("\n[PRODUCT-MODIFICATION-SUCCESS] %s\n|- User: %s\n|- Operation: %s\n|- Duration: %d ms\n└─ Product operation completed successfully", 
                    LocalDateTime.now().format(TIMESTAMP_FORMAT), userEmail, methodName, executionTime));
            
            return result;
            
        } catch (Exception e) {
            // Log error
            long executionTime = System.currentTimeMillis() - startTime;
            log.severe(String.format("\n[PRODUCT-MODIFICATION-ERROR] %s\n|- User: %s\n|- Operation: %s\n|- Duration: %d ms\n|- Error: %s\n└─ Message: %s", 
                    LocalDateTime.now().format(TIMESTAMP_FORMAT), userEmail, methodName, executionTime, 
                    e.getClass().getSimpleName(), e.getMessage()));
            
            throw e;
        }
    }
    
    /**
     * Log después de operaciones exitosas en el service
     */
    @AfterReturning(pointcut = "productServiceMethods()", returning = "result")
    public void logAfterProductService(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String resultInfo = getResultInfo(result);
        
        log.info(String.format("[PRODUCT-SERVICE] Method: %s completed - Result: %s", 
                methodName, resultInfo));
    }
    
    /**
     * Log cuando ocurren errores en el service
     */
    @AfterThrowing(pointcut = "productServiceMethods()", throwing = "exception")
    public void logProductServiceErrors(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().getName();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        
        log.warning(String.format("\n[PRODUCT-SERVICE-ERROR] %s\n|- Method: %s\n|- Exception: %s\n└─ Message: %s", 
                timestamp, methodName, exception.getClass().getSimpleName(), exception.getMessage()));
    }
    
    /**
     * Validaciones adicionales específicas para operaciones de productos
     */
    private void validateProductOperation(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();
        
        // Validar si hay un producto en los parámetros
        for (Object arg : args) {
            if (arg instanceof Product) {
                Product product = (Product) arg;
                
                // Validaciones específicas para productos
                if (methodName.equals("saveProduct")) {
                    if (product.getName() == null || product.getName().trim().isEmpty()) {
                        throw new IllegalArgumentException("Product name cannot be null or empty");
                    }
                    if (product.getPrice() == null || product.getPrice() <= 0) {
                        throw new IllegalArgumentException("Product price must be greater than 0");
                    }
                }
                
                log.fine(String.format("Product validation passed for %s - Product: %s", 
                        methodName, product.getName() != null ? product.getName() : "Unknown"));
            }
            
            // Validar IDs para operaciones de eliminación
            if (methodName.equals("deleteProduct") && arg instanceof Long) {
                Long productId = (Long) arg;
                if (productId <= 0) {
                    throw new IllegalArgumentException("Product ID must be greater than 0");
                }
            }
        }
    }
    
    /**
     * Obtiene información segura sobre el resultado de la operación
     */
    private String getResultInfo(Object result) {
        if (result == null) {
            return "null";
        }
        
        String className = result.getClass().getSimpleName();
        
        // Para ResponseEntity, extraer información del body
        if (className.equals("ResponseEntity")) {
            return "ResponseEntity[" + result.toString().length() + " chars]";
        }
        
        // Para colecciones, mostrar tamaño
        if (result instanceof java.util.Collection) {
            return "Collection[" + ((java.util.Collection<?>) result).size() + " items]";
        }
        
        // Para productos individuales
        if (result instanceof Product) {
            Product product = (Product) result;
            return "Product[id=" + product.getId() + ", name=" + product.getName() + "]";
        }
        
        return className;
    }
    
    /**
     * Obtiene el email del usuario actual
     */
    private String getCurrentUserEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                return jwt.getClaimAsString("sub");
            }
            return "SYSTEM";
        } catch (Exception e) {
            return "PUBLIC_USER";
        }
    }
}
