# 🚀 Kafka Infrastructure - Quick Guide

## ✅ Current Status

- **Kafka**: ✅ Running at `localhost:9092`
- **Zookeeper**: ✅ Running at `localhost:2181`
- **Kafka UI**: ✅ Available at [http://localhost:9090](http://localhost:9090)
- **Created topics**: ✅ `product-events`, `user-events`, `order-events`, `product`, `user`, `order`

## 🎯 Essential Commands

### Kafka Management

```powershell
# Start Kafka
.\scripts\kafka-manager.ps1 start

# Stop Kafka
.\scripts\kafka-manager.ps1 stop

# Restart Kafka
.\scripts\kafka-manager.ps1 restart

# Check status
.\scripts\kafka-manager.ps1 status

# View logs
.\scripts\kafka-manager.ps1 logs

# List topics
.\scripts\kafka-manager.ps1 topics
```

### Direct Docker Commands

```powershell
# List topics
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Describe a topic
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic product-events

# Produce test messages
docker exec -it fp-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic product-events

# Consume messages
docker exec -it fp-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic product-events --from-beginning
```

## 🔄 Event Flow in Microservices

### 1. Product Service → Order Service

- **Topic**: `product-events`
- **Events**: CREATED, UPDATED, DELETED
- **Producer**: Product Service
- **Consumer**: Order Service

### 2. User Service → Order Service

- **Topic**: `user-events`
- **Events**: CREATED, UPDATED, DELETED
- **Producer**: User Service
- **Consumer**: Order Service

### 3. Order Service (Audit Events)

- **Topic**: `order-events`
- **Events**: CREATED, UPDATED, CANCELLED
- **Producer**: Order Service

## 🛠️ Microservice Configuration

All microservices must be configured to connect to:

```properties
spring.kafka.bootstrap-servers=localhost:9092
```

### Order Service (Consumer)

- Groups: `product-group`, `user-group`
- Topics: `product-events`, `user-events`

### Product Service (Producer)

- Topic: `product-events`

### User Service (Producer)

- Topic: `user-events`

## 🔧 Microservice Startup Sequence

1. **Kafka Infrastructure** ✅ (This repository)
2. **Config Server** (port 8888)
3. **Discovery Server** (port 8761)
4. **Auth Service** (port 8081)
5. **Gateway** (port 8080)
6. **User Service** (port 9002)
7. **Product Service** (port 9001)
8. **Order Service** (port 9003)

## 📊 Monitoring

### Kafka UI (Recommended)

- **URL**: [http://localhost:9090](http://localhost:9090)
- **Features**:

  - View topics and messages
  - Monitor consumers
  - Analyze throughput
  - Manage configurations

### Microservice Logs

Kafka events are logged with the prefix `"KAFKA:"` in each microservice.

## ⚠️ Important Notes

- Kafka **must** be running **before** starting the microservices
- Topics are created automatically if they do not exist
- To stop everything: `.\scripts\kafka-manager.ps1 stop`
- To check status: `.\scripts\kafka-manager.ps1 status`
- Data is persisted using Docker volumes

## Done

Your Kafka broker is up and running and the topics are created.
You can now start your Spring Boot microservices and observe the real-time communication events.

## 🔗 Useful Links

- [Detailed Configuration](SETUP.md)
- [Troubleshooting](TROUBLESHOOTING.md)
- [Production Configuration](PRODUCTION.md)
