# 🔧 Kafka Infrastructure - Guía de Configuración Detallada

## 📋 Descripción

Esta guía proporciona instrucciones detalladas para configurar y personalizar la infraestructura de Kafka para el ecosistema de microservicios FP.

## 🏗️ Arquitectura Completa

```text
┌─────────────────────────────────────────────────────────────────┐
│                        Microservicios FP                        │
├─────────────────┬─────────────────┬─────────────────┬───────────┤
│   Auth Service  │  User Service   │ Product Service │ Order...  │
│    (port 8081)  │   (port 9002)   │   (port 9001)   │(port 9003)│
└─────────────────┴─────────────────┴─────────────────┴───────────┘
                                │
                                │ Events via Kafka
                                │
┌─────────────────────────────────────────────────────────────────┐
│                      Kafka Infrastructure                       │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │ Kafka UI    │  │   Kafka     │  │      Zookeeper          │  │
│  │ (port 9090) │  │(port 9092)  │  │     (port 2181)         │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## 🔧 Configuración del Docker Compose

### Servicio Zookeeper

```yaml
zookeeper:
  image: confluentinc/cp-zookeeper:7.4.0
  hostname: zookeeper
  container_name: fp-zookeeper
  ports:
    - "2181:2181"
  environment:
    ZOOKEEPER_CLIENT_PORT: 2181
    ZOOKEEPER_TICK_TIME: 2000
  networks:
    - kafka-network
```

### Servicio Kafka

```yaml
kafka:
  image: confluentinc/cp-kafka:7.4.0
  hostname: kafka
  container_name: fp-kafka
  depends_on:
    - zookeeper
  ports:
    - "9092:9092"
    - "9101:9101"
  environment:
    KAFKA_BROKER_ID: 1
    KAFKA_ZOOKEEPER_CONNECT: "zookeeper:2181"
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
    KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:29092,PLAINTEXT_HOST://0.0.0.0:9092
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
    KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
    KAFKA_JMX_PORT: 9101
    KAFKA_JMX_HOSTNAME: localhost
  volumes:
    - kafka-data:/var/lib/kafka/data
  networks:
    - kafka-network
```

### Servicio Kafka UI

```yaml
kafka-ui:
  image: provectuslabs/kafka-ui:latest
  container_name: fp-kafka-ui
  depends_on:
    - kafka
  ports:
    - "9090:8080"
  environment:
    KAFKA_CLUSTERS_0_NAME: fp-cluster
    KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092
    KAFKA_CLUSTERS_0_ZOOKEEPER: zookeeper:2181
  networks:
    - kafka-network
```

## 📊 Configuración de Topics

### Topics Automáticos

Los siguientes topics se crean automáticamente al iniciar:

```yaml
Topics:
  product-events:
    partitions: 3
    replication-factor: 1
    description: "Eventos del servicio de productos (CREATED, UPDATED, DELETED)"

  user-events:
    partitions: 3
    replication-factor: 1
    description: "Eventos del servicio de usuarios (CREATED, UPDATED, DELETED)"

  order-events:
    partitions: 3
    replication-factor: 1
    description: "Eventos del servicio de pedidos (CREATED, UPDATED, CANCELLED)"

  product:
    partitions: 3
    replication-factor: 1
    description: "Topic simple para operaciones de productos"

  user:
    partitions: 3
    replication-factor: 1
    description: "Topic simple para operaciones de usuarios"

  order:
    partitions: 3
    replication-factor: 1
    description: "Topic simple para operaciones de pedidos"
```

### Crear Topics Personalizados

```powershell
# Crear un topic específico
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic my-custom-topic --partitions 3 --replication-factor 1

# Crear topic con configuración específica
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic my-topic --partitions 6 --replication-factor 1 --config retention.ms=86400000
```

## 🔌 Configuración de Microservicios Spring Boot

### Configuración Base (application.properties)

```properties
# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

### Configuración Producer

