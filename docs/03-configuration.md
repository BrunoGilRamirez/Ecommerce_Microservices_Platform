# System Configuration

## Configuration Repository

All microservices fetch their configuration from a central Git repository:

**https://github.com/BrunoGilRamirez/Ecommerce_files_configproperties**

### Repository Structure

```
Ecommerce_files_configproperties/
├── application.properties
├── logback-spring.xml
├── authservice/
│   ├── fp_micro_authservice.properties
│   └── fp_micro_authservice-logback-spring.xml
├── userservice/
│   ├── fp_micro_userservice.properties
│   └── fp_micro_userservice-logback-spring.xml
├── productservice/
│   ├── fp_micro_productservice.properties
│   └── fp_micro_productservice-logback-spring.xml
├── orderservice/
│   ├── fp_micro_orderservice.properties
│   └── fp_micro_orderservice-logback-spring.xml
├── gateway/
│   ├── fp_micro_gateway.properties
│   └── fp_micro_gateway-logback-spring.xml
├── configserver/
│   └── fp_micro_configserver-logback-spring.xml
└── discoveryserver/
    ├── fp_micro_discoveryserver.properties
    └── fp_micro_discoveryserver-logback-spring.xml
```

### Branches

The Config Server is configured to use the `dev-profile` branch (default). To switch to production, modify `spring.cloud.config.server.git.default-label` in the Config Server's `application-dev.properties`.

## Per-Service Configuration

### Config Server (:8888)

| Property | Value | Description |
|---|---|---|
| `server.port` | 8888 | Server port |
| `spring.cloud.config.server.git.uri` | `file:///fp_files_configproperties` | Path to local Git repo |
| `spring.cloud.config.server.git.default-label` | `dev-profile` | Repository branch |
| `spring.cloud.config.server.git.search-paths` | `**` | Search all subdirectories |

### Discovery Server (:8761)

| Property | Value |
|---|---|
| `server.port` | 8761 |
| `eureka.client.register-with-eureka` | `false` |
| `eureka.client.fetch-registry` | `false` |
| `eureka.server.enable-self-preservation` | `false` |

### Auth Service (:8081)

| Property | Value |
|---|---|
| `server.port` | 8081 |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/auth_db` |
| `spring.security.oauth2.authorizationserver.issuer-url` | `http://localhost:8081` |
| `service.env.frontend.server` | `http://localhost:3000` |

### User Service (:9001)

| Property | Value |
|---|---|
| `server.port` | 9001 |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/user_db` |
| `spring.kafka.bootstrap-servers` | `localhost:9092` |
| `kafka.topic.user` | `user` |
| `eureka.client.service-url.defaultZone` | `http://localhost:8761/eureka/` |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | `http://localhost:8081` |

### Product Service (:9002)

| Property | Value |
|---|---|
| `server.port` | 9002 |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/product_db` |
| `spring.kafka.bootstrap-servers` | `localhost:9092` |
| `kafka.topic.product` | `product` |

### Order Service (:9003)

| Property | Value |
|---|---|
| `server.port` | 9003 |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/order_db` |
| `spring.kafka.bootstrap-servers` | `localhost:9092` |
| `kafka.topic.product` | `product` |
| `kafka.topic.user` | `user` |

### Gateway (:8080)

| Property | Value |
|---|---|
| `server.port` | 8080 |
| `eureka.client.service-url.defaultZone` | `http://localhost:8761/eureka/` |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | `http://localhost:8081` |

## Spring Profiles

Each service has the `dev` profile active via the environment variable `SPRING_PROFILES_ACTIVE=dev`.

| File | Purpose |
|---|---|
| `application.properties` | Base configuration (service name, default port) |
| `application-dev.properties` | Development configuration (Config Server import) |

The `dev` profile includes `spring.config.import=optional:configserver:http://localhost:8888`, which tells the service to fetch additional configuration from Config Server. The `optional:` prefix allows the service to start even if Config Server is unavailable.

## MySQL Databases

### Schemas and Credentials

| Database | Service | Username | Password |
|---|---|---|---|
| `auth_db` | Auth | `service_auth` | `securePassword123` |
| `user_db` | User | `service_user` | `securePassword123` |
| `product_db` | Product | `service_product` | `securePassword123` |
| `order_db` | Order | `service_order` | `securePassword123` |

### Initialization

The script `infrastructure/mysql/init/001-init.sql` runs automatically the first time MySQL starts. It creates the databases, users, and grants the necessary permissions.
