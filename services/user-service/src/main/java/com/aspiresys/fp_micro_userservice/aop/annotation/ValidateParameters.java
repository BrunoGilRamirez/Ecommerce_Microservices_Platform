package com.aspiresys.fp_micro_userservice.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación personalizada para validación automática de parámetros en métodos
 * del servicio de usuarios.
 * 
 * <p>Esta anotación permite aplicar validaciones estándar a los parámetros de entrada
 * de métodos antes de su ejecución, evitando la necesidad de código repetitivo
 * de validación manual.</p>
 * 
 * <h3>Tipos de validación soportadas:</h3>
 * <ul>
 *   <li><b>Null Check</b>: Validación de parámetros no nulos</li>
 *   <li><b>Empty Check</b>: Validación de colecciones/strings no vacíos</li>
 *   <li><b>Email Format</b>: Validación de formato de email</li>
 *   <li><b>Custom Message</b>: Mensajes de error personalizados</li>
 * </ul>
 * 
 * <h3>Comportamiento:</h3>
 * <ul>
 *   <li>Las validaciones se ejecutan <b>antes</b> del método</li>
 *   <li>Se lanza <b>IllegalArgumentException</b> si la validación falla</li>
 *   <li>El mensaje de error incluye contexto detallado</li>
 *   <li>Los parámetros se validan en orden secuencial</li>
 * </ul>
 * 
 * <h3>Ejemplo de uso:</h3>
 * <pre>
 * {@code
 * @ValidateParameters(notNull = true, notEmpty = true, 
 *                    message = "User data cannot be null or empty")
 * public User saveUser(User user) {
 *     return userRepository.save(user);
 * }
 * 
 * @ValidateParameters(notNull = true, validateEmail = true,
 *                    message = "Email cannot be null and must be valid")
 * public User getUserByEmail(String email) {
 *     return userRepository.findByEmail(email);
 * }
 * }
 * </pre>
 * 
 * <h3>Excepciones generadas:</h3>
 * <pre>
 * IllegalArgumentException: User service parameter validation failed in 
 * UserServiceImpl.saveUser(): Parameter at index 0 is null. User data cannot be null
 * </pre>
 * 
 * @author bruno.gil
 * @see com.aspiresys.fp_micro_userservice.aop.aspect.ValidationAspect
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateParameters {
    
    /**
     * Indica si los parámetros deben ser no nulos.
     * 
     * @return true para validar que ningún parámetro sea null
     */
    boolean notNull() default false;
    
    /**
     * Indica si los parámetros de tipo colección/string deben ser no vacíos.
     * 
     * @return true para validar que colecciones y strings no estén vacíos
     */
    boolean notEmpty() default false;
    
    /**
     * Indica si los parámetros de tipo String que representen emails deben 
     * tener formato válido.
     * 
     * @return true para validar formato de email
     */
    boolean validateEmail() default false;
    
    /**
     * Mensaje personalizado para mostrar cuando la validación falla.
     * 
     * @return mensaje de error personalizado
     */
    String message() default "Parameter validation failed";
    
    /**
     * Indica si se debe fallar rápidamente al primer error de validación
     * o continuar validando todos los parámetros.
     * 
     * @return true para fallar al primer error, false para validar todos
     */
    boolean failFast() default true;
}
