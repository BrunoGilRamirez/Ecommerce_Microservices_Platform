package com.aspiresys.fp_micro_userservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Configuración principal de AOP para el servicio de usuarios.
 * 
 * <p>Esta clase configura Spring AOP y habilita el procesamiento automático
 * de aspectos con AspectJ. También habilita las propiedades de configuración
 * personalizadas para los aspectos.</p>
 * 
 * <h3>Configuraciones habilitadas:</h3>
 * <ul>
 *   <li><b>@EnableAspectJAutoProxy</b>: Habilita el proxy automático de AspectJ</li>
 *   <li><b>proxyTargetClass = true</b>: Fuerza el uso de proxies CGLIB en lugar de JDK proxies</li>
 *   <li><b>@EnableConfigurationProperties</b>: Habilita las propiedades de configuración AOP</li>
 * </ul>
 * 
 * <p><b>Nota sobre proxyTargetClass = true:</b><br>
 * Se usa CGLIB en lugar de JDK dynamic proxies porque:</p>
 * <ul>
 *   <li>Permite proxy de clases concretas (no solo interfaces)</li>
 *   <li>Mayor compatibilidad con frameworks como Spring Data JPA</li>
 *   <li>Mejor integración con inyección de dependencias por constructor</li>
 * </ul>
 * 
 * @author bruno.gil
 * @see org.springframework.context.annotation.EnableAspectJAutoProxy
 * @see AopProperties
 * @since 1.0
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties(AopProperties.class)
public class AopConfig {
    // Esta clase actúa principalmente como configuración
    // Los beans de aspectos se registran automáticamente
    // debido a la anotación @Component en cada aspecto
}
