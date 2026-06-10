# System Architecture

## Overview

Ecommerce platform based on a microservices architecture. Each service is independent, communicates via HTTP (REST) and asynchronous messaging (Kafka), and is registered with a Discovery Server (Eureka) for dynamic discovery.

```
                    ┌─────────────┐
                    │  Frontend   │
                    │  React/Vite │
                    │  :3000      │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │   Gateway   │
                    │  Spring     │
                    │  :8080      │
                    └──┬───┬───┬──┘
                       │   │   │
              ┌────────┘   │   └────────┐
              ▼            ▼            ▼
       ┌──────────┐ ┌──────────┐ ┌──────────┐
       │   Auth   │ │  User    │ │ Product  │
       │  :8081   │ │  :9001   │ │  :9002   │
       └────┬─────┘ └────┬─────┘ └────┬─────┘
            │            │            │
            ▼            ▼            ▼
       ┌─────────────────────────────────┐
       │          Order :9003            │
       └─────────────────────────────────┘
```

## Service Communication

### Synchronous (HTTP)

Each service exposes a REST API. The Gateway centralizes routing:

| Gateway Route | Destination Service |
|---|---|
| `/auth/**` | Auth Service (:8081) |
| `/api/users/**` | User Service (:9001) |
| `/api/products/**` | Product Service (:9002) |
| `/api/orders/**` | Order Service (:9003) |

### Asynchronous (Kafka)

Messaging for data synchronization between services:

```
Product Service ──topic:product──► Order Service
User Service    ──topic:user────► Order Service
```

- **`product` topic**: Product Service sends product events (creation, update). Order Service consumes them to maintain a local reference copy.
- **`user` topic**: User Service sends user events. Order Service consumes them for order validation.

## System Components

### 1. Config Server (:8888)
- **Spring Cloud Config Server**
- Provides centralized configuration to all services
- Reads properties from a local Git repository (`Ecommerce_files_configproperties`)
- Each service fetches its configuration on startup (`spring.config.import=optional:configserver:`)

### 2. Discovery Server (:8761)
- **Eureka Server** (Netflix)
- Service registration and discovery
- Services register with their name and port
- Gateway queries Eureka for dynamic routing

### 3. Auth Service (:8081)
- **Spring Authorization Server** with OAuth2
- Handles authentication (login/register) and JWT token issuance
- Supports PKCE flow for the frontend
- Endpoints: `/auth/login`, `/auth/register`, `/oauth2/**`

### 4. User Service (:9001)
- User CRUD operations
- Roles: `USER`, `ADMIN`
- User synchronization via Kafka to Order Service
- JWT-protected endpoints

### 5. Product Service (:9002)
- Product CRUD (clothes, electronics, smartphones)
- Initial sample data loading
- Product synchronization via Kafka to Order Service
- JWT-protected endpoints

### 6. Order Service (:9003)
- Purchase order management
- Consumes Kafka events to maintain local copies of products and users
- Validates orders against locally synchronized data
- JWT-protected endpoints

### 7. Gateway (:8080)
- **Spring Cloud Gateway**
- Request routing to internal services
- JWT token validation
- CORS configuration for the frontend

### 8. Frontend (:3000)
- **React + Vite + TypeScript**
- User interface for the online store
- Admin panel for product and order management

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.x / 3.5.x |
| Cloud | Spring Cloud 2023.x / 2025.x |
| Auth | Spring Authorization Server / OAuth2 / JWT |
| Discovery | Netflix Eureka |
| Config | Spring Cloud Config Server |
| Gateway | Spring Cloud Gateway |
| Frontend | React 19 + Vite + TypeScript |
| Database | MySQL 8.0 |
| Messaging | Apache Kafka |
| Containers | Docker + Docker Compose |
| Build | Maven |
