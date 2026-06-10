# Ecommerce Microservices Platform

Monorepo unificado de la plataforma Ecommerce basada en microservicios con Spring Boot, Spring Cloud, Kafka y React.

Todo el sistema corre en contenedores Docker.

## Estructura

```
apps/
  frontend/frontend-product-order/   # Frontend React + Vite (puerto 3000)
services/
  auth-service/                      # Autenticación OAuth2/JWT (puerto 8081)
  user-service/                      # Gestión de usuarios (puerto 9001)
  product-service/                   # CRUD de productos (puerto 9002)
  order-service/                     # Gestión de órdenes (puerto 9003)
infrastructure/
  config-server/                     # Spring Cloud Config Server (puerto 8888)
  discovery-server/                  # Eureka Discovery Server (puerto 8761)
  gateway/                           # API Gateway (puerto 8080)
  kafka/                             # Configuración de Kafka
  mysql/                             # Scripts de inicialización de MySQL
```

## Requisitos

- **Docker** y **Docker Compose**

## Inicio rápido

### Arrancar todo el sistema

```bash
./start.sh
```

El script:

1. Verifica/Clona el repositorio de configuraciones (`Ecommerce_files_configproperties`)
2. Construye las imágenes Docker de todos los servicios
3. Levanta los contenedores en orden (MySQL → Kafka → Config Server → Discovery → servicios → Gateway → Frontend)
4. Espera a que MySQL, Config Server y Discovery Server estén listos
5. Muestra un resumen con las URLs de cada servicio

### Detener todo el sistema

```bash
./stop.sh
```

Detiene todos los contenedores Docker.

### Ver logs

```bash
docker compose logs -f              # Todos los servicios
docker compose logs -f gateway      # Un servicio específico
```

## Servicios

| Servicio          | Puerto | URL                            |
|-------------------|--------|--------------------------------|
| Frontend          | 3000   | http://localhost:3000           |
| Gateway           | 8080   | http://localhost:8080           |
| Auth Service      | 8081   | http://localhost:8081           |
| User Service      | 9001   | http://localhost:9001           |
| Product Service   | 9002   | http://localhost:9002           |
| Order Service     | 9003   | http://localhost:9003           |
| Discovery Server  | 8761   | http://localhost:8761           |
| Config Server     | 8888   | http://localhost:8888           |
| Kafka UI          | 9090   | http://localhost:9090           |
| MySQL             | 3306   | localhost:3306                  |

## Infraestructura

### Bases de datos MySQL

| Base de datos | Servicio    | Usuario          | Contraseña        |
|---------------|-------------|------------------|-------------------|
| `auth_db`     | auth        | `service_auth`   | `securePassword123` |
| `user_db`     | user        | `service_user`   | `securePassword123` |
| `product_db`  | product     | `service_product`| `securePassword123` |
| `order_db`    | order       | `service_order`  | `securePassword123` |

### Kafka

- **Broker**: `localhost:9092`
- **Topics**: `product`, `user` (auto-creados)
- **Kafka UI**: http://localhost:9090
