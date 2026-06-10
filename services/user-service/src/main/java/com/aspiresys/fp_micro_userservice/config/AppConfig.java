package com.aspiresys.fp_micro_userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración general de la aplicación User Service.
 * 
 * Esta clase define beans adicionales que pueden ser útiles
 * para la comunicación con otros servicios o funcionalidades generales.
 */
@Configuration
public class AppConfig {

    /**
     * Bean para RestTemplate, útil para comunicación con otros servicios.
     * 
     * @return una instancia configurada de RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
