# Product Service

The Product Service is a microservice responsible for managing products in the e-commerce platform. It provides a RESTful API for product creation, retrieval, updating, and deletion across multiple categories, with advanced security features and event-driven architecture.

This service is part of the Ecommerce microservices ecosystem and requires other services (Auth Service, Discovery Server, Config Server) to be running to function properly.

![Service Structure](<./diagrams/Final Project - ServiceStructure.png>)

![Product Flow](<./diagrams/Final Project - ProductFlow.png>)

---

## Main Technologies

- **Spring Boot 3.5.0** – Core framework
- **Spring Security** – OAuth2 Resource Server with JWT
- **Spring Data JPA** – Persistence with MySQL
- **Spring Cloud** – Service discovery and configuration
- **Spring Kafka** – Event streaming and messaging
- **Netflix Eureka Client** – Service Discovery
- **Spring Boot AOP** – Aspect-Oriented Programming
- **Lombok** – Reduces boilerplate code

### Key Components

#### 1. Entities and Data Model

- **[Product.java](src/main/java/com/aspiresys/fp_micro_productservice/product/Product.java)** – Abstract base product entity with inheritance hierarchy

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "category", "imageUrl"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public abstract class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int stock;
    private String name;
    private Double price;
    private String category;
    private String imageUrl;
}
```

##### 1.1 Specialization subclasses for different product categories

- **[Clothes.java](src/main/java/com/aspiresys/fp_micro_productservice/product/subclasses/clothes/Clothes.java)** – Clothing products with specialized attributes
- **[Electronics.java](src/main/java/com/aspiresys/fp_micro_productservice/product/subclasses/electronics/Electronics.java)** – Base electronics entity
  - **[Smartphone.java](src/main/java/com/aspiresys/fp_micro_productservice/product/subclasses/electronics/smartphone/Smartphone.java)** – Smartphone specialization

#### 2. REST Controllers

**[ProductController.java](src/main/java/com/aspiresys/fp_micro_productservice/product/ProductController.java)** – General product management with public access for viewing

- The product management system provides comprehensive CRUD operations with role-based security:

- **Product retrieval** (public access for catalog browsing)

```java
@GetMapping("")
@Auditable(operation = "GET_ALL_PRODUCTS", entityType = "Product", logResult = true)
@ExecutionTime(operation = "Retrieve All Products", warningThreshold = 800)
public ResponseEntity<AppResponse<List<Product>>> getAllProducts() {
    List<Product> products = productService.getAllProducts();
    return ResponseEntity.ok(new AppResponse<>("Product list retrieved successfully", products));
}
```

- **Category management** for product organization

```java
@GetMapping("/categories")
@Auditable(operation = "GET_PRODUCT_CATEGORIES", entityType = "Category", logResult = true)
@ExecutionTime(operation = "Retrieve Product Categories", warningThreshold = 200)
public ResponseEntity<AppResponse<List<String>>> getCategories() {
    List<String> categories = Arrays.asList("clothes", "electronics", "smartphone");
    return ResponseEntity.ok(new AppResponse<>("Categories retrieved successfully", categories));
}
```

**Category-Specific Controllers:**

- **[ClothesController.java](src/main/java/com/aspiresys/fp_micro_productservice/product/subclasses/clothes/ClothesController.java)** – Clothes-specific operations with admin security
- **[ElectronicsController.java](src/main/java/com/aspiresys/fp_micro_productservice/product/subclasses/electronics/ElectronicsController.java)** – Electronics management
- **[SmartphoneController.java](src/main/java/com/aspiresys/fp_micro_productservice/product/subclasses/electronics/smartphone/SmartphoneController.java)** – Smartphone-specific operations

#### 3. Business Services

- **[ProductService.java](src/main/java/com/aspiresys/fp_micro_productservice/product/ProductService.java)** – Product service interface
- **[ProductServiceImpl.java](src/main/java/com/aspiresys/fp_micro_productservice/product/ProductServiceImpl.java)** – Product service implementation
- **[ClothesService.java](src/main/java/com/aspiresys/fp_micro_productservice/product/subclasses/clothes/ClothesService.java)** – Clothes-specific business logic
- **[SmartphoneService.java](src/main/java/com/aspiresys/fp_micro_productservice/product/subclasses/electronics/smartphone/SmartphoneService.java)** – Smartphone business logic

#### 4. OAuth2 Security

**[SecurityConfig.java](src/main/java/com/aspiresys/fp_micro_productservice/config/SecurityConfig.java)** – OAuth2 Resource Server setup

- **JWT Token Validation** using the Authorization Server

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/products/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/products/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/products/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter()))
        )
        .build();
}
```

