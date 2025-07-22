# 🔍 Troubleshooting - Kafka Infrastructure

## 🚨 Common Issues and Solutions

### 1. Kafka Fails to Start Properly

#### Symptoms - Kafka Fails to Start

- Containers stop unexpectedly
- Error: "Connection to node -1 could not be established"
- Logs show connection errors with Zookeeper

#### Solutions

```powershell
# Check that Docker is running
docker version

# Clean up existing containers
.\scripts\kafka-manager.ps1 stop
docker system prune -f

# Verify available ports
netstat -an | findstr ":9092"
netstat -an | findstr ":2181"
netstat -an | findstr ":9090"

# Restart from scratch
.\scripts\kafka-manager.ps1 start
```

#### Verification

```powershell
# Check container status
docker ps --filter "name=fp-"

# Check logs
docker logs fp-zookeeper
docker logs fp-kafka
docker logs fp-kafka-ui
```

### 2. Microservices Cannot Connect to Kafka

#### Symptoms - Connection Failures

- Error: "Failed to send ProducerRecord"
- "Connection to localhost:9092 failed"
- Connection timeouts

#### Connectivity Solutions

```powershell
# Check Kafka status
.\scripts\kafka-manager.ps1 status

# Test connectivity from host
telnet localhost 9092

# Inspect network settings
docker network ls
docker network inspect fp_kafka-network
```

#### Microservice Configuration

```properties
# In application.properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.group-id=my-service-group
```

### 3. Topics Are Not Created Automatically

#### Symptoms - Missing Topics

- Error: "Topic 'my-topic' not found"
- Producers fail to send messages
- Topic list is empty

#### Topic Creation Solutions

```powershell
# Create topics manually
.\scripts\kafka-manager.ps1 create-topics

# Check auto-creation settings
docker exec fp-kafka kafka-configs --bootstrap-server localhost:9092 --entity-type brokers --entity-name 1 --describe

# Create a specific topic
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic my-topic --partitions 3 --replication-factor 1
```

### 4. Kafka UI Is Not Accessible

#### Symptoms - UI Unreachable

- 404 Error at <http://localhost:9090>
- "This site can't be reached"
- Blank page

#### UI Solutions

```powershell
# Check that the container is running
docker ps --filter "name=fp-kafka-ui"

# Check UI logs
docker logs fp-kafka-ui

# Verify port usage
netstat -an | findstr ":9090"

# Restart only Kafka UI
docker restart fp-kafka-ui
```

### 5. Messages Are Not Consumed

#### Symptoms - No Consumers

- Producers send but consumers do not receive
- Consumer group lag increases
- Pending messages

#### Diagnosis

```powershell
# List consumer groups
docker exec fp-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list

# Describe a specific group
docker exec fp-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group my-service-group

# Read messages from topic
docker exec fp-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic product-events --from-beginning
```

#### Consumer Solutions

```java
// Verify consumer configuration
@KafkaListener(topics = "product-events", groupId = "my-unique-group")
public void handleMessage(String message) {
    log.info("Received: {}", message);
}
```

### 6. Performance Issues

#### Symptoms - Low Performance

- High message latency
- Low throughput
- Frequent timeouts

#### Optimizations

```yaml
# In docker-compose.yml, add performance settings
kafka:
  environment:
    KAFKA_NUM_NETWORK_THREADS: 8
    KAFKA_NUM_IO_THREADS: 8
    KAFKA_SOCKET_SEND_BUFFER_BYTES: 102400
    KAFKA_SOCKET_RECEIVE_BUFFER_BYTES: 102400
    KAFKA_SOCKET_REQUEST_MAX_BYTES: 104857600
```

```java
// Optimized producer configuration
@Bean
public ProducerFactory<String, Object> producerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
    props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
    props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
    return new DefaultKafkaProducerFactory<>(props);
}
```

### 7. Serialization/Deserialization Errors

#### Symptoms - Deserialization Failures

- "Error deserializing value"
- "JsonParseException"
- Corrupted messages

#### Serialization/Deserialization Solutions

```java
// Robust deserialization setup
@Bean
public ConsumerFactory<String, Object> consumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.yourpackage.YourClass");
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
    return new DefaultKafkaConsumerFactory<>(props);
}
```

### 8. Disk Space Issues

#### Symptoms - No Disk Space

- "No space left on device"
- Kafka stops unexpectedly
- Degraded performance

#### Disk Space Solutions

