package com.aspiresys.fp_micro_userservice.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.aspiresys.fp_micro_userservice.aop.annotation.ValidateParameters;
import com.aspiresys.fp_micro_userservice.config.AopProperties;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Aspecto para validación automática de parámetros en el servicio de usuarios.
 * 
 * <p>Este aspecto intercepta métodos anotados con {@link ValidateParameters} y aplica
 * validaciones automáticas a los parámetros de entrada antes de la ejecución del método.</p>
 * 
 * <h3>Validaciones soportadas:</h3>
 * <ul>
 *   <li><b>Not Null</b>: Verifica que los parámetros no sean nulos</li>
 *   <li><b>Not Empty</b>: Verifica que strings y colecciones no estén vacías</li>
 *   <li><b>Email Format</b>: Valida formato de emails usando regex</li>
 *   <li><b>Fail Fast</b>: Detiene al primer error o valida todos los parámetros</li>
 * </ul>
 * 
 * @author bruno.gil
 * @see ValidateParameters
 * @since 1.0
 */
@Aspect
@Component
@Slf4j
@Order(1) // Ejecutar primero las validaciones
public class ValidationAspect {

    @Autowired
    private AopProperties aopProperties;

    // Patrón regex para validación de email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    /**
     * Valida parámetros antes de la ejecución del método.
     * 
     * @param joinPoint información del punto de unión
     * @param validateParameters anotación con configuración de validación
     */
    @Before("@annotation(validateParameters)")
    public void validateMethodParameters(JoinPoint joinPoint, ValidateParameters validateParameters) {
        if (!aopProperties.getValidation().isEnabled()) {
            return;
        }

        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        // Validar parámetros null
        if (validateParameters.notNull()) {
            validateNotNull(args, className, methodName, validateParameters);
        }
        
        // Validar parámetros vacíos
        if (validateParameters.notEmpty()) {
            validateNotEmpty(args, className, methodName, validateParameters);
        }
        
        // Validar formato de email
        if (validateParameters.validateEmail() && aopProperties.getValidation().isValidateEmailFormat()) {
            validateEmailFormat(args, className, methodName, validateParameters);
        }

        // Log de validación exitosa
        StringBuilder validationLog = new StringBuilder();
        validationLog.append("\n[USER-VALIDATION-SUCCESS] ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        validationLog.append("\n|- Class: ").append(className);
        validationLog.append("\n|- Method: ").append(methodName);
        validationLog.append("\n|- Param Count: ").append(args.length);
        validationLog.append("\n|- Validation: SUCCESS");
        validationLog.append("\n|_ All parameters validated successfully");
        
        log.info(validationLog.toString());
    }

    /**
     * Valida que los parámetros no sean nulos.
     * 
     * @param args parámetros del método
     * @param className nombre de la clase
     * @param methodName nombre del método
     * @param validateParameters configuración de validación
     */
    private void validateNotNull(Object[] args, String className, String methodName, 
                                ValidateParameters validateParameters) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                String errorMessage = String.format(
                    "User service parameter validation failed in %s.%s(): Parameter at index %d is null. %s",
                    className, methodName, i, validateParameters.message()
                );
                log.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }

    /**
     * Valida que strings y colecciones no estén vacías.
     * 
     * @param args parámetros del método
     * @param className nombre de la clase
     * @param methodName nombre del método
     * @param validateParameters configuración de validación
     */
    private void validateNotEmpty(Object[] args, String className, String methodName, 
                                 ValidateParameters validateParameters) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) continue; // Ya validado en notNull si está habilitado
            
            boolean isEmpty = false;
            
            if (arg instanceof String && ((String) arg).trim().isEmpty()) {
                isEmpty = true;
            } else if (arg instanceof Collection && ((Collection<?>) arg).isEmpty()) {
                isEmpty = true;
            } else if (arg.getClass().isArray() && java.lang.reflect.Array.getLength(arg) == 0) {
                isEmpty = true;
            }
            
            if (isEmpty) {
                String errorMessage = String.format(
                    "User service parameter validation failed in %s.%s(): Parameter at index %d is empty. %s",
                    className, methodName, i, validateParameters.message()
                );
                log.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }

    /**
     * Valida formato de emails en parámetros de tipo String.
     * 
     * @param args parámetros del método
     * @param className nombre de la clase
     * @param methodName nombre del método
     * @param validateParameters configuración de validación
     */
    private void validateEmailFormat(Object[] args, String className, String methodName, 
                                    ValidateParameters validateParameters) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            
            // Solo validar strings que parezcan emails (contienen @)
            if (arg instanceof String) {
                String stringArg = (String) arg;
                if (stringArg.contains("@") && !EMAIL_PATTERN.matcher(stringArg).matches()) {
                    String errorMessage = String.format(
                        "User service parameter validation failed in %s.%s(): Parameter at index %d has invalid email format. %s",
                        className, methodName, i, validateParameters.message()
                    );
                    log.error(errorMessage);
                    throw new IllegalArgumentException(errorMessage);
                }
            }
        }
    }
}
