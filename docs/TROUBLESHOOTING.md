# 🔍 Solución de Problemas - Kafka Infrastructure

## 🚨 Problemas Comunes y Soluciones

### 1. Kafka no inicia correctamente

#### Síntomas - Kafka no inicia correctamente

- Los contenedores se detienen inesperadamente
- Error: "Connection to node -1 could not be established"
- Logs muestran errores de conexión con Zookeeper

#### Soluciones 1

```powershell
# Verificar que Docker está ejecutándose
docker version

# Limpiar contenedores existentes
.\scripts\kafka-manager.ps1 stop
docker system prune -f

# Verificar puertos disponibles
netstat -an | findstr ":9092"
netstat -an | findstr ":2181"
netstat -an | findstr ":9090"

# Reiniciar desde cero
.\scripts\kafka-manager.ps1 start
```

#### Verificación

```powershell
# Comprobar estado de contenedores
docker ps --filter "name=fp-"

# Verificar logs
docker logs fp-zookeeper
docker logs fp-kafka
docker logs fp-kafka-ui
```

### 2. Microservicios no se pueden conectar a Kafka

#### Síntomas - Microservicios no se pueden conectar a Kafka

- Error: "Failed to send ProducerRecord"
- "Connection to localhost:9092 failed"
- Timeout en conexiones

#### Soluciones de Conexión

```powershell
# Verificar que Kafka está ejecutándose
.\scripts\kafka-manager.ps1 status

# Probar conectividad desde host
telnet localhost 9092

# Verificar configuración de red
docker network ls
docker network inspect fp_kafka-network
```

#### Configuración de microservicios

```properties
# Verificar en application.properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.group-id=my-service-group
```

### 3. Topics no se crean automáticamente

#### Síntomas - Topics no se crean automáticamente

- Error: "Topic 'my-topic' not found"
- Producers fallan al enviar mensajes
- Lista de topics está vacía

#### Soluciones para Topics

```powershell
# Crear topics manualmente
.\scripts\kafka-manager.ps1 create-topics

# Verificar configuración de auto-creación
docker exec fp-kafka kafka-configs --bootstrap-server localhost:9092 --entity-type brokers --entity-name 1 --describe

# Crear topic específico
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic my-topic --partitions 3 --replication-factor 1
```

### 4. Kafka UI no es accesible

#### Síntomas - Kafka UI no es accesible

- Error 404 en <http://localhost:9090>
- "This site can't be reached"
- Página en blanco

#### Soluciones

```powershell
# Verificar que el contenedor está ejecutándose
docker ps --filter "name=fp-kafka-ui"

# Verificar logs de Kafka UI
docker logs fp-kafka-ui

# Verificar puerto
netstat -an | findstr ":9090"

# Reiniciar solo Kafka UI
docker restart fp-kafka-ui
```

### 5. Mensajes no se consumen

#### Síntomas - Mensajes no se consumen

- Producers envían pero consumers no reciben
- Lag en consumer groups aumenta
- Mensajes quedan pendientes

#### Diagnóstico

```powershell
# Verificar consumer groups
docker exec fp-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list

# Ver detalles de un group
docker exec fp-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group my-service-group

# Verificar mensajes en topic
docker exec fp-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic product-events --from-beginning
```

#### Soluciones de Consumer

```java
// Verificar configuración del consumer
@KafkaListener(topics = "product-events", groupId = "my-unique-group")
public void handleMessage(String message) {
    log.info("Received: {}", message);
}
```

### 6. Problemas de Performance

#### Síntomas - Problemas de Performance

- Latencia alta en mensajes
- Throughput bajo
- Timeouts frecuentes

#### Optimizaciones

```yaml
# En docker-compose.yml, agregar configuraciones de performance
kafka:
  environment:
    KAFKA_NUM_NETWORK_THREADS: 8
    KAFKA_NUM_IO_THREADS: 8
    KAFKA_SOCKET_SEND_BUFFER_BYTES: 102400
    KAFKA_SOCKET_RECEIVE_BUFFER_BYTES: 102400
    KAFKA_SOCKET_REQUEST_MAX_BYTES: 104857600
```

```java
// Configuración optimizada del producer
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

### 7. Errores de Serialización/Deserialización

#### Síntomas - Errores de Serialización/Deserialización

- "Error deserializing value"
- "JsonParseException"
- Mensajes corruptos

#### Soluciones de Serialización/Deserialización

```java
// Configuración robusta de deserialización
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

