package com.aspiresys.fp_micro_userservice.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación personalizada para habilitar auditoría automática en métodos del servicio de usuarios.
 * 
 * <p>Esta anotación se utiliza para marcar métodos que requieren auditoría automática,
 * registrando información como timestamp, usuario, operación realizada, parámetros
 * de entrada y resultados de la ejecución.</p>
 * 
 * <h3>Características de la auditoría:</h3>
 * <ul>
 *   <li><b>Timestamp</b>: Registro automático del momento de ejecución</li>
 *   <li><b>Operación</b>: Nombre descriptivo de la operación realizada</li>
 *   <li><b>Entidad</b>: Tipo de entidad involucrada (User)</li>
 *   <li><b>Parámetros</b>: Registro opcional de parámetros de entrada</li>
 *   <li><b>Resultado</b>: Registro opcional del valor de retorno</li>
 *   <li><b>Estado</b>: SUCCESS/ERROR según el resultado de la ejecución</li>
 * </ul>
 * 
 * <h3>Ejemplo de uso:</h3>
 * <pre>
 * {@code
 * @Auditable(operation = "CREATE_USER", entityType = "User", 
 *           logParameters = true, logResult = false)
 * public User saveUser(User user) {
 *     return userRepository.save(user);
 * }
 * }
 * </pre>
 * 
 * <h3>Log generado:</h3>
 * <pre>
 * [INFO] USER_AUDIT|timestamp=2024-01-15T10:30:45|operation=CREATE_USER|
 *        entity=User|class=UserServiceImpl|method=saveUser|status=SUCCESS|
 *        parameters=[User{email='user@example.com'}]
 * </pre>
 * 
 * @author bruno.gil
 * @see com.aspiresys.fp_micro_userservice.aop.aspect.AuditAspect
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    
    /**
     * Nombre descriptivo de la operación que se está auditando.
     * 
     * @return el nombre de la operación (ej: "CREATE_USER", "DELETE_USER", "UPDATE_USER")
     */
    String operation() default "";
    
    /**
     * Tipo de entidad involucrada en la operación.
     * 
     * @return el tipo de entidad (ej: "User", "UserProfile")
     */
    String entityType() default "User";
    
    /**
     * Indica si se deben registrar los parámetros de entrada del método.
     * Los parámetros sensibles (passwords, tokens) se enmascaran automáticamente.
     * 
     * @return true para registrar parámetros, false para omitirlos
     */
    boolean logParameters() default false;
    
    /**
     * Indica si se debe registrar el valor de retorno del método.
     * Los datos sensibles se filtran automáticamente.
     * 
     * @return true para registrar el resultado, false para omitirlo
     */
    boolean logResult() default false;
    
    /**
     * Nivel de detalle de la auditoría.
     * 
     * @return nivel de auditoría: "BASIC", "DETAILED", "FULL"
     */
    String level() default "BASIC";
}
