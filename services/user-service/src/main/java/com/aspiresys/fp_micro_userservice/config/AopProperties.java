package com.aspiresys.fp_micro_userservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Propiedades de configuración para AOP en el servicio de usuarios.
 * 
 * <p>Esta clase permite configurar externamente el comportamiento de los aspectos AOP
 * a través de propiedades en application.properties o variables de entorno.</p>
 * 
 * <h3>Configuraciones disponibles:</h3>
 * <ul>
 *   <li><b>Auditoría</b>: Habilitar/deshabilitar y configurar logging de auditoría</li>
 *   <li><b>Rendimiento</b>: Configurar umbrales y logging de métricas de performance</li>
 *   <li><b>Validación</b>: Configurar comportamiento de validación de parámetros</li>
 * </ul>
 * 
 * <h3>Ejemplo de configuración en application.properties:</h3>
 * <pre>
 * # Configuración de auditoría
 * app.aop.audit.enabled=true
 * app.aop.audit.include-parameters=true
 * app.aop.audit.include-return-value=false
 * app.aop.audit.log-level=INFO
 * 
 * # Configuración de rendimiento
 * app.aop.performance.enabled=true
 * app.aop.performance.threshold-ms=100
 * app.aop.performance.log-slow-operations=true
 * app.aop.performance.log-level=INFO
 * 
 * # Configuración de validación
 * app.aop.validation.enabled=true
 * app.aop.validation.fail-fast=true
 * app.aop.validation.log-level=WARN
 * </pre>
 * 
 * @author bruno.gil
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @since 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.aop")
public class AopProperties {

    /**
     * Configuración de auditoría
     */
    private Audit audit = new Audit();

    /**
     * Configuración de rendimiento
     */
    private Performance performance = new Performance();

    /**
     * Configuración de validación
     */
    private Validation validation = new Validation();

    /**
     * Configuración específica para auditoría de operaciones.
     */
    @Data
    public static class Audit {
        /**
         * Habilitar/deshabilitar auditoría
         */
        private boolean enabled = true;

        /**
         * Incluir parámetros en los logs de auditoría
         */
        private boolean includeParameters = true;

        /**
         * Incluir valor de retorno en los logs de auditoría
         */
        private boolean includeReturnValue = false;

        /**
         * Nivel de logging para auditoría
         */
        private String logLevel = "INFO";

        /**
         * Máximo número de caracteres para parámetros en logs
         */
        private int maxParameterLength = 200;
    }

    /**
     * Configuración específica para medición de rendimiento.
     */
    @Data
    public static class Performance {
        /**
         * Habilitar/deshabilitar medición de rendimiento
         */
        private boolean enabled = true;

        /**
         * Umbral en milisegundos para considerar una operación lenta
         */
        private long thresholdMs = 100;

        /**
         * Registrar operaciones que superen el umbral
         */
        private boolean logSlowOperations = true;

        /**
         * Nivel de logging para métricas de rendimiento
         */
        private String logLevel = "INFO";

        /**
         * Incluir detalles adicionales en logs de rendimiento
         */
        private boolean includeDetails = false;
    }

    /**
     * Configuración específica para validación de parámetros.
     */
    @Data
    public static class Validation {
        /**
         * Habilitar/deshabilitar validación de parámetros
         */
        private boolean enabled = true;

        /**
         * Fallar rápidamente al primer error de validación
         */
        private boolean failFast = true;

        /**
         * Nivel de logging para errores de validación
         */
        private String logLevel = "WARN";

        /**
         * Validar automáticamente formato de emails
         */
        private boolean validateEmailFormat = true;
    }
}