```java
@Configuration
@EnableKafka
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

### Configuración Consumer

```java
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}
```

## 📝 Ejemplos de Uso

### Producer Example

```java
@Service
public class ProductEventProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendProductEvent(String eventType, Product product) {
        ProductEvent event = new ProductEvent();
        event.setEventType(eventType);
        event.setProductId(product.getId());
        event.setProductName(product.getName());
        event.setTimestamp(Instant.now());

        kafkaTemplate.send("product-events", product.getId().toString(), event);
        log.info("KAFKA: Sent product event: {} for product: {}", eventType, product.getId());
    }
}
```

### Consumer Example

```java
@Service
public class OrderEventConsumer {

    @KafkaListener(topics = "product-events", groupId = "order-service-group")
    public void handleProductEvent(ConsumerRecord<String, ProductEvent> record, Acknowledgment acknowledgment) {
        try {
            ProductEvent event = record.value();
            log.info("KAFKA: Received product event: {} for product: {}", event.getEventType(), event.getProductId());

            // Process the event
            processProductEvent(event);

            // Acknowledge the message
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("KAFKA: Error processing product event: {}", e.getMessage());
            // Handle error - could retry or send to DLQ
        }
    }

    private void processProductEvent(ProductEvent event) {
        // Business logic here
        switch (event.getEventType()) {
            case "CREATED":
                handleProductCreated(event);
                break;
            case "UPDATED":
                handleProductUpdated(event);
                break;
            case "DELETED":
                handleProductDeleted(event);
                break;
        }
    }
}
```

## 🌐 Variables de Entorno

Crear archivo `.env` para configuraciones personalizadas:

```env
# Kafka Configuration
KAFKA_BROKER_ID=1
KAFKA_AUTO_CREATE_TOPICS=true
KAFKA_DELETE_TOPIC_ENABLE=true
KAFKA_LOG_RETENTION_HOURS=168

# Zookeeper Configuration
ZOOKEEPER_CLIENT_PORT=2181
ZOOKEEPER_TICK_TIME=2000

# Kafka UI Configuration
KAFKA_UI_PORT=9090
KAFKA_CLUSTERS_0_NAME=fp-cluster
```

## 🔒 Configuración de Seguridad (Opcional)

Para entornos que requieren autenticación:

```yaml
# Agregar al servicio kafka en docker-compose.yml
environment:
  KAFKA_SECURITY_INTER_BROKER_PROTOCOL: SASL_PLAINTEXT
  KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL: PLAIN
  KAFKA_SASL_ENABLED_MECHANISMS: PLAIN
  KAFKA_OPTS: "-Djava.security.auth.login.config=/etc/kafka/kafka_server_jaas.conf"
```

## 📊 Monitoreo y Métricas

### JMX Metrics

Kafka expone métricas JMX en el puerto 9101. Puedes conectar herramientas como:

- JConsole
- Prometheus + JMX Exporter
- Grafana

### Health Checks

```powershell
# Verificar que Kafka responde
docker exec fp-kafka kafka-broker-api-versions --bootstrap-server localhost:9092

# Verificar topics
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Verificar consumer groups
docker exec fp-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

## 📁 Estructura de Datos Recomendada

### Event Structure

```json
{
  "eventId": "uuid",
  "eventType": "CREATED|UPDATED|DELETED",
  "entityId": "string",
  "entityType": "PRODUCT|USER|ORDER",
  "timestamp": "2024-01-01T10:00:00Z",
  "data": {
    // Entity specific data
  },
  "metadata": {
    "source": "service-name",
    "version": "1.0",
    "correlationId": "uuid"
  }
}
```

## 🚀 Próximos Pasos

1. Personaliza la configuración según tus necesidades
2. Implementa los producers en tus microservicios
3. Implementa los consumers correspondientes
4. Configura monitoreo y alertas
5. Considera configuraciones de producción

## 🔗 Referencias

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka Documentation](https://spring.io/projects/spring-kafka)
- [Confluent Platform](https://docs.confluent.io/)
