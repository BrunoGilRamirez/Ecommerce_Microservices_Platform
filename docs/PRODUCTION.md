# 🏭 Configuración para Producción - Kafka Infrastructure

## 📋 Consideraciones de Producción

Esta guía proporciona las mejores prácticas y configuraciones recomendadas para desplegar Kafka en un entorno de producción.

## ⚡ Configuraciones Críticas

### 1. Configuración de Cluster Multi-Broker

```yaml
# docker-compose-production.yml
version: "3.8"

services:
  zookeeper-1:
    image: confluentinc/cp-zookeeper:7.4.0
    hostname: zookeeper-1
    container_name: zookeeper-1
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
      ZOOKEEPER_SERVER_ID: 1
      ZOOKEEPER_SERVERS: zookeeper-1:2888:3888;zookeeper-2:2888:3888;zookeeper-3:2888:3888
    volumes:
      - zookeeper-1-data:/var/lib/zookeeper/data
      - zookeeper-1-logs:/var/lib/zookeeper/log
    networks:
      - kafka-network

  zookeeper-2:
    image: confluentinc/cp-zookeeper:7.4.0
    hostname: zookeeper-2
    container_name: zookeeper-2
    ports:
      - "2182:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
      ZOOKEEPER_SERVER_ID: 2
      ZOOKEEPER_SERVERS: zookeeper-1:2888:3888;zookeeper-2:2888:3888;zookeeper-3:2888:3888
    volumes:
      - zookeeper-2-data:/var/lib/zookeeper/data
      - zookeeper-2-logs:/var/lib/zookeeper/log
    networks:
      - kafka-network

  zookeeper-3:
    image: confluentinc/cp-zookeeper:7.4.0
    hostname: zookeeper-3
    container_name: zookeeper-3
    ports:
      - "2183:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
      ZOOKEEPER_SERVER_ID: 3
      ZOOKEEPER_SERVERS: zookeeper-1:2888:3888;zookeeper-2:2888:3888;zookeeper-3:2888:3888
    volumes:
      - zookeeper-3-data:/var/lib/zookeeper/data
      - zookeeper-3-logs:/var/lib/zookeeper/log
    networks:
      - kafka-network

  kafka-1:
    image: confluentinc/cp-kafka:7.4.0
    hostname: kafka-1
    container_name: kafka-1
    depends_on:
      - zookeeper-1
      - zookeeper-2
      - zookeeper-3
    ports:
      - "9092:9092"
      - "9101:9101"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: "zookeeper-1:2181,zookeeper-2:2181,zookeeper-3:2181"
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-1:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:29092,PLAINTEXT_HOST://0.0.0.0:9092
      KAFKA_DEFAULT_REPLICATION_FACTOR: 3
      KAFKA_MIN_INSYNC_REPLICAS: 2
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 3
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 2
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "false"
      KAFKA_JMX_PORT: 9101
      KAFKA_JMX_HOSTNAME: localhost
      # Performance optimizations
      KAFKA_NUM_NETWORK_THREADS: 8
      KAFKA_NUM_IO_THREADS: 8
      KAFKA_SOCKET_SEND_BUFFER_BYTES: 102400
      KAFKA_SOCKET_RECEIVE_BUFFER_BYTES: 102400
      KAFKA_SOCKET_REQUEST_MAX_BYTES: 104857600
      # Log retention
      KAFKA_LOG_RETENTION_HOURS: 168
      KAFKA_LOG_SEGMENT_BYTES: 1073741824
      KAFKA_LOG_RETENTION_CHECK_INTERVAL_MS: 300000
    volumes:
      - kafka-1-data:/var/lib/kafka/data
    networks:
      - kafka-network

  kafka-2:
    image: confluentinc/cp-kafka:7.4.0
    hostname: kafka-2
    container_name: kafka-2
    depends_on:
      - zookeeper-1
      - zookeeper-2
      - zookeeper-3
    ports:
      - "9093:9093"
      - "9102:9102"
    environment:
      KAFKA_BROKER_ID: 2
      KAFKA_ZOOKEEPER_CONNECT: "zookeeper-1:2181,zookeeper-2:2181,zookeeper-3:2181"
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-2:29093,PLAINTEXT_HOST://localhost:9093
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:29093,PLAINTEXT_HOST://0.0.0.0:9093
      KAFKA_DEFAULT_REPLICATION_FACTOR: 3
      KAFKA_MIN_INSYNC_REPLICAS: 2
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 3
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 2
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "false"
      KAFKA_JMX_PORT: 9102
      KAFKA_JMX_HOSTNAME: localhost
      # Performance optimizations
      KAFKA_NUM_NETWORK_THREADS: 8
      KAFKA_NUM_IO_THREADS: 8
      KAFKA_SOCKET_SEND_BUFFER_BYTES: 102400
      KAFKA_SOCKET_RECEIVE_BUFFER_BYTES: 102400
      KAFKA_SOCKET_REQUEST_MAX_BYTES: 104857600
      # Log retention
      KAFKA_LOG_RETENTION_HOURS: 168
      KAFKA_LOG_SEGMENT_BYTES: 1073741824
      KAFKA_LOG_RETENTION_CHECK_INTERVAL_MS: 300000
    volumes:
      - kafka-2-data:/var/lib/kafka/data
    networks:
      - kafka-network

  kafka-3:
    image: confluentinc/cp-kafka:7.4.0
    hostname: kafka-3
    container_name: kafka-3
    depends_on:
      - zookeeper-1
      - zookeeper-2
      - zookeeper-3
    ports:
      - "9094:9094"
      - "9103:9103"
    environment:
      KAFKA_BROKER_ID: 3
      KAFKA_ZOOKEEPER_CONNECT: "zookeeper-1:2181,zookeeper-2:2181,zookeeper-3:2181"
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-3:29094,PLAINTEXT_HOST://localhost:9094
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:29094,PLAINTEXT_HOST://0.0.0.0:9094
      KAFKA_DEFAULT_REPLICATION_FACTOR: 3
      KAFKA_MIN_INSYNC_REPLICAS: 2
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 3
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 2
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "false"
      KAFKA_JMX_PORT: 9103
      KAFKA_JMX_HOSTNAME: localhost
      # Performance optimizations
      KAFKA_NUM_NETWORK_THREADS: 8
      KAFKA_NUM_IO_THREADS: 8
      KAFKA_SOCKET_SEND_BUFFER_BYTES: 102400
      KAFKA_SOCKET_RECEIVE_BUFFER_BYTES: 102400
      KAFKA_SOCKET_REQUEST_MAX_BYTES: 104857600
      # Log retention
      KAFKA_LOG_RETENTION_HOURS: 168
      KAFKA_LOG_SEGMENT_BYTES: 1073741824
      KAFKA_LOG_RETENTION_CHECK_INTERVAL_MS: 300000
    volumes:
      - kafka-3-data:/var/lib/kafka/data
    networks:
      - kafka-network

volumes:
  zookeeper-1-data:
  zookeeper-1-logs:
  zookeeper-2-data:
  zookeeper-2-logs:
  zookeeper-3-data:
  zookeeper-3-logs:
  kafka-1-data:
  kafka-2-data:
  kafka-3-data:

networks:
  kafka-network:
    driver: bridge
```

