package com.aspiresys.fp_micro_userservice.kafka.config;

import com.aspiresys.fp_micro_userservice.kafka.dto.UserMessage;
import lombok.extern.java.Log;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for User Service producer.
 * Configures the producer to send user messages to Kafka topics.
 * 
 * @author bruno.gil
 */
@Configuration
@Log
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Producer factory configuration for UserMessage production.
     * 
     * @return ProducerFactory for UserMessage
     */
    @Bean
    public ProducerFactory<String, UserMessage> userProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Producer resilience configurations
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        // JSON serializer configurations
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        
        log.info("KAFKA PRODUCER CONFIG: Bootstrap servers: " + bootstrapServers);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka template for UserMessage production.
     * 
     * @return KafkaTemplate for UserMessage
     */
    @Bean
    public KafkaTemplate<String, UserMessage> userKafkaTemplate() {
        return new KafkaTemplate<>(userProducerFactory());
    }
}