- **Role-based Access Control** (ADMIN) and **Method-level Security** using @PreAuthorize

```java
@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    return converter;
}

// Usage in controllers:
@PreAuthorize("hasRole('ADMIN')")
```

- **CORS Configuration** for frontend and gateway

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    return source;
}
```

#### 5. Kafka Integration - Event Publishing

**[KafkaProducerConfig.java](src/main/java/com/aspiresys/fp_micro_productservice/kafka/config/KafkaProducerConfig.java)** – Kafka producer configuration

- **Kafka Integration** for publishing product events to other services

```java
@Configuration
@EnableKafka
@Log
public class KafkaProducerConfig {
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

- **Event Publishing** for product lifecycle events

**[ProductEventService.java](src/main/java/com/aspiresys/fp_micro_productservice/kafka/producer/ProductEventService.java)** – Product events publisher

```java
@Service
@Log
public class ProductEventService {
    public void publishProductEvent(Product product, EventType eventType) {
        ProductMessage message = ProductMessage.builder()
            .product(product)
            .eventType(eventType)
            .timestamp(LocalDateTime.now())
            .build();

        kafkaTemplate.send("product", message);
    }
}
```

- **Event-driven Architecture** for microservice communication

Event types supported: **CREATED**, **UPDATED**, **DELETED**, **INITIAL_LOAD** for product synchronization.

#### 6. Aspect-Oriented Programming (AOP)

**[AOP Annotations](src/main/java/com/aspiresys/fp_micro_productservice/aop/annotation/)** – Aspect-Oriented Programming features

- **AOP Auditing** for critical operation traceability

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String operation() default "";
    boolean logResult() default false;
}

// Usage example in ProductController:
@Auditable(operation = "GET_ALL_PRODUCTS", entityType = "Product", logResult = true)
public ResponseEntity<AppResponse<List<Product>>> getAllProducts() {
    // Method implementation - auditing handled automatically by AOP
}
```

- **Performance Monitoring** with execution time metrics

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecutionTime {
    String operation() default "";
    boolean detailed() default false;
}

// Usage example:
@ExecutionTime(operation = "Retrieve All Products", warningThreshold = 800)
public ResponseEntity<AppResponse<List<Product>>> getAllProducts() {
    // Method execution time is automatically measured and logged
}
```

- **Transaction Management** for data operations

```java
@PostMapping("/clothes")
@PreAuthorize("hasRole('ADMIN')")
@Transactional  // Ensures data consistency
@Auditable(operation = "CREATE_CLOTHES", entityType = "Clothes")
public ResponseEntity<AppResponse<ClothesDTO>> createClothes(@RequestBody Clothes clothes) {
    // All database operations within this method are transactional
    // Automatic rollback on exceptions
}
```

## Development Configuration

### Prerequisites

1. **Java 17+**
2. **Maven 3.6+**
3. **MySQL 8.0+**
4. **Apache Kafka 2.8+**
5. **Authorization Server** (fp_micro_authservice) running
6. **Discovery Server** (fp_micro_discoveryserver) running
7. **Config Server** (fp_micro_configserver) running

### Environment Variables

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/product_db
spring.datasource.username=service_product
spring.datasource.password=securePassword123

# OAuth2 Configuration
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8081
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8081/oauth2/jwks

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
kafka.topic.product=product

# Service Discovery
eureka.client.service-url.defaultZone=http://localhost:8761/eureka

# CORS Configuration
service.env.frontend.server=http://localhost:3000
service.env.gateway.server=http://localhost:8080
```

### Database Setup

```sql
-- Create the database
CREATE DATABASE product_db;
CREATE USER 'service_product'@'localhost' IDENTIFIED BY 'securePassword123';
GRANT ALL PRIVILEGES ON product_db.* TO 'service_product'@'localhost';

-- Tables are auto-generated by JPA/Hibernate with inheritance strategy
-- Main structure:
-- - product (id, name, price, stock, category, image_url)
-- - clothes (id, brand, size, color, fabric_type) [inherits from product]
-- - electronics (id, brand, model, warranty_period, specifications) [inherits from product]
-- - smartphone (id, operating_system, storage_capacity, ram, processor, screen_size) [inherits from electronics]
```

### Kafka Setup

```bash
# Create required topics
kafka-topics.sh --create --topic product --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

## API Endpoints

### Authentication Required

Most management endpoints require a valid JWT token from the Authorization Server:

```text
Authorization: Bearer <jwt_token>
```

### Public Endpoints

#### Get All Products

```http
GET /products
```

**Response:**

```json
{
  "message": "Product list retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Basic T-shirt",
      "price": 199.99,
      "stock": 100,
      "category": "clothes",
      "imageUrl": "https://ejemplo.com/camiseta.jpg"
    }
  ]
}
```

