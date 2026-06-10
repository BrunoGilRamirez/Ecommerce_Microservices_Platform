package com.aspiresys.fp_micro_userservice.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación personalizada para medir automáticamente el tiempo de ejecución de métodos
 * en el servicio de usuarios.
 * 
 * <p>Esta anotación permite monitorear el rendimiento de métodos críticos,
 * registrando el tiempo de ejecución y generando alertas cuando se superen
 * los umbrales configurados.</p>
 * 
 * <h3>Funcionalidades de medición:</h3>
 * <ul>
 *   <li><b>Tiempo de ejecución</b>: Medición precisa en milisegundos</li>
 *   <li><b>Umbral de advertencia</b>: Alertas para operaciones lentas</li>
 *   <li><b>Métricas detalladas</b>: Información adicional de contexto</li>
 *   <li><b>Estado de ejecución</b>: SUCCESS/ERROR</li>
 *   <li><b>Logging estructurado</b>: Para integración con herramientas de monitoreo</li>
 * </ul>
 * 
 * <h3>Ejemplo de uso:</h3>
 * <pre>
 * {@code
 * @ExecutionTime(operation = "Save User", warningThreshold = 500, detailed = true)
 * public User saveUser(User user) {
 *     return userRepository.save(user);
 * }
 * }
 * </pre>
 * 
 * <h3>Log generado:</h3>
 * <pre>
 * [INFO] USER_PERFORMANCE|operation=Save User|class=UserServiceImpl|method=saveUser|
 *        execution_time_ms=245|success=true|timestamp=2024-01-15T10:30:45
 * [WARN] USER_PERFORMANCE|operation=Save User|execution_time_ms=750|threshold_ms=500|
 *        message=Operation exceeded warning threshold
 * </pre>
 * 
 * @author bruno.gil
 * @see com.aspiresys.fp_micro_userservice.aop.aspect.ExecutionTimeAspect
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecutionTime {
    
    /**
     * Nombre descriptivo de la operación que se está midiendo.
     * 
     * @return el nombre de la operación (ej: "Save User", "Find User", "Delete User")
     */
    String operation() default "";
    
    /**
     * Umbral de tiempo en milisegundos para generar advertencias.
     * Operaciones que superen este umbral generarán logs de advertencia.
     * 
     * @return umbral en milisegundos (por defecto 1000ms = 1 segundo)
     */
    long warningThreshold() default 1000;
    
    /**
     * Indica si se debe registrar información detallada sobre la ejecución.
     * Incluye contexto adicional como parámetros de entrada y stack trace en errores.
     * 
     * @return true para información detallada, false para información básica
     */
    boolean detailed() default false;
    
    /**
     * Unidad de medida para el reporte de tiempo.
     * 
     * @return unidad de tiempo: "ms" (milisegundos), "s" (segundos)
     */
    String unit() default "ms";
}
