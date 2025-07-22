# 🚀 Kafka Infrastructure - Guía Rápida

## ✅ Estado Actual

- **Kafka**: ✅ Ejecutándose en `localhost:9092`
- **Zookeeper**: ✅ Ejecutándose en `localhost:2181`
- **Kafka UI**: ✅ Disponible en <http://localhost:9090>
- **Topics creados**: ✅ `product-events`, `user-events`, `order-events`, `product`, `user`, `order`

## 🎯 Comandos Esenciales

### Gestión de Kafka

```powershell
# Iniciar Kafka
.\scripts\kafka-manager.ps1 start

# Detener Kafka
.\scripts\kafka-manager.ps1 stop

# Reiniciar Kafka
.\scripts\kafka-manager.ps1 restart

# Ver estado
.\scripts\kafka-manager.ps1 status

# Ver logs
.\scripts\kafka-manager.ps1 logs

# Listar topics
.\scripts\kafka-manager.ps1 topics
```

### Comandos Docker Directos

```powershell
# Listar topics
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Ver detalles de un topic
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic product-events

# Producir mensajes de prueba
docker exec -it fp-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic product-events

# Consumir mensajes
docker exec -it fp-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic product-events --from-beginning
```

## 🔄 Flujo de Eventos en Microservicios

### 1. Product Service → Order Service

- **Topic**: `product-events`
- **Eventos**: CREATED, UPDATED, DELETED
- **Producer**: Product Service
- **Consumer**: Order Service

### 2. User Service → Order Service

- **Topic**: `user-events`
- **Eventos**: CREATED, UPDATED, DELETED
- **Producer**: User Service
- **Consumer**: Order Service

### 3. Order Service (Eventos de auditoría)

- **Topic**: `order-events`
- **Eventos**: CREATED, UPDATED, CANCELLED
- **Producer**: Order Service

## 🛠️ Configuración de Microservicios

Todos los microservicios deben configurarse para conectar a:

```properties
spring.kafka.bootstrap-servers=localhost:9092
```

### Order Service (Consumer)

- Grupos: `product-group`, `user-group`
- Topics: `product-events`, `user-events`

### Product Service (Producer)

- Topic: `product-events`

### User Service (Producer)

- Topic: `user-events`

## 🔧 Secuencia de Inicio de Microservicios

1. **Kafka Infrastructure** ✅ (Este repositorio)
2. **Config Server** (puerto 8888)
3. **Discovery Server** (puerto 8761)
4. **Auth Service** (puerto 8081)
5. **Gateway** (puerto 8080)
6. **User Service** (puerto 9002)
7. **Product Service** (puerto 9001)
8. **Order Service** (puerto 9003)

## 📊 Monitoreo

### Kafka UI (Recomendado)

- **URL**: <http://localhost:9090>
- **Funciones**:
  - Ver topics y mensajes
  - Monitorear consumidores
  - Analizar throughput
  - Gestionar configuraciones

### Logs de Microservicios

Los eventos Kafka se loguean con prefijo `"KAFKA:"` en cada microservicio.

## ⚠️ Notas Importantes

- Kafka debe estar ejecutándose ANTES de iniciar los microservicios
- Los topics se crean automáticamente si no existen
- Para detener todo: `.\scripts\kafka-manager.ps1 stop`
- Para ver estado: `.\scripts\kafka-manager.ps1 status`
- Los datos se persisten en volúmenes Docker

## Listo

Tu broker Kafka está funcionando correctamente y los topics están creados.
Ahora puedes iniciar tus microservicios Spring Boot y verás los eventos de comunicación en tiempo real.

## 🔗 Enlaces Útiles

- [Configuración Detallada](SETUP.md)
- [Solución de Problemas](TROUBLESHOOTING.md)
- [Configuración para Producción](PRODUCTION.md)