### 8. Problemas de Espacio en Disco

#### Síntomas - Problemas de Espacio en Disco

- "No space left on device"
- Kafka se detiene inesperadamente
- Performance degradada

#### Soluciones de Espacio en Disco

```powershell
# Verificar uso de espacio
docker system df

# Limpiar datos de Kafka (¡CUIDADO! Esto borra todos los mensajes)
.\scripts\kafka-manager.ps1 stop
docker volume rm fp_kafka-data
.\scripts\kafka-manager.ps1 start

# Configurar retención de mensajes
docker exec fp-kafka kafka-configs --bootstrap-server localhost:9092 --entity-type topics --entity-name product-events --alter --add-config retention.ms=86400000
```

## 🛠️ Comandos de Debugging

### Verificación General

```powershell
# Estado completo del sistema
.\scripts\kafka-manager.ps1 status

# Logs en tiempo real
.\scripts\kafka-manager.ps1 logs

# Verificar conectividad
docker exec fp-kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Inspección de Topics

```powershell
# Listar todos los topics
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Describir un topic específico
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic product-events

# Ver configuración de un topic
docker exec fp-kafka kafka-configs --bootstrap-server localhost:9092 --entity-type topics --entity-name product-events --describe
```

### Monitoreo de Consumer Groups

```powershell
# Listar consumer groups
docker exec fp-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list

# Ver detalles de un group
docker exec fp-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group my-service-group

# Resetear offsets (¡CUIDADO!)
docker exec fp-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --group my-service-group --reset-offsets --to-earliest --topic product-events --execute
```

### Testing Manual

```powershell
# Producir mensajes de prueba
docker exec -it fp-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic product-events

# Consumir mensajes
docker exec fp-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic product-events --from-beginning

# Consumir con consumer group
docker exec fp-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic product-events --group test-group
```

## 📊 Monitoreo Proactivo

### Health Checks Automáticos

```powershell
# Script de verificación de salud
function Test-KafkaHealth {
    Write-Host "Verificando salud de Kafka..." -ForegroundColor Yellow

    # Verificar contenedores
    $containers = docker ps --filter "name=fp-" --format "{{.Names}}" 2>$null
    if ($containers.Count -lt 3) {
        Write-Host "❌ No todos los contenedores están ejecutándose" -ForegroundColor Red
        return $false
    }

    # Verificar conectividad
    $result = docker exec fp-kafka kafka-broker-api-versions --bootstrap-server localhost:9092 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Kafka no responde" -ForegroundColor Red
        return $false
    }

    Write-Host "✅ Kafka está saludable" -ForegroundColor Green
    return $true
}
```

### Alertas de Log

```powershell
# Buscar errores en logs
docker logs fp-kafka 2>&1 | Select-String -Pattern "ERROR|WARN|Exception"
docker logs fp-zookeeper 2>&1 | Select-String -Pattern "ERROR|WARN|Exception"
docker logs fp-kafka-ui 2>&1 | Select-String -Pattern "ERROR|WARN|Exception"
```

## 🆘 Escalación de Problemas

### Información a Recopilar

```powershell
# Crear reporte de diagnóstico
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

Write-Host "Reporte de diagnóstico creado: $reportPath" -ForegroundColor Green
```

### Contacto y Soporte

Si los problemas persisten después de seguir esta guía:

1. Generar reporte de diagnóstico usando el script anterior
2. Revisar logs detallados de microservicios
3. Verificar configuración de red y firewall
4. Crear issue en el repositorio con el reporte completo

## 🔄 Procedimientos de Recuperación

### Recuperación Completa

```powershell
# Parada completa y limpieza
.\scripts\kafka-manager.ps1 stop
docker system prune -f
docker volume prune -f

# Reinicio completo
.\scripts\kafka-manager.ps1 start

# Verificación
.\scripts\kafka-manager.ps1 status
.\scripts\kafka-manager.ps1 topics
```

### Recuperación de Datos

```powershell
# Backup de topics importantes antes de problemas
docker exec fp-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic product-events --from-beginning > backup-product-events.json

# Restaurar mensajes después de recuperación
docker exec -i fp-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic product-events < backup-product-events.json
```

---

**Recuerda**: La mayoría de problemas se resuelven con un reinicio completo del stack. Siempre verifica logs para entender la causa raíz.
