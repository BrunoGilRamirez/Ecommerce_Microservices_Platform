# User Service - User Management Microservice

The **User Service** is a Spring Boot microservice and part of the Ecommerce project's microservices architecture. It handles comprehensive user management, including CRUD operations, OAuth2 authentication, and asynchronous communication with other services through Kafka events.

![Service Structure](<./diagrams/Final Project - ServiceStructure.png>)

![User Flow](<./diagrams/Final Project - UserFlow.png>)

---

## Main Features

- **User Management**: Full create, read, update, and delete operations
- **Asynchronous Communication**: User event publishing via Apache Kafka
- **Cross-cutting Concerns**: Auditing, logging, and metrics using Spring AOP
- **Service Discovery**: Automatic registration with Eureka Discovery Server

---

## Technology Stack

| Technology          | Version | Purpose                        |
| ------------------- | ------- | ------------------------------ |
| **Java**            | 21      | Programming language           |
| **Spring Boot**     | 3.5.0   | Main framework                 |
| **Spring Security** | 6.x     | Authentication & authorization |
| **Spring Data JPA** | 3.x     | Data persistence               |
| **MySQL**           | 8.x     | Relational database            |
| **Apache Kafka**    | 3.x     | Asynchronous messaging         |
| **Netflix Eureka**  | 4.x     | Service discovery              |
| **Maven**           | 3.x     | Dependency management          |

---

## Architecture

### Project Structure

```text
fp_micro_userservice/
├── src/main/java/com/aspiresys/fp_micro_userservice/
│   ├── FpMicroUserserviceApplication.java
│   ├── aop/
│   │   ├── annotation/
│   │   └── aspect/
│   │       ├── AuditAspect.java
│   │       ├── ExecutionTimeAspect.java
│   │       ├── UserOperationAspect.java
│   │       └── ValidationAspect.java
│   ├── config/
│   │   ├── AopConfig.java
│   │   ├── InternalServiceFilter.java
│   │   └── SecurityConfig.java
│   ├── kafka/
│   │   ├── config/
│   │   ├── dto/
│   │   └── producer/
│   │       └── UserProducerService.java
│   └── user/
│       ├── User.java
│       ├── UserController.java
│       ├── UserRepository.java
│       └── UserService.java
├── src/test/
└── pom.xml
```

### Design Patterns Used

1. **Repository Pattern**: [`UserRepository`](src/main/java/com/aspiresys/fp_micro_userservice/user/UserRepository.java)
2. **Service Layer Pattern**: [`UserService`](src/main/java/com/aspiresys/fp_micro_userservice/user/UserService.java)
3. **Producer Pattern**: [`UserProducerService`](src/main/java/com/aspiresys/fp_micro_userservice/kafka/producer/UserProducerService.java)
4. **Aspect-Oriented Programming**: [`aop/aspect/`](src/main/java/com/aspiresys/fp_micro_userservice/aop/aspect/)

---

## Security Configuration

The service uses OAuth2 with JWT for secure authentication and authorization.

### Endpoints and Authorization

| Endpoint            | Method | Required Roles  | Description            |
| ------------------- | ------ | --------------- | ---------------------- |
| `/users/hello`      | GET    | Public          | Health check           |
| `/users/find/**`    | GET    | Internal Public | User validation        |
| `/users/me`         | GET    | USER            | Current user info      |
| `/users/me/create`  | POST   | USER            | Create user profile    |
| `/users/me/update`  | PUT    | USER            | Update user profile    |
| `/users/me/delete`  | DELETE | USER            | Delete user profile    |
| `/users/me/current` | GET    | USER, ADMIN     | Current user full info |
| `/actuator/**`      | \*     | ADMIN           | Metrics and monitoring |

### OAuth2 Configuration

Defined in [`SecurityConfig`](src/main/java/com/aspiresys/fp_micro_userservice/config/SecurityConfig.java):

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    // JWT Resource Server
    // Claims to Authorities conversion
    // CORS configuration for frontend
}
```

---

## Data Model

### User Entity

The [`User`](src/main/java/com/aspiresys/fp_micro_userservice/user/User.java) class represents the main data model:

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String address;
}
```