### 2. Configuración de Seguridad con SSL/SASL

```yaml
# Configuración con autenticación SASL_SSL
kafka-1:
  environment:
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,SSL:SSL,SASL_PLAINTEXT:SASL_PLAINTEXT,SASL_SSL:SASL_SSL
    KAFKA_ADVERTISED_LISTENERS: SASL_SSL://kafka-1:9092
    KAFKA_LISTENERS: SASL_SSL://0.0.0.0:9092
    KAFKA_SECURITY_INTER_BROKER_PROTOCOL: SASL_SSL
    KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL: PLAIN
    KAFKA_SASL_ENABLED_MECHANISMS: PLAIN
    KAFKA_SSL_KEYSTORE_FILENAME: kafka.server.keystore.jks
    KAFKA_SSL_KEYSTORE_CREDENTIALS: kafka_keystore_creds
    KAFKA_SSL_KEY_CREDENTIALS: kafka_ssl_key_creds
    KAFKA_SSL_TRUSTSTORE_FILENAME: kafka.server.truststore.jks
    KAFKA_SSL_TRUSTSTORE_CREDENTIALS: kafka_truststore_creds
    KAFKA_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM: ""
    KAFKA_SSL_CLIENT_AUTH: none
    KAFKA_OPTS: "-Djava.security.auth.login.config=/etc/kafka/kafka_server_jaas.conf"
  volumes:
    - ./ssl:/etc/kafka/secrets
```

