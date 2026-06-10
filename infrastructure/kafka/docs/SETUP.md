# 🔧 Kafka Infrastructure - Detailed Setup Guide

## 📋 Description

This guide provides detailed instructions for configuring and customizing the Kafka infrastructure for the FP microservices ecosystem.

## 🏗️ Full Architecture

```text
┌─────────────────────────────────────────────────────────────────┐
│                        FP Microservices                         │
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

## 🔧 Docker Compose Configuration

### Zookeeper Service

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

### Kafka Service

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

### Kafka UI Service

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

## 📊 Topic Configuration

### Automatic Topics

The following topics are automatically created on startup:

```yaml
Topics:
  product-events:
    partitions: 3
    replication-factor: 1
    description: "Product service events (CREATED, UPDATED, DELETED)"

  user-events:
    partitions: 3
    replication-factor: 1
    description: "User service events (CREATED, UPDATED, DELETED)"

  order-events:
    partitions: 3
    replication-factor: 1
    description: "Order service events (CREATED, UPDATED, CANCELLED)"

  product:
    partitions: 3
    replication-factor: 1
    description: "Simple topic for product operations"

  user:
    partitions: 3
    replication-factor: 1
    description: "Simple topic for user operations"

  order:
    partitions: 3
    replication-factor: 1
    description: "Simple topic for order operations"
```

### Create Custom Topics

```powershell
# Create a specific topic
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic my-custom-topic --partitions 3 --replication-factor 1

# Create a topic with custom configuration
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic my-topic --partitions 6 --replication-factor 1 --config retention.ms=86400000
```

## 🔌 Spring Boot Microservice Configuration

### Base Configuration (application.properties)

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

### Producer Configuration

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

### Consumer Configuration

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

## 📝 Usage Examples

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

## 🌐 Environment Variables

Create a `.env` file for custom configurations:

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

## 🔒 Security Configuration (Optional)

For environments that require authentication:

```yaml
# Add to kafka service in docker-compose.yml
environment:
  KAFKA_SECURITY_INTER_BROKER_PROTOCOL: SASL_PLAINTEXT
  KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL: PLAIN
  KAFKA_SASL_ENABLED_MECHANISMS: PLAIN
  KAFKA_OPTS: "-Djava.security.auth.login.config=/etc/kafka/kafka_server_jaas.conf"
```

## 📊 Monitoring and Metrics

### JMX Metrics

Kafka exposes JMX metrics on port 9101. You can connect tools like:

- JConsole
- Prometheus + JMX Exporter
- Grafana

### Health Checks

```powershell
# Verify Kafka is responding
docker exec fp-kafka kafka-broker-api-versions --bootstrap-server localhost:9092

# Verify topics
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Verify consumer groups
docker exec fp-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

## 📁 Recommended Data Structure

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

## 🚀 Next Steps

1. Customize the configuration according to your needs
2. Implement producers in your microservices
3. Implement the corresponding consumers
4. Configure monitoring and alerts
5. Consider production-level settings

## 🔗 References

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka Documentation](https://spring.io/projects/spring-kafka)
- [Confluent Platform](https://docs.confluent.io/)
