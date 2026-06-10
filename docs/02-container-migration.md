# Container Migration

## Summary

Originally each microservice ran directly with Maven (`./mvnw spring-boot:run`). Migrated to Docker to:

- Eliminate local dependencies (Java, Maven, Node.js)
- Ensure reproducible environments
- Simplify startup with a single command
- Isolate services from each other

## Changes Made

### 1. Dockerfiles for each Java service

Each Java service has a `Dockerfile` in its root directory:

```
services/auth-service/Dockerfile
services/user-service/Dockerfile
services/product-service/Dockerfile
services/order-service/Dockerfile
infrastructure/config-server/Dockerfile
infrastructure/discovery-server/Dockerfile
infrastructure/gateway/Dockerfile
apps/frontend/frontend-product-order/Dockerfile
```

**Dockerfile structure (Java):**

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.war app.war
ENTRYPOINT ["java", "-jar", "app.war"]
```

- **Multi-stage build**: The `build` stage compiles with Maven, the final stage only contains the JRE and packaged WAR.
- **Reduced final image**: Only includes Eclipse Temurin 17 JRE (~200MB).

**Dockerfile structure (Frontend):**

```dockerfile
FROM node:20-alpine
WORKDIR /app
COPY package*.json .
RUN npm install
COPY . .
EXPOSE 3000
CMD ["npm", "run", "dev", "--", "--host"]
```

### 2. WAR vs JAR files

All Spring Boot projects are packaged as **WAR** (not JAR). Each service's `pom.xml` defines:

```xml
<packaging>war</packaging>
```

Spring Boot Maven Plugin packages the WAR as executable (`java -jar`), so it works the same as a JAR. The Dockerfile copies the `*.war` file and runs it with `java -jar app.war`.

### 3. docker-compose.yml

Single file at the monorepo root defining 12 services:

**Infrastructure:**
- `zookeeper`, `kafka`, `kafka-ui` — Kafka cluster
- `mysql` — MySQL 8.0 database

**Java services (network_mode: host):**
- `config-server`, `discovery-server`, `auth-service`, `user-service`, `product-service`, `order-service`, `gateway`

**Frontend:**
- `frontend`

### 4. Host networking

Java services use `network_mode: host` instead of Docker virtual networks.

**Reason**: Configuration files (served by Config Server) contain `localhost` references for database, Kafka, and other service connections. With host networking, containers share the host's network and `localhost` works the same as in local development without Docker.

**Advantage**: No property file modifications were needed for the services.

**Limitation**: Only works on Linux. On macOS/Windows, Docker containers run inside a VM, so `localhost` inside the container does not resolve to the host. Those systems would require Docker networking or replacing `localhost` with `host.docker.internal`.

### 5. Bind mount volume for Config Server

The Config Server needs access to the Git repository with configuration files:

```yaml
config-server:
  volumes:
    - ../Ecommerce_files_configproperties:/fp_files_configproperties
```

This mounts the cloned repository at `/fp_files_configproperties` inside the container, where the Config Server looks for it using the `file:///fp_files_configproperties` URI (defined in `application-dev.properties`).

### 6. Healthchecks

Config Server and MySQL have healthchecks to control startup order:

```yaml
config-server:
  healthcheck:
    test: ["CMD", "bash", "-c", "curl -s http://localhost:8888/actuator/health"]
    interval: 5s
    timeout: 5s
    retries: 20

mysql:
  healthcheck:
    test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-prootpassword"]
    interval: 5s
    timeout: 3s
    retries: 10
```

Other services use `depends_on` with `condition: service_healthy` to wait for Config Server and MySQL to be ready before starting.

## Startup Flow

```
1. Docker: mysql, config-server, zookeeper, kafka, kafka-ui
   (all start in parallel)
   
2. Wait: mysql healthy, config-server healthy
   
3. discovery-server
   (depends on: config-server healthy)
   
4. auth-service, user-service, product-service, order-service
   (depends on: mysql healthy, config-server healthy, discovery-server started)
   
5. gateway
   (depends on: config-server healthy)
   
6. frontend
   (no health dependencies)
```