```powershell
# Check disk usage
docker system df

# Clean Kafka data (WARNING: deletes all messages)
.\scripts\kafka-manager.ps1 stop
docker volume rm fp_kafka-data
.\scripts\kafka-manager.ps1 start

# Configure topic retention
docker exec fp-kafka kafka-configs --bootstrap-server localhost:9092 --entity-type topics --entity-name product-events --alter --add-config retention.ms=86400000
```

## 🛠️ Debugging Commands

### General Check

```powershell
# Full system status
.\scripts\kafka-manager.ps1 status

# Real-time logs
.\scripts\kafka-manager.ps1 logs

# Connectivity check
docker exec fp-kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Topic Inspection

```powershell
# List all topics
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Describe a topic
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic product-events

# Show topic config
docker exec fp-kafka kafka-configs --bootstrap-server localhost:9092 --entity-type topics --entity-name product-events --describe
```

### Consumer Group Monitoring

```powershell
# List consumer groups
docker exec fp-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list

# Describe a group
docker exec fp-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group my-service-group

# Reset offsets (WARNING!)
docker exec fp-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --group my-service-group --reset-offsets --to-earliest --topic product-events --execute
```

### Manual Testing

```powershell
# Produce test messages
docker exec -it fp-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic product-events

# Consume messages
docker exec fp-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic product-events --from-beginning

# Consume with a consumer group
docker exec fp-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic product-events --group test-group
```

## 📊 Proactive Monitoring

### Automated Health Checks

```powershell
# Kafka health check script
function Test-KafkaHealth {
    Write-Host "Checking Kafka health..." -ForegroundColor Yellow

    # Check containers
    $containers = docker ps --filter "name=fp-" --format "{{.Names}}" 2>$null
    if ($containers.Count -lt 3) {
        Write-Host "Not all containers are running" -ForegroundColor Red
        return $false
    }

    # Check connectivity
    docker exec fp-kafka kafka-broker-api-versions --bootstrap-server localhost:9092 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Kafka is not responding" -ForegroundColor Red
        return $false
    }

    Write-Host "Kafka is healthy" -ForegroundColor Green
    return $true
}
```

### Log Alerts

```powershell
# Search for errors in logs
docker logs fp-kafka 2>&1 | Select-String -Pattern "ERROR|WARN|Exception"
docker logs fp-zookeeper 2>&1 | Select-String -Pattern "ERROR|WARN|Exception"
docker logs fp-kafka-ui 2>&1 | Select-String -Pattern "ERROR|WARN|Exception"
```

## 🆘 Escalation Procedures

### Information to Collect

```powershell
# Generate diagnostic report
$reportPath = "kafka-diagnostic-$(Get-Date -Format 'yyyyMMdd-HHmmss').txt"

@"
=== KAFKA DIAGNOSTIC REPORT ===
Date: $(Get-Date)

=== CONTAINER STATUS ===
$(docker ps --filter "name=fp-")

=== KAFKA LOGS (last 50 lines) ===
$(docker logs fp-kafka --tail 50 2>&1)

=== ZOOKEEPER LOGS (last 50 lines) ===
$(docker logs fp-zookeeper --tail 50 2>&1)

=== TOPICS ===
$(docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --list 2>&1)

=== CONSUMER GROUPS ===
$(docker exec fp-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list 2>&1)

=== DOCKER SYSTEM INFO ===
$(docker system df)
$(docker version)
"@ | Out-File -FilePath $reportPath

Write-Host "Diagnostic report created: $reportPath" -ForegroundColor Green
```

### Support Contact

If issues persist after following this guide:

1. Generate the diagnostic report using the above script
2. Review detailed microservice logs
3. Check network and firewall settings
4. Open an issue in the repository with the full report

## 🔄 Recovery Procedures

### Full Recovery

```powershell
# Full stop and cleanup
.\scripts\kafka-manager.ps1 stop
docker system prune -f
docker volume prune -f

# Full restart
.\scripts\kafka-manager.ps1 start

# Verification
.\scripts\kafka-manager.ps1 status
.\scripts\kafka-manager.ps1 topics
```

### Data Recovery

```powershell
# Backup important topics before issues
docker exec fp-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic product-events --from-beginning > backup-product-events.json

# Restore messages after recovery
docker exec -i fp-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic product-events < backup-product-events.json
```

---

**Remember**: Most issues are resolved with a full stack restart. Always check logs to identify the root cause.
