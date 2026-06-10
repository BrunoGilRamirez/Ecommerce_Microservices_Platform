# Ecommerce Microservices Platform

Unified monorepo for an Ecommerce platform built on microservices with Spring Boot,
Spring Cloud, Kafka, and React. The entire system runs in Docker containers.

## Structure

\```
apps/
  frontend/frontend-product-order/   # React + Vite frontend (port 3000)
services/
  auth-service/                      # OAuth2/JWT authentication (port 8081)
  user-service/                      # User management (port 9001)
  product-service/                   # Product CRUD (port 9002)
  order-service/                     # Order management (port 9003)
infrastructure/
  config-server/                     # Spring Cloud Config Server (port 8888)
  discovery-server/                  # Eureka Discovery Server (port 8761)
  gateway/                           # API Gateway (port 8080)
  kafka/                             # Kafka configuration
  mysql/                             # MySQL initialization scripts
\```

## Requirements

- **Docker** and **Docker Compose**

## Quick Start

### Start the entire system

\```bash
./start.sh
\```

The script:
1. Verifies/Clones the configuration repository (`Ecommerce_files_configproperties`)
2. Builds Docker images for all services
3. Brings up containers in order (MySQL → Kafka → Config Server → Discovery → services → Gateway → Frontend)
4. Waits for MySQL, Config Server, and Discovery Server to be ready
5. Prints a summary with the URL for each service

### Stop the entire system

\```bash
./stop.sh
\```

Stops all Docker containers.

### View logs

\```bash
docker compose logs -f              # All services
docker compose logs -f gateway      # A specific service
\```

## Services

| Service           | Port | URL                            |
|-------------------|------|--------------------------------|
| Frontend          | 3000 | http://localhost:3000          |
| Gateway           | 8080 | http://localhost:8080          |
| Auth Service      | 8081 | http://localhost:8081          |
| User Service      | 9001 | http://localhost:9001          |
| Product Service   | 9002 | http://localhost:9002          |
| Order Service     | 9003 | http://localhost:9003          |
| Discovery Server  | 8761 | http://localhost:8761          |
| Config Server     | 8888 | http://localhost:8888          |
| Kafka UI          | 9090 | http://localhost:9090          |
| MySQL             | 3306 | localhost:3306                 |

## Infrastructure

### MySQL Databases

| Database    | Service | User              | Password            |
|-------------|---------|-------------------|---------------------|
| `auth_db`   | auth    | `service_auth`    | `securePassword123` |
| `user_db`   | user    | `service_user`    | `securePassword123` |
| `product_db`| product | `service_product` | `securePassword123` |
| `order_db`  | order   | `service_order`   | `securePassword123` |

### Kafka

- **Broker**: `localhost:9092`
- **Topics**: `product`, `user` (auto-created)
- **Kafka UI**: http://localhost:9090
