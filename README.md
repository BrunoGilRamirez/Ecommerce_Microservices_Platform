# 🚀 FP Kafka Infrastructure

Repositorio independiente para la infraestructura de Apache Kafka utilizada en el ecosistema de microservicios FP.

## 📋 Descripción

Este repositorio contiene toda la configuración necesaria para ejecutar Apache Kafka como broker de mensajes para la comunicación entre microservicios, incluyendo:

- Configuración Docker Compose
- Scripts de gestión automatizados
- Documentación completa
- Configuraciones optimizadas

## 🏗️ Arquitectura

```plain
┌─────────────────────────────────────────┐
│              Kafka UI (9090)            │
│          Interface de Gestión           │
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
│         Coordinación Kafka             │
└─────────────────────────────────────────┘
```

## 🚀 Inicio Rápido

### Prerrequisitos

- Docker instalado y ejecutándose
- Docker Compose
- PowerShell (Windows) o Terminal (Linux/Mac)

### 1. Clonar y Navegar

```bash
git clone <repository-url>
cd fp_kafka_infrastructure
```

### 2. Iniciar Infraestructura

```powershell
# Windows PowerShell
.\scripts\kafka-manager.ps1 start

# Linux/Mac
chmod +x scripts/kafka-manager.sh
./scripts/kafka-manager.sh start
```

### 3. Verificar Estado

```powershell
.\scripts\kafka-manager.ps1 status
```

### 4. Acceder a Kafka UI

- **URL**: <http://localhost:9090>
- **Bootstrap Servers**: localhost:9092

## 📁 Estructura del Repositorio

```plain
fp_kafka_infrastructure/
├── docker-compose.yml              # Configuración principal de Docker
├── README.md                       # Este archivo
├── scripts/                        # Scripts de gestión
│   ├── kafka-manager.ps1           # Script PowerShell
│   └── kafka-manager.sh            # Script Bash (futuro)
├── docs/                           # Documentación
│   ├── SETUP.md                    # Guía de configuración detallada
│   ├── QUICK_GUIDE.md              # Guía rápida de uso
│   ├── TROUBLESHOOTING.md          # Solución de problemas
│   └── PRODUCTION.md               # Configuración para producción
├── config/                         # Configuraciones adicionales
│   ├── kafka.properties            # Configuración custom de Kafka
│   └── log4j.properties            # Configuración de logging
└── .env.example                    # Variables de entorno ejemplo
```

## 🛠️ Comandos Disponibles

### Script de Gestión

| Comando                                     | Descripción                     |
| ------------------------------------------- | ------------------------------- |
| `.\scripts\kafka-manager.ps1 start`         | Inicia toda la infraestructura  |
| `.\scripts\kafka-manager.ps1 stop`          | Detiene todos los servicios     |
| `.\scripts\kafka-manager.ps1 restart`       | Reinicia los servicios          |
| `.\scripts\kafka-manager.ps1 status`        | Muestra el estado actual        |
| `.\scripts\kafka-manager.ps1 logs`          | Muestra logs en tiempo real     |
| `.\scripts\kafka-manager.ps1 topics`        | Lista todos los topics          |
| `.\scripts\kafka-manager.ps1 create-topics` | Crea topics para microservicios |
| `.\scripts\kafka-manager.ps1 help`          | Muestra ayuda completa          |

### Docker Compose Directo

```powershell
# Iniciar servicios
docker-compose up -d

# Detener servicios
docker-compose down

# Ver logs
docker-compose logs -f kafka

# Ver estado
docker-compose ps
```

## 📋 Topics Configurados

| Topic          | Particiones | Replicación | Descripción                    |
| -------------- | ----------- | ----------- | ------------------------------ |
| product-events | 3           | 1           | Eventos del servicio productos |
| user-events    | 3           | 1           | Eventos del servicio usuarios  |
| order-events   | 3           | 1           | Eventos del servicio pedidos   |
| product        | 3           | 1           | Topic simple para productos    |
| user           | 3           | 1           | Topic simple para usuarios     |
| order          | 3           | 1           | Topic simple para pedidos      |

## 🔧 Configuración de Microservicios

### Conexión Básica

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

## 🌐 Puertos Utilizados

| Servicio  | Puerto Host | Puerto Contenedor | Descripción       |
| --------- | ----------- | ----------------- | ----------------- |
| Zookeeper | 2181        | 2181              | Cliente Zookeeper |
| Kafka     | 9092        | 9092              | Bootstrap servers |
| Kafka JMX | 9101        | 9101              | Métricas JMX      |
| Kafka UI  | 9090        | 8080              | Interface web     |

## 🔍 Monitoreo y Debug

### Kafka UI Dashboard

Accede a <http://localhost:9090> para:

- Visualizar topics y mensajes
- Monitorear consumidores
- Analizar throughput
- Gestionar configuraciones

### Comandos CLI Útiles

```powershell
# Listar topics
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Describir un topic
docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic product-events

# Consumir mensajes en tiempo real
docker exec fp-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic product-events --from-beginning

# Producir mensajes de prueba
docker exec -it fp-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic product-events
```

## 📚 Documentación Adicional

- [Guía de Configuración Detallada](docs/SETUP.md)
- [Guía Rápida de Uso](docs/QUICK_GUIDE.md)
- [Solución de Problemas](docs/TROUBLESHOOTING.md)
- [Configuración para Producción](docs/PRODUCTION.md)

## 🚀 Integración con Microservicios

Este repositorio está diseñado para integrarse con:

- **Auth Service** (puerto 8081)
- **User Service** (puerto 9002)
- **Product Service** (puerto 9001)
- **Order Service** (puerto 9003)
- **Gateway** (puerto 8080)
- **Config Server** (puerto 8888)
- **Discovery Server** (puerto 8761)

## 🤝 Contribución

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/amazing-feature`)
3. Commit tus cambios (`git commit -m 'Add amazing feature'`)
4. Push a la rama (`git push origin feature/amazing-feature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## 🆘 Soporte

Si encuentras problemas:

1. Revisa la [documentación de troubleshooting](docs/TROUBLESHOOTING.md)
2. Verifica que Docker esté ejecutándose
3. Comprueba los logs: `.\scripts\kafka-manager.ps1 logs`
4. Abre un issue en este repositorio

---

**Desarrollado para el ecosistema de microservicios FP** 🚀
