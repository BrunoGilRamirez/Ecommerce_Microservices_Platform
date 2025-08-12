# 🚀 FP Kafka Infrastructure

Standalone repository for the Apache Kafka infrastructure used in the FP microservices ecosystem.

## 📋 Description

This repository contains all the configuration needed to run Apache Kafka as a message broker for communication between microservices, including:

- Docker Compose configuration
- Automated management scripts
- Complete documentation
- Optimized settings

## 🏗️ Architecture

```plain
┌─────────────────────────────────────────┐
│              Kafka UI (9090)            │
│          Management Interface           │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│             Kafka Broker               │
│              (Port 9092)               │
│    ┌─────────────────────────────────┐   │
│    │        Topics:                  │   │
│    │  • product-events               │   │
│    │  • user-events                  │   │
│    │  • order-events                 │   │
│    │  • product                      │   │
│    │  • user                         │   │
│    │  • order                        │   │
│    └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│            Zookeeper                   │
│              (Port 2181)               │
│         Kafka Coordination             │
└─────────────────────────────────────────┘
```

## 🚀 Quick Start

### Prerequisites

- Docker installed and running
- Docker Compose
- PowerShell (Windows) or Terminal (Linux/Mac)

### 1. Clone and Navigate

```bash
git clone <repository-url>
cd fp_kafka_infrastructure
```

### 2. Start Infrastructure

```powershell
# Windows PowerShell
.\scripts\kafka-manager.ps1 start

# Linux/Mac
chmod +x scripts/kafka-manager.sh
./scripts/kafka-manager.sh start
```

### 3. Check Status

```powershell
.\scripts\kafka-manager.ps1 status
```

### 4. Access Kafka UI

- URL: <http://localhost:9090>
- Bootstrap Servers: localhost:9092

## 📁 Repository Structure

```plain
fp_kafka_infrastructure/
├── docker-compose.yml              # Main Docker configuration
├── README.md                       # This file
├── scripts/                        # Management scripts
│   ├── kafka-manager.ps1           # PowerShell script
│   └── kafka-manager.sh            # Bash script (future)
├── docs/                           # Documentation
│   ├── SETUP.md                    # Detailed setup guide
│   ├── QUICK_GUIDE.md              # Quick usage guide
│   ├── TROUBLESHOOTING.md          # Troubleshooting
│   └── PRODUCTION.md               # Production setup
├── config/                         # Additional configurations
│   ├── kafka.properties            # Custom Kafka settings
│   └── log4j.properties            # Logging configuration
└── .env.example                    # Example environment variables
```

## 🛠️ Available Commands

### Management Script

| Command                                     | Description                      |
| ------------------------------------------- | -------------------------------- |
| `.\scripts\kafka-manager.ps1 start`         | Starts the entire infrastructure |
| `.\scripts\kafka-manager.ps1 stop`          | Stops all services               |
| `.\scripts\kafka-manager.ps1 restart`       | Restarts services                |
| `.\scripts\kafka-manager.ps1 status`        | Shows current status             |
| `.\scripts\kafka-manager.ps1 logs`          | Streams real-time logs           |
| `.\scripts\kafka-manager.ps1 topics`        | Lists all topics                 |
| `.\scripts\kafka-manager.ps1 create-topics` | Creates topics for microservices |
| `.\scripts\kafka-manager.ps1 help`          | Displays full help               |

### Direct Docker Compose

```powershell
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f kafka

# View status
docker-compose ps
```

## 📋 Configured Topics

| Topic          | Partitions | Replication | Description               |
| -------------- | ---------- | ----------- | ------------------------- |
| product-events | 3          | 1           | Product service events    |
| user-events    | 3          | 1           | User service events       |
| order-events   | 3          | 1           | Order service events      |
| product        | 3          | 1           | Simple topic for products |
| user           | 3          | 1           | Simple topic for users    |
| order          | 3          | 1           | Simple topic for orders   |

## 🔧 Microservice Configuration

### Basic Connection

```properties
# application.properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=my-service-group
spring.kafka.consumer.auto-offset-reset=earliest
```

### Producer Configuration

```java
@Configuration
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }
}
```

### Consumer Configuration

```java
@Configuration
public class KafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-service-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props);
    }
}
```

## 🌐 Ports Used

| Service   | Host Port | Container Port | Description           |
| --------- | --------- | -------------- | --------------------- |
| Zookeeper | 2181      | 2181           | Zookeeper client port |
| Kafka     | 9092      | 9092           | Bootstrap servers     |
| Kafka JMX | 9101      | 9101           | JMX metrics           |
| Kafka UI  | 9090      | 8080           | Web interface         |

## 🔍 Monitoring & Debug

### Kafka UI Dashboard

Visit <http://localhost:9090> to:

- View topics and messages
- Monitor consumers
- Analyze throughput
- Manage configurations

### Useful CLI Commands

```powershell
# List topics
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Describe a topic
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic product-events

# Consume live messages
docker exec fp-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic product-events --from-beginning

# Produce test messages
docker exec -it fp-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic product-events
```

## 📚 Additional Documentation

- [Detailed Setup Guide](docs/SETUP.md)
- [Quick Usage Guide](docs/QUICK_GUIDE.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)
- [Production Setup](docs/PRODUCTION.md)

## 🚀 Integration with Microservices

This repository is designed to integrate with:

- Auth Service (port 8081)
- User Service (port 9002)
- Product Service (port 9001)
- Order Service (port 9003)
- Gateway (port 8080)
- Config Server (port 8888)
- Discovery Server (port 8761)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License – see the [LICENSE](LICENSE) file for details.

## 🆘 Support

If you encounter issues:

1. Check the [troubleshooting documentation](docs/TROUBLESHOOTING.md)
2. Ensure Docker is running
3. Check logs: `.\scripts\kafka-manager.ps1 logs`
4. Open an issue in this repository

---

**Built for the Ecommerce project microservices ecosystem** 🚀