#### Get Product Categories

```http
GET /products/categories
```

**Response:**

```json
{
  "message": "Categories retrieved successfully",
  "data": ["clothes", "electronics", "smartphone"]
}
```

### Admin Endpoints (Role ADMIN)

#### Create Clothes Product

```http
POST /products/clothes
Content-Type: application/json
Authorization: Bearer <admin_jwt_token>

{
  "name": "Premium Shirt",
  "price": 299.99,
  "stock": 50,
  "category": "clothes",
  "imageUrl": "https://example.com/shirt.jpg",
  "brand": "FashionBrand",
  "size": "L",
  "color": "Red",
  "fabricType": "Cotton"
}
```

#### Update Product

```http
PUT /products/clothes/{id}
Content-Type: application/json
Authorization: Bearer <admin_jwt_token>

{
  "id": 1,
  "name": "Updated Premium Shirt",
  "price": 279.99,
  "stock": 45
}
```

#### Delete Product

```http
DELETE /products/clothes/{id}
Authorization: Bearer <admin_jwt_token>
```

## Kafka Integration

### Publishing Product Events

The service publishes events to the `product` topic for other services to consume:

```java
@Service
public class ProductEventService {

    @KafkaProducer(topic = "${kafka.topic.product:product}")
    public void publishProductEvent(Product product, EventType eventType) {
        ProductMessage message = ProductMessage.builder()
            .product(product)
            .eventType(eventType)
            .timestamp(LocalDateTime.now())
            .build();

        kafkaTemplate.send("product", message);
    }
}
```

### Supported Event Types

- **CREATED** – New product created
- **UPDATED** – Product updated
- **DELETED** – Product deleted
- **INITIAL_LOAD** – Initial product synchronization

## Aspect-Oriented Programming

### Operation Auditing

```java
@Auditable(operation = "CREATE_CLOTHES", entityType = "Clothes", logParameters = true, logResult = true)
public ResponseEntity<AppResponse<ClothesDTO>> createClothes(@RequestBody Clothes clothes) {
    // Auditing is handled automatically by AOP
}
```

### Performance Monitoring

```java
@ExecutionTime(operation = "Retrieve All Products", warningThreshold = 800, detailed = true)
public ResponseEntity<AppResponse<List<Product>>> getAllProducts() {
    // Execution time is automatically logged
}
```

### Parameter Validation

```java
@ValidateParameters(notNull = true, notEmpty = true, message = "Product data cannot be null or empty")
public ResponseEntity<AppResponse<ProductDTO>> createProduct(@RequestBody Product product) {
    // Validation is performed before method execution
}
```

## Advanced Business Logic

### Product Entity Business Methods

The Product entity includes sophisticated business logic for product management:

```java
public abstract class Product {
    public boolean isInStock() {
        return stock > 0;
    }

    public boolean hasLowStock() {
        return stock <= 10;
    }

    public void updateStock(int quantity) {
        this.stock = Math.max(0, this.stock + quantity);
    }
}
```

### Multi-Category Support

The service uses JPA inheritance to support multiple product categories:

```java
// Clothes specialization
@Entity
public class Clothes extends Product {
    private String brand;
    private String size;
    private String color;
    private String fabricType;

    public boolean isAvailableInSize(String requestedSize) {
        return this.size.equals(requestedSize) && isInStock();
    }
}

// Smartphone specialization
@Entity
public class Smartphone extends Electronics {
    private String operatingSystem;
    private Integer storageCapacity;
    private Integer ram;
    private String processor;
    private Double screenSize;

    public boolean isCompatibleWith(String osVersion) {
        return this.operatingSystem.contains(osVersion);
    }
}
```

## Logging Configuration

### Logback Configuration

Centralized logging via Config Server:

- **Application Logs**: `logs/product-service/application.log`
- **Error Logs**: `logs/product-service/error.log`
- **Audit Logs**: Included in application logs (INFO level)
- **Performance Logs**: Warnings for slow operations

### Log Structure

```text
[TIMESTAMP] [LEVEL] [THREAD] [LOGGER] - [MESSAGE]
- Kafka events: KAFKA: prefix
- AOP operations: AOP: prefix
- Security events: SECURITY: prefix
- Business logic: PRODUCT: prefix
```

## Running and Development

### Build and Run

```bash
# Compile project
mvn clean compile

# Run tests
mvn test

# Start application
mvn spring-boot:run
```

### IDE Configuration

1. **Default port**: 9002
2. **Active profile**: development

### Health Checks

```http
GET /actuator/health
GET /actuator/info
```

---
