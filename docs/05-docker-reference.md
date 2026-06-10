# Docker Container Reference

## Images

Each service produces a Docker image named `ecommerce_microservices_platform-{service}`.

| Image | Base | Approx. size |
|---|---|---|
| `ecommerce_microservices_platform-config-server` | eclipse-temurin:17-jre | ~200MB |
| `ecommerce_microservices_platform-discovery-server` | eclipse-temurin:17-jre | ~200MB |
| `ecommerce_microservices_platform-auth-service` | eclipse-temurin:17-jre | ~250MB |
| `ecommerce_microservices_platform-user-service` | eclipse-temurin:17-jre | ~250MB |
| `ecommerce_microservices_platform-product-service` | eclipse-temurin:17-jre | ~250MB |
| `ecommerce_microservices_platform-order-service` | eclipse-temurin:17-jre | ~250MB |
| `ecommerce_microservices_platform-gateway` | eclipse-temurin:17-jre | ~200MB |
| `ecommerce_microservices_platform-frontend` | node:20-alpine | ~300MB |

## Containers

| Container name | Docker service | Port |
|---|---|---|
| `fp-config-server` | `config-server` | 8888 |
| `fp-discovery-server` | `discovery-server` | 8761 |
| `fp-auth-service` | `auth-service` | 8081 |
| `fp-user-service` | `user-service` | 9001 |
| `fp-product-service` | `product-service` | 9002 |
| `fp-order-service` | `order-service` | 9003 |
| `fp-gateway` | `gateway` | 8080 |
| `fp-frontend` | `frontend` | 3000 |
| `fp-mysql` | `mysql` | 3306 |
| `fp-zookeeper` | `zookeeper` | 2181 |
| `fp-kafka` | `kafka` | 9092 |
| `fp-kafka-ui` | `kafka-ui` | 9090 |

## Volumes

| Volume | Mount point | Purpose |
|---|---|---|
| `mysql-data` | `/var/lib/mysql` | MySQL persistent data |
| `kafka-data` | `/var/lib/kafka/data` | Kafka data |

## Networks

| Network | Driver | Purpose |
|---|---|---|
| `ecommerce-network` | bridge | Infrastructure connectivity (MySQL, Kafka) |
| Host | host | Java services (share host network) |

## Environment Variables

### Config Server
| Variable | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` |

### All Java services
| Variable | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` |

### MySQL
| Variable | Value |
|---|---|
| `MYSQL_ROOT_PASSWORD` | `rootpassword` |

## Useful Docker Commands

```bash
# List active containers
docker ps

# List all containers (including stopped)
docker ps -a

# View resource usage
docker stats

# View real-time logs
docker compose logs -f --tail=100

# Run command inside a container
docker exec -it fp-mysql mysql -u root -prootpassword auth_db

# Inspect network configuration
docker inspect fp-auth-service | grep Network

# View container environment variables
docker inspect fp-auth-service | jq '.[0].Config.Env'

# Clean unused images
docker image prune -a

# Clean everything (containers, images, volumes)
docker system prune -a --volumes
```