### 3. Configuración de Topics para Producción

```powershell
# Script para crear topics con configuración de producción
function Create-ProductionTopics {
    $topics = @(
        @{
            name = "product-events"
            partitions = 12
            replication = 3
            configs = @{
                "min.insync.replicas" = "2"
                "retention.ms" = "604800000"  # 7 días
                "compression.type" = "lz4"
                "cleanup.policy" = "delete"
            }
        },
        @{
            name = "user-events"
            partitions = 12
            replication = 3
            configs = @{
                "min.insync.replicas" = "2"
                "retention.ms" = "604800000"
                "compression.type" = "lz4"
                "cleanup.policy" = "delete"
            }
        },
        @{
            name = "order-events"
            partitions = 18
            replication = 3
            configs = @{
                "min.insync.replicas" = "2"
                "retention.ms" = "2592000000"  # 30 días
                "compression.type" = "lz4"
                "cleanup.policy" = "delete"
            }
        }
    )

    foreach ($topic in $topics) {
        Write-Host "Creating topic: $($topic.name)" -ForegroundColor Cyan

        # Crear topic
        docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --topic $($topic.name) --partitions $($topic.partitions) --replication-factor $($topic.replication) --if-not-exists

        # Aplicar configuraciones
        foreach ($config in $topic.configs.GetEnumerator()) {
            docker exec kafka-1 kafka-configs --bootstrap-server localhost:9092 --entity-type topics --entity-name $($topic.name) --alter --add-config "$($config.Key)=$($config.Value)"
        }
    }
}
```

## 🔧 Configuración de Microservicios para Producción

### Producer Configuration

```java
@Configuration
public class ProductionKafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Basic connection
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Durability and reliability
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // Performance optimization
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 32768);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 67108864);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");

        // Timeouts
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory());

        // Configure error handling
        template.setProducerInterceptors(List.of(new ProducerInterceptor<String, Object>() {
            @Override
            public ProducerRecord<String, Object> onSend(ProducerRecord<String, Object> record) {
                // Add tracing headers, metrics, etc.
                return record;
            }

            @Override
            public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
                if (exception != null) {
                    log.error("Failed to send message to topic: {}, partition: {}",
                             metadata.topic(), metadata.partition(), exception);
                } else {
                    log.debug("Message sent successfully to topic: {}, partition: {}, offset: {}",
                             metadata.topic(), metadata.partition(), metadata.offset());
                }
            }

            @Override
            public void close() {}

            @Override
            public void configure(Map<String, ?> configs) {}
        }));

        return template;
    }
}
```

### Consumer Configuration

```java
@Configuration
public class ProductionKafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        // Basic connection
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        // Reliability settings
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        // Performance settings
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);

        // Session settings
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // Enable manual acknowledgment
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // Configure concurrency (number of consumer threads per partition)
        factory.setConcurrency(3);

        // Error handling
        factory.setCommonErrorHandler(new DefaultErrorHandler(
            new FixedBackOff(1000L, 3)
        ));

        // Dead Letter Topic configuration
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate(),
            (record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition()));

        factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3)));

        return factory;
    }
}
```

## 📊 Monitoreo y Observabilidad

### 1. Configuración de Métricas con Prometheus

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: "kafka"
    static_configs:
      - targets: ["localhost:9101", "localhost:9102", "localhost:9103"]
    metrics_path: /metrics
    scrape_interval: 10s

  - job_name: "kafka-exporter"
    static_configs:
      - targets: ["localhost:9308"]
