# Usage Guide

## Requirements

- **Docker** and **Docker Compose** (included with Docker Desktop)
- ~8 GB free RAM for the 12 containers
- Internet connection (first run only, to download images)

## Quick Start

### 1. Clone the Monorepo

```bash
git clone https://github.com/BrunoGilRamirez/Ecommerce_Microservices_Platform.git
cd Ecommerce_Microservices_Platform
```

### 2. Start the Entire System

```bash
./start.sh
```

The script:
1. Verifies the `Ecommerce_files_configproperties` repository exists (clones it if not)
2. Builds Docker images for all 8 services (Java + Frontend)
3. Starts all 12 containers in the correct order
4. Waits for Config Server and MySQL to become healthy
5. Displays URLs for all services

**First run**: Takes ~10-15 minutes (downloads base Docker images + compiles 7 Java projects with Maven). Subsequent runs are much faster (only start containers).

### 3. Access the Application

| Service | URL |
|---|---|
| Frontend | http://localhost:3000 |
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| Kafka UI | http://localhost:9090 |

### 4. Stop Everything

```bash
./stop.sh
```

Stops and removes all containers.

## Service Administration

### Check container status

```bash
docker ps
```

### View logs for all services

```bash
docker compose logs -f
```

### View logs for a specific service

```bash
docker compose logs -f gateway
docker compose logs -f auth-service
```

### Restart a service

```bash
docker compose restart auth-service
```

### Rebuild and restart a service (after code changes)

```bash
docker compose build auth-service
docker compose up -d auth-service
```

### Rebuild and restart all services

```bash
docker compose build
docker compose up -d --force-recreate
```

### Stop and clean everything (including data volumes)

```bash
docker compose down -v
```

**Warning**: `-v` removes MySQL and Kafka volumes, losing all data.

## Troubleshooting

### A service won't start

Check logs:

```bash
docker logs fp-auth-service
```

Common causes:
- **Config Server not accessible**: Verify `Ecommerce_files_configproperties` exists and has content. Check with `curl http://localhost:8888/actuator/health`
- **MySQL not accessible**: Check MySQL is healthy with `docker ps | grep mysql`
- **Port already in use**: If a local process uses the same port (e.g., 8080), stop it first

### Maven compilation error in Docker

If `mvn package` fails, clear Docker cache and rebuild:

```bash
docker compose build --no-cache auth-service
```

### Kafka unavailable

Services that use Kafka (user-service, order-service, product-service) can start without Kafka active. Verify Kafka:

```bash
docker logs fp-kafka | tail -20
```

### Insufficient resources

If containers stop unexpectedly, memory may be insufficient:

```bash
docker stats   # View resource usage
```

## File Structure

```
Ecommerce_Microservices_Platform/
├── apps/frontend/frontend-product-order/   # Frontend code
├── services/
│   ├── auth-service/                       # Authentication service
│   ├── user-service/                       # User service
│   ├── product-service/                    # Product service
│   └── order-service/                      # Order service
├── infrastructure/
│   ├── config-server/                      # Configuration server
│   ├── discovery-server/                   # Discovery server
│   ├── gateway/                            # API Gateway
│   ├── kafka/                              # Kafka configuration
│   └── mysql/                              # MySQL initialization scripts
├── docs/                                   # Documentation
├── docker-compose.yml                      # Service orchestration
├── start.sh                                # Startup script
├── stop.sh                                 # Shutdown script
└── README.md                               # Main documentation
```

## Service URLs

| Service | Internal URL | External URL |
|---|---|---|
| Frontend | http://localhost:3000 | http://localhost:3000 |
| Gateway | http://localhost:8080 | http://localhost:8080 |
| Auth Service | http://localhost:8081 | http://localhost:8081 |
| User Service | http://localhost:9001 | http://localhost:9001 |
| Product Service | http://localhost:9002 | http://localhost:9002 |
| Order Service | http://localhost:9003 | http://localhost:9003 |
| Eureka | http://localhost:8761 | http://localhost:8761 |
| Config Server | http://localhost:8888 | http://localhost:8888 |
| Kafka UI | http://localhost:9090 | http://localhost:9090 |
| MySQL | localhost:3306 | localhost:3306 |