### Database Table

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL
);
```

---

## API Endpoints

### User Management

Exposed via [`UserController`](src/main/java/com/aspiresys/fp_micro_userservice/user/UserController.java):

#### Get Current User

```http
GET /users/me
Authorization: Bearer {jwt_token}
```

#### Create User

```http
POST /users/me/create
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "address": "123 Main St, City, Country"
}
```

#### Update User

```http
PUT /users/me/update
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "firstName": "John Updated",
    "lastName": "Doe Updated",
    "email": "john.updated@example.com",
    "address": "456 New St, City, Country"
}
```

#### Delete User

```http
DELETE /users/me/delete
Authorization: Bearer {jwt_token}
```

---

## Kafka Integration

### User Events

Published via [`UserProducerService`](src/main/java/com/aspiresys/fp_micro_userservice/kafka/producer/UserProducerService.java):

- **USER_CREATED**: On new user creation
- **USER_UPDATED**: On user update
- **USER_DELETED**: On user deletion

### Message Structure

```java
@Builder
public class UserMessage {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String eventType;
    private LocalDateTime timestamp;
}
```

### Kafka Configuration

```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
kafka.topic.user=user
```

---

## Aspect-Oriented Programming (AOP)

### Implemented Aspects

1. **[`AuditAspect`](src/main/java/com/aspiresys/fp_micro_userservice/aop/aspect/AuditAspect.java)**: Operation audit
2. **[`ExecutionTimeAspect`](src/main/java/com/aspiresys/fp_micro_userservice/aop/aspect/ExecutionTimeAspect.java)**: Performance metrics
3. **[`UserOperationAspect`](src/main/java/com/aspiresys/fp_micro_userservice/aop/aspect/UserOperationAspect.java)**: Domain-specific aspects
4. **[`ValidationAspect`](src/main/java/com/aspiresys/fp_micro_userservice/aop/aspect/ValidationAspect.java)**: Input validation

### AOP Configuration

```properties
spring.aop.auto=true
spring.aop.proxy-target-class=true
app.aop.audit.enabled=true
app.aop.performance.enabled=true
app.aop.validation.enabled=true
```

---

## Configuration

### Environment Variables

| Variable         | Default Value                                                | Description           |
| ---------------- | ------------------------------------------------------------ | --------------------- |
| `SERVER_PORT`    | 9001                                                         | Service port          |
| `MYSQL_HOST`     | localhost:3306                                               | MySQL host            |
| `MYSQL_DATABASE` | user_db                                                      | Database name         |
| `MYSQL_USER`     | service_user                                                 | MySQL user            |
| `MYSQL_PASSWORD` | securePassword123                                            | MySQL password        |
| `EUREKA_URI`     | [http://localhost:8761/eureka](http://localhost:8761/eureka) | Eureka server         |
| `AUTH_SERVER`    | [http://localhost:8081](http://localhost:8081)               | Authentication server |
| `KAFKA_SERVERS`  | localhost:9092                                               | Kafka brokers         |

### Main Application

[`FpMicroUserserviceApplication`](src/main/java/com/aspiresys/fp_micro_userservice/FpMicroUserserviceApplication.java) includes:

- Spring Boot configuration
- Eureka auto-registration
- Test data initialization
- Core bean configuration

---

## Running the Application

### Prerequisites

1. Java 21 installed
2. MySQL 8.x running on port 3306
3. Apache Kafka running on port 9092
4. Config Server running on port 8888
5. Discovery Server running on port 8761
6. Auth Service running on port 8081

### Execution Commands

```bash
mvn clean compile
mvn test
mvn package
java -jar target/fp_micro_userservice-0.0.1-SNAPSHOT.war
# or
mvn spring-boot:run
```

### Docker (Optional)

```dockerfile
FROM openjdk:21-jdk
COPY target/fp_micro_userservice-0.0.1-SNAPSHOT.war app.war
EXPOSE 9001
ENTRYPOINT ["java", "-jar", "/app.war"]
```

---

## Testing

### Test Structure

- Unit tests: Business logic
- Integration tests: REST endpoints
- Security tests: OAuth2 validation

### Run Tests

```bash
mvn test
mvn test -Dtest="*Test"
mvn test -Dtest="*IT"
```

---

## Main Dependencies

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
</dependencies>
```

---