```

### 2. JMX Exporter Configuration

```yaml
# kafka-jmx-exporter.yml
rules:
  - pattern: "kafka.server<type=(.+), name=(.+)><>Value"
    name: kafka_server_$1_$2
  - pattern: "kafka.server<type=(.+), name=(.+), clientId=(.+)><>Value"
    name: kafka_server_$1_$2
    labels:
      clientId: "$3"
  - pattern: "kafka.server<type=(.+), name=(.+), topic=(.+)><>Value"
    name: kafka_server_$1_$2
    labels:
      topic: "$3"
  - pattern: "kafka.server<type=(.+), name=(.+), topic=(.+), partition=(.+)><>Value"
    name: kafka_server_$1_$2
    labels:
      topic: "$3"
      partition: "$4"
```

### 3. Alertas de Kafka

```yaml
# kafka-alerts.yml
groups:
  - name: kafka.rules
    rules:
      - alert: KafkaDown
        expr: up{job="kafka"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Kafka broker is down"
          description: "Kafka broker {{ $labels.instance }} has been down for more than 1 minute."

      - alert: KafkaHighProducerLatency
        expr: kafka_server_producer_request_metrics_request_latency_avg > 100
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High Kafka producer latency"
          description: "Kafka producer latency is {{ $value }}ms on {{ $labels.instance }}"

      - alert: KafkaConsumerLag
        expr: kafka_consumer_lag_sum > 1000
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High Kafka consumer lag"
          description: "Consumer group {{ $labels.group }} has a lag of {{ $value }} messages"

      - alert: KafkaUnderReplicatedPartitions
        expr: kafka_server_replica_manager_under_replicated_partitions > 0
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Kafka has under-replicated partitions"
          description: "Kafka has {{ $value }} under-replicated partitions on {{ $labels.instance }}"
```

## 🔐 Configuración de Seguridad Avanzada

### 1. Certificados SSL

```bash
# Generar certificados para Kafka
#!/bin/bash

# Create CA
openssl req -new -x509 -keyout ca-key -out ca-cert -days 365 -subj "/CN=ca1.test.confluent.io/OU=TEST/O=CONFLUENT/L=PaloAlto/S=Ca/C=US" -passout pass:confluent

# Create keystore
keytool -genkey -noprompt \
 -alias kafka-server \
 -dname "CN=kafka-server,OU=TEST,O=CONFLUENT,L=PaloAlto,S=Ca,C=US" \
 -keystore kafka.server.keystore.jks \
 -keyalg RSA \
 -storepass confluent \
 -keypass confluent

# Create certificate signing request
keytool -keystore kafka.server.keystore.jks -alias kafka-server -certreq -file cert-file -storepass confluent -keypass confluent

# Sign the certificate
openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file -out cert-signed -days 365 -CAcreateserial -passin pass:confluent

# Import CA certificate into keystore
keytool -keystore kafka.server.keystore.jks -alias CARoot -import -file ca-cert -storepass confluent -keypass confluent -noprompt

# Import signed certificate into keystore
keytool -keystore kafka.server.keystore.jks -alias kafka-server -import -file cert-signed -storepass confluent -keypass confluent -noprompt

# Create truststore
keytool -keystore kafka.server.truststore.jks -alias CARoot -import -file ca-cert -storepass confluent -keypass confluent -noprompt
```

### 2. JAAS Configuration

```bash
# kafka_server_jaas.conf
KafkaServer {
   org.apache.kafka.common.security.plain.PlainLoginModule required
   username="admin"
   password="admin-secret"
   user_admin="admin-secret"
   user_alice="alice-secret"
   user_bob="bob-secret";
};

Client {
   org.apache.kafka.common.security.plain.PlainLoginModule required
   username="admin"
   password="admin-secret";
};
```

## 🚀 Deployment y Operaciones

### 1. Health Checks

```yaml
# Añadir a docker-compose.yml
kafka-1:
  healthcheck:
    test: kafka-broker-api-versions --bootstrap-server localhost:9092
    interval: 30s
    timeout: 10s
    retries: 3
    start_period: 40s
```

### 2. Backup y Recovery

```powershell
# Script de backup
function Backup-KafkaTopics {
    param(
        [string]$BackupPath = ".\backup\$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    )

    New-Item -ItemType Directory -Path $BackupPath -Force

    $topics = docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --list

    foreach ($topic in $topics) {
        Write-Host "Backing up topic: $topic" -ForegroundColor Cyan
        docker exec kafka-1 kafka-console-consumer --bootstrap-server localhost:9092 --topic $topic --from-beginning --timeout-ms 10000 > "$BackupPath\$topic.json"
    }

    Write-Host "Backup completed in: $BackupPath" -ForegroundColor Green
}

# Script de recovery
function Restore-KafkaTopics {
    param(
        [string]$BackupPath
    )

    $backupFiles = Get-ChildItem "$BackupPath\*.json"

    foreach ($file in $backupFiles) {
        $topicName = $file.BaseName
        Write-Host "Restoring topic: $topicName" -ForegroundColor Cyan
        Get-Content $file.FullName | docker exec -i kafka-1 kafka-console-producer --bootstrap-server localhost:9092 --topic $topicName
    }

    Write-Host "Restore completed" -ForegroundColor Green
}
```

### 3. Rolling Updates

```powershell
# Script para rolling update
function Update-KafkaCluster {
    $brokers = @("kafka-1", "kafka-2", "kafka-3")

    foreach ($broker in $brokers) {
        Write-Host "Updating $broker..." -ForegroundColor Yellow

        # Graceful shutdown
        docker exec $broker kafka-server-stop

        # Wait for reassignment
        Start-Sleep -Seconds 30

        # Update container
        docker-compose stop $broker
        docker-compose pull $broker
        docker-compose up -d $broker

        # Wait for broker to be ready
        do {
            Start-Sleep -Seconds 10
            $status = docker exec $broker kafka-broker-api-versions --bootstrap-server localhost:9092 2>$null
        } while ($LASTEXITCODE -ne 0)

        Write-Host "$broker updated successfully" -ForegroundColor Green
    }
}
```

## 📈 Optimización de Performance

### 1. Configuración de OS

```bash
# Configuraciones del sistema operativo para producción
# /etc/sysctl.conf

# Network optimizations
net.core.rmem_default = 262144
net.core.rmem_max = 16777216
net.core.wmem_default = 262144
net.core.wmem_max = 16777216
net.ipv4.tcp_rmem = 4096 65536 16777216
net.ipv4.tcp_wmem = 4096 65536 16777216

# File descriptor limits
fs.file-max = 2097152

# Virtual memory settings
vm.max_map_count = 262144
vm.swappiness = 1
```

### 2. Configuración de JVM

```yaml
# Añadir a docker-compose.yml
kafka-1:
  environment:
    KAFKA_HEAP_OPTS: "-Xmx4G -Xms4G"
    KAFKA_JVM_PERFORMANCE_OPTS: >
      -server
      -XX:+UseG1GC
      -XX:MaxGCPauseMillis=20
      -XX:InitiatingHeapOccupancyPercent=35
      -XX:+ExplicitGCInvokesConcurrent
      -Djava.awt.headless=true
      -Dcom.sun.management.jmxremote=true
      -Dcom.sun.management.jmxremote.authenticate=false
      -Dcom.sun.management.jmxremote.ssl=false
```

## 🎯 Conclusiones

Esta configuración de producción proporciona:

- **Alta Disponibilidad**: Cluster de 3 brokers con replicación
- **Durabilidad**: Configuraciones de persistencia y backup
- **Seguridad**: SSL/SASL y autenticación
- **Observabilidad**: Métricas, logs y alertas
- **Performance**: Optimizaciones de red y JVM
- **Operabilidad**: Scripts de gestión y mantenimiento

Adapta estas configuraciones según las necesidades específicas de tu entorno de producción.
