# Project Schema — Ecommerce Microservices Platform

## Index

1. [General Architecture Diagram](#1-general-architecture-diagram)
2. [Auth Service](#2-auth-service)
3. [User Service](#3-user-service)
4. [Product Service](#4-product-service)
5. [Order Service](#5-order-service)
6. [Gateway](#6-gateway)
7. [Config Server & Discovery Server](#7-config-server--discovery-server)
8. [Frontend](#8-frontend)
9. [Entity Relationship Diagram (ERD)](#9-entity-relationship-diagram-erd)
10. [Kafka Event Flow](#10-kafka-event-flow)
11. [OAuth2 Authentication Flow](#11-oauth2-authentication-flow)

---

## 1. General Architecture Diagram

```mermaid
graph TB
    subgraph Client
        FRONTEND["Frontend React + Vite\nport 9001"]
    end

    subgraph "Infrastructure (Docker)"
        GW["Gateway Spring Cloud\nport 8080"]
        CS["Config Server\nport 8888"]
        DS["Discovery Server (Eureka)\nport 8761"]
        MYSQL["MySQL\nport 3306"]
        KAFKA["Kafka\nport 9092"]
    end

    subgraph "Microservices"
        AUTH["Auth Service\n(OAuth2 Authorization Server)\nport 8081"]
        USER["User Service\nport 9002"]
        PRODUCT["Product Service\nport 9003"]
        ORDER["Order Service\nport 9004"]
    end

    FRONTEND -->|"HTTP :8080"| GW
    FRONTEND -->|"HTTP :8081 /oauth2"| AUTH

    GW -->|"/auth/**"| AUTH
    GW -->|"/api/user-service/**"| USER
    GW -->|"/api/product-service/**"| PRODUCT
    GW -->|"/api/order-service/**"| ORDER

    CS -->|"serves config"| GW
    CS -->|"serves config"| AUTH
    CS -->|"serves config"| USER
    CS -->|"serves config"| PRODUCT
    CS -->|"serves config"| ORDER

    AUTH -.->|"register"| DS
    USER -.->|"register"| DS
    PRODUCT -.->|"register"| DS
    ORDER -.->|"register"| DS
    GW -.->|"discovery"| DS

    AUTH -->|"JDBC"| MYSQL
    USER -->|"JDBC"| MYSQL
    PRODUCT -->|"JDBC"| MYSQL
    ORDER -->|"JDBC"| MYSQL

    USER -->|"produce"| KAFKA
    PRODUCT -->|"produce"| KAFKA
    ORDER -->|"consume"| KAFKA
```

---

## 2. Auth Service

### Service Layers

```mermaid
graph TB
    subgraph "Controllers"
        RC["RegisterController\n/auth/api"]
        UWC["UserWebController\n/user"]
        LC["LoginController\n/login, /"]
        OIC["OAuth2InfoController\n/oauth2"]
        OCC["OAuth2ConsentController\n/oauth2/consent"]
        AEC["AuthErrorController\n/error"]
    end

    subgraph "Service Layer"
        MDS["MyUserDetailsService\nimplements UserDetailsService"]
    end

    subgraph "Repository Layer"
        AUR["AppUserRepository\nJpaRepository"]
        RR["RoleRepository\nCrudRepository"]
    end

    subgraph "Entities"
        AU["AppUser\n@Entity @Table=app_user"]
        R["Role\n@Entity @Table=role"]
    end

    subgraph "Config"
        SC["SecurityConfig\nSecurityFilterChain, PasswordEncoder,\nJWKSource, OAuth2TokenCustomizer"]
        CC["ClientConfig\nRegisteredClientRepository"]
        DI["DataInitializer\nSeeder: roles + admin"]
        AC["AuthConstants\nOAuth2 constants"]
    end

    RC --> AUR
    RC --> RR
    MDS --> AUR
    UWC --> AUR
    UWC --> RR

    AU -->|"@ManyToMany"| R
    R -->|"implements GrantedAuthority"| AU

    DI --> AUR
    DI --> RR
    SC --> AC
    CC --> AC
```

### Endpoints

| Method | Path | Access | Description |
|--------|------|--------|-------------|
| `POST` | `/auth/api/register` | Public | User registration via REST |
| `GET` | `/user/register` | Public | Registration form |
| `POST` | `/user/register` | Public | User registration via form |
| `GET` | `/login` | Public | Login page |
| `GET` | `/` | Public | Home |
| `GET` | `/oauth2/consent` | Public | OAuth2 consent screen |
| `GET` | `/oauth2/server-info` | Public | OAuth2 server info |
| `GET` | `/oauth2/test-direct` | Public | Test endpoint |
| `GET` | `/error` | Public | Error handling |

### Entities

```mermaid
erDiagram
    APP_USER {
        Long id PK
        string username UK
        string password
    }

    ROLE {
        Long id PK
        string name
    }

    USER_ROLES {
        Long user_id FK
        Long role_id FK
    }

    APP_USER ||--o{ USER_ROLES : has
    ROLE ||--o{ USER_ROLES : assigned
```

---

## 3. User Service

### Service Layers

```mermaid
graph TB
    subgraph "Controllers"
        UC["UserController\n/users"]
    end

    subgraph "Service Layer"
        USI["UserServiceImpl\n@Service"]
        US["UserService\ninterface"]
    end

    subgraph "Repository"
        UR["UserRepository\nCrudRepository"]
    end

    subgraph "Entities / DTOs"
        U["User\n@Entity @Table=users"]
        AR["AppResponse<T>\nGeneric DTO"]
    end

    subgraph "Kafka"
        UPS["UserProducerService\n@Service"]
        UM["UserMessage\nKafka DTO"]
        KPC["KafkaProducerConfig\n@Configuration"]
        UKI["UserKafkaInitializer\nApplicationRunner"]
    end

    subgraph "AOP"
        UOA["UserOperationAspect\n@Aspect"]
        AA["AuditAspect\n@Aspect"]
        ETA["ExecutionTimeAspect\n@Aspect"]
        VA["ValidationAspect\n@Aspect"]
    end

    subgraph "Config"
        ISF["InternalServiceFilter"]
        AC["AppConfig\nRestTemplate"]
        SC["SecurityConfig\nOAuth2 Resource Server"]
        AP["AopProperties\n@ConfigurationProperties"]
    end

    UC --> USI
    USI --> US
    USI --> UR
    USI --> UPS

    UPS --> UM
    UKI --> UR
    UKI --> UPS

    USI --> AA
    USI --> ETA
    USI --> VA
    UC --> UOA
```

### Endpoints

| Method | Path | Access | Description |
|--------|------|--------|-------------|
| `GET` | `/users/hello` | Public | Basic health check |
| `GET` | `/users/me` | USER | Get user by email |
| `POST` | `/users/me/create` | USER | Create user profile |
| `PUT` | `/users/me/update` | USER | Update profile |
| `DELETE` | `/users/me/delete` | USER | Delete profile |
| `GET` | `/users/me/current` | USER/ADMIN | Get current user |
| `GET` | `/users/find` | Internal | Find user by email |

### Entity

```mermaid
erDiagram
    USERS {
        Long id PK
        string firstName
        string lastName
        string email
        string address
    }
```

### Kafka Events

| Event | Topic | Description |
|--------|--------|-------------|
| `UserCreated` | `user` | User created |
| `UserUpdated` | `user` | User updated |
| `UserDeleted` | `user` | User deleted |

---

## 4. Product Service

### Service Layers

```mermaid
graph TB
    subgraph "Controllers"
        PC["ProductController\n/products"]
        CLC["ClothesController\n/products/clothes"]
        ELC["ElectronicsController\n/products/electronics"]
        SPC["SmartphoneController\n/products/smartphones"]
        KSC["KafkaSyncController\n/products/kafka/sync"]
    end

    subgraph "Service Layer"
        PSI["ProductServiceImpl"]
        CLSI["ClothesServiceImpl"]
        ELSI["ElectronicsServiceImpl"]
        SPSI["SmartphoneServiceImpl"]
    end

    subgraph "Repositories"
        PR["ProductRepository\nJpaRepository<Product>"]
        CLR["ClothesRepository\nJpaRepository<Clothes>"]
        ELR["ElectronicsRepository\nJpaRepository<Electronics>"]
        SPR["SmartphoneRepository\nJpaRepository<Smartphone>"]
    end

    subgraph "Entities"
        P["Product\n@Entity abstract\n@Inheritance JOINED"]
        CL["Clothes\n@Entity\nbrand, size, color, fabricType"]
        EL["Electronics\n@Entity abstract\nbrand, model, warrantyPeriod, specs"]
        SP["Smartphone\n@Entity\nOS, storage, RAM, processor, screenSize"]
    end

    subgraph "Kafka"
        PPS["ProductProducerService"]
        PM["ProductMessage\nKafka DTO"]
        PKC["KafkaProducerConfig"]
        PKI["ProductKafkaInitializer"]
    end

    subgraph "DTOs / Utils"
        AR2["AppResponse<T>"]
        PU["ProductUtils\nvalidations"]
        PE["ProductException"]
    end

    subgraph "AOP"
        POA["ProductOperationAspect\n@Aspect"]
        AA2["AuditAspect\n@Aspect"]
        ETA2["ExecutionTimeAspect\n@Aspect"]
        VA2["ValidationAspect\n@Aspect"]
    end

    PC --> PSI
    CLC --> CLSI
    ELC --> ELSI
    SPC --> SPSI

    PSI --> PR
    CLSI --> CLR
    ELSI --> ELR
    SPSI --> SPR

    P ---> CL
    P ---> EL
    EL ---> SP

    CLC --> PPS
    ELC --> PPS
    SPC --> PPS
    PKI --> PSI
    PKI --> PPS
```

### Endpoints

| Method | Path | Access | Description |
|--------|------|--------|-------------|
| `GET` | `/products` | Public | List all products |
| `GET` | `/products/categories` | Public | List categories |
| `POST` | `/products/clothes` | ADMIN | Create clothes |
| `GET` | `/products/clothes` | Public | List clothes |
| `GET` | `/products/clothes/{id}` | Public | Get clothes by ID |
| `PUT` | `/products/clothes/{id}` | ADMIN | Update clothes |
| `DELETE` | `/products/clothes/{id}` | ADMIN | Delete clothes |
| `POST` | `/products/electronics` | ADMIN | Create electronics |
| `GET` | `/products/electronics` | Public | List electronics |
| `GET` | `/products/electronics/{id}` | Public | Get electronics by ID |
| `PUT` | `/products/electronics/{id}` | ADMIN | Update electronics |
| `DELETE` | `/products/electronics/{id}` | ADMIN | Delete electronics |
| `POST` | `/products/smartphones` | ADMIN | Create smartphone |
| `GET` | `/products/smartphones` | Public | List smartphones |
| `GET` | `/products/smartphones/{id}` | Public | Get smartphone by ID |
| `PUT` | `/products/smartphones/{id}` | ADMIN | Update smartphone |
| `DELETE` | `/products/smartphones/{id}` | ADMIN | Delete smartphone |
| `POST` | `/products/kafka/sync/force-full-sync` | ADMIN | Force Kafka sync |
| `GET` | `/products/kafka/sync/status` | ADMIN | Sync status |

### Inheritance Hierarchy

```mermaid
classDiagram
    class Product {
        <<abstract>>
        #Long id
        #int stock
        #String name
        #Double price
        #String category
        #String imageUrl
    }

    class Clothes {
        -String brand
        -String size
        -String color
        -String fabricType
    }

    class Electronics {
        <<abstract>>
        -String brand
        -String model
        -String warrantyPeriod
        -String specifications
    }

    class Smartphone {
        -String operatingSystem
        -int storageCapacity
        -int ram
        -String processor
        -double screenSize
    }

    Product <|-- Clothes
    Product <|-- Electronics
    Electronics <|-- Smartphone
```

### Kafka Events

| Event | Topic | Description |
|--------|--------|-------------|
| `ProductCreated` | `product` | Product created |
| `ProductUpdated` | `product` | Product updated |
| `ProductDeleted` | `product` | Product deleted |

---

## 5. Order Service

### Service Layers

```mermaid
graph TB
    subgraph "Controllers"
        OC["OrderController\n/orders"]
    end

    subgraph "Service Layer"
        OSI["OrderServiceImpl"]
        ISI["ItemServiceImpl"]
        PSI2["ProductServiceImpl"]
        USI2["UserServiceImpl"]
        OVS["OrderValidationService\nasync validation"]
        PSS["ProductSyncService"]
        USS["UserSyncService"]
    end

    subgraph "Repositories"
        OR["OrderRepository\nJpaRepository"]
        IR["ItemRepository\nJpaRepository"]
        PR2["ProductRepository\nJpaRepository"]
        UR2["UserRepository\nCrudRepository"]
    end

    subgraph "Entities"
        O["Order\n@Entity @Table=orders"]
        I["Item\n@Entity @Table=items"]
        P2["Product\n@Entity @Table=products\n(read replica)"]
        U2["User\n@Entity @Table=users\n(read replica)"]
    end

    subgraph "DTOs"
        ODTO["OrderDTO"]
        IDTO["ItemDTO"]
        OM["OrderMapper\n@Component"]
        AR3["AppResponse<T>"]
    end

    subgraph "Kafka Consumers"
        PCS["ProductConsumerService\n@KafkaListener"]
        UCS["UserConsumerService\n@KafkaListener"]
        PM2["ProductMessage"]
        UM2["UserMessage"]
    end

    subgraph "Kafka Config"
        KCC["KafkaConsumerConfig\n@EnableKafka"]
    end

    subgraph "Config"
        ASC["AsyncConfig\nThreadPoolTaskExecutor"]
        APC["AppConfiguration\nWebClient.Builder"]
        SC2["SecurityConfig\nOAuth2 Resource Server"]
    end

    subgraph "AOP"
        OOA["OrderOperationAspect\n@Aspect"]
        AA3["AuditAspect\n@Aspect"]
        ETA3["ExecutionTimeAspect\n@Aspect"]
        VA3["ValidationAspect\n@Aspect"]
    end

    OC --> OSI
    OC --> OVS
    OC --> OM
    OSI --> OR
    OSI --> IR
    OSI --> OVS

    OVS --> PSS
    OVS --> USS

    PSS --> PR2
    USS --> UR2

    PCS --> PSS
    UCS --> USS

    O --> I
    O --> U2
    I --> P2
    U2 --> O
    P2 --> I
```

### Endpoints

| Method | Path | Access | Description |
|--------|------|--------|-------------|
| `GET` | `/orders/` | ADMIN | List all orders |
| `GET` | `/orders/find?id=` | ADMIN | Find order by ID |
| `GET` | `/orders/me` | USER | Authenticated user's orders |
| `POST` | `/orders/me` | USER | Create order |
| `PUT` | `/orders/me` | USER | Update order |
| `DELETE` | `/orders/me?id=` | USER | Delete order |

### Entities

```mermaid
erDiagram
    ORDERS {
        Long id PK
        Long user_id FK
        datetime createdAt
    }

    ITEMS {
        Long id PK
        Long order_id FK
        Long product_id FK
        int quantity
    }

    USERS {
        Long id PK
        string firstName
        string lastName
        string email
        string address
    }

    PRODUCTS {
        Long id PK
        string name
        double price
        int stock
        string category
        string imageUrl
        string brand
    }

    ORDERS ||--o{ ITEMS : contains
    ORDERS }o--|| USERS : belongs to
    ITEMS }o--|| PRODUCTS : references
```

---

## 6. Gateway

### Internal Architecture

```mermaid
graph LR
    subgraph "Spring Cloud Gateway"
        SC["SecurityConfig\nSecurityWebFilterChain"]
        GC["GatewayConfig\nRouteLocator"]
        JAF["JwtAuthenticationFilter\nAbstractGatewayFilterFactory"]
        JC["JwtConfig\nReactiveJwtDecoder"]
        GTC["GatewayTestController\n/gateway"]
        GCONST["GatewayConstants"]
    end

    subgraph "Routes"
        R1["/auth/** → auth-service"]
        R2["/api/** → lb://MICROSERVICE-NAME\n+ JwtAuthenticationFilter"]
    end

    subgraph "Downstream"
        A["Auth Service\n:8081"]
        U["User Service\n:9002"]
        P["Product Service\n:9003"]
        O["Order Service\n:9004"]
    end

    R1 --> A
    R2 --> U
    R2 --> P
    R2 --> O

    GC --> R1
    GC --> R2
    SC --> JAF
    JAF --> JC
```

### Health Endpoints

| Method | Path | Access | Description |
|--------|------|--------|-------------|
| `GET` | `/gateway/test` | USER/ADMIN | Auth test |
| `GET` | `/gateway/health` | ADMIN | Full health check |
| `GET` | `/gateway/health/public` | Public | Basic health check |
| `GET` | `/gateway/health/user` | USER/ADMIN | Health with user info |

### Route Map

| Public Route | Destination | Auth | Roles |
|---|---|---|---|
| `GET /product-service/products/**` | Product Service | Public | — |
| `GET /product-service/products` | Product Service | Public | — |
| `GET /user-service/users/hello` | User Service | Public | — |
| `/auth/**` | Auth Service | Public | — |
| `/actuator/**` | — | Public | — |
| `/gateway/health/public` | Gateway | Public | — |

| Protected Route | Destination | Roles |
|---|---|---|
| `POST/PUT/DELETE /product-service/**` | Product Service | ADMIN |
| `/user-service/users/me/**` | User Service | USER |
| `/order-service/orders/me/**` | Order Service | USER |
| `/order-service/orders` | Order Service | ADMIN |
| `/order-service/orders/find` | Order Service | ADMIN |
| `/gateway/health` | Gateway | ADMIN |
| `/gateway/health/user` | Gateway | USER/ADMIN |

---

## 7. Config Server & Discovery Server

```mermaid
graph TB
    subgraph "Config Server :8888"
        CSAPP["FpMicroConfigserverApplication\n@EnableConfigServer"]
        CSC["ConfigServerConstants\nCORS + matchers"]
        SSC["SecurityConfig\ncorsAndLocalhostFilter\n(restricts to localhost)"]
        CONFIG_REPO["Config Repo (Git)\n../fp_files_configproperties"]
    end

    subgraph "Discovery Server :8761"
        DSAPP["FpMicroDiscoveryserverApplication\n@EnableEurekaServer"]
    end

    subgraph "Services consuming config"
        GW2["Gateway"]
        AUTH2["Auth Service"]
        USER2["User Service"]
        PRODUCT2["Product Service"]
        ORDER2["Order Service"]
    end

    subgraph "Services registered in Eureka"
        AUTH_E["Auth Service"]
        USER_E["User Service"]
        PRODUCT_E["Product Service"]
        ORDER_E["Order Service"]
        GW_E["Gateway"]
    end

    CONFIG_REPO --> CSAPP
    CSAPP -->|"serves config"| GW2
    CSAPP -->|"serves config"| AUTH2
    CSAPP -->|"serves config"| USER2
    CSAPP -->|"serves config"| PRODUCT2
    CSAPP -->|"serves config"| ORDER2

    AUTH_E -.->|"register"| DSAPP
    USER_E -.->|"register"| DSAPP
    PRODUCT_E -.->|"register"| DSAPP
    ORDER_E -.->|"register"| DSAPP
    GW_E -.->|"register"| DSAPP
```

---

## 8. Frontend

### Component Architecture

```mermaid
graph TB
    subgraph "App (Router)"
        NAV["Navigation"]
        DASH["Dashboard"]
        PROD["ProductsPage"]
        ORD["OrdersPage"]
        USERPG["UserPage"]
        CALLBACK["CallbackPage"]
        ACCESS["AccessDenied"]
    end

    subgraph "Product Components"
        PL["ProductsList\nproducts + cart"]
        APF["AdminProductForm\ncreate product"]
        APM["AdminProductsManager\nedit/delete"]
    end

    subgraph "Order Components"
        OC2["OrderCard\nitems + total"]
        OSM["OrderSelectionModal\nchoose order"]
        OTC["OrderTestComponent\ndebug"]
    end

    subgraph "User Components"
        UP["UserProfile\nprofile form"]
    end

    subgraph "Hooks"
        UA["useAuth\ntoken lifecycle"]
        UUR["useUserRole\nrole from JWT"]
        UU["useUser\nuser data"]
        UO["useOrdersStore\norders + store"]
    end

    subgraph "Services"
        AUTH_S["auth.ts\nOAuth2 + PKCE"]
        OS["orderService.ts\nAPI orders"]
        US["userService.ts\nAPI users"]
    end

    subgraph "Store"
        OS_STORE["ordersStore.ts\nsingleton + subscribers"]
    end

    subgraph "Config"
        CONFIG["config.ts\nendpoint URLs"]
    end

    DASH --> UA
    PROD --> PL
    PROD --> APF
    PROD --> APM

    ORD --> OC2
    ORD --> OSM
    ORD --> OTC
    ORD --> UO

    USERPG --> UU
    USERPG --> UP

    CALLBACK --> UA
    CALLBACK --> AUTH_S

    PL --> UO
    PL --> OS_STORE

    UA --> AUTH_S
    UUR --> UA
    UU --> US
    UO --> OS_STORE
    OS_STORE --> OS
    OS_STORE --> US

    AUTH_S --> CONFIG
    OS --> CONFIG
    US --> CONFIG
```

### Routing

```mermaid
graph LR
    START["/"] -->|redirect| DASH2["/dashboard"]
    CALLBACK2["/callback"] -->|"OAuth2 redirect"| DASH2
    DASH2 -->|"admin"| ADMIN_VIEW["Admin Panel"]
    DASH2 -->|"user"| USER_VIEW["User Panel"]
    PROD2["/products"] --> BROWSE["Browse Products"]
    PROD2 -->|"admin"| CREATE["Create Product"]
    PROD2 -->|"admin"| MANAGE["Manage Products"]
    ORD2["/orders"] -->|"USER only"| ORDER_VIEW["My Orders"]
    PROFILE["/profile"] -->|"USER only"| PROFILE_VIEW["My Profile"]
```

### External Services (API Calls)

```mermaid
sequenceDiagram
    participant F as Frontend :9001
    participant G as Gateway :8080
    participant A as Auth Server :8081
    participant P as Product Service
    participant U as User Service
    participant O as Order Service

    Note over F,A: OAuth2 Login
    F->>A: GET /oauth2/authorize (PKCE)
    A-->>F: redirect /callback (code)
    F->>A: POST /oauth2/token (code → tokens)
    A-->>F: access_token + id_token

    Note over F,P: Products
    F->>G: GET /api/product-service/products
    G->>P: GET /products
    P-->>G: Product[]
    G-->>F: JSON

    Note over F,U: User
    F->>G: GET /api/user-service/users/me
    G->>U: GET /users/me
    U-->>G: User
    G-->>F: JSON

    Note over F,O: Orders
    F->>G: POST /api/order-service/orders/me
    G->>O: POST /orders/me
    O-->>G: Order
    G-->>F: JSON
```

---

## 9. Entity Relationship Diagram (ERD)

### Cross-Service Overview

```mermaid
erDiagram
    APP_USER {
        Long id PK
        string username UK
        string password
    }

    ROLE {
        Long id PK
        string name
    }

    USER_ROLES {
        Long user_id FK
        Long role_id FK
    }

    USERS {
        Long id PK
        string firstName
        string lastName
        string email UK
        string address
    }

    PRODUCT {
        Long id PK
        int stock
        string name
        double price
        string category
        string imageUrl
    }

    CLOTHES {
        Long id PK FK
        string brand
        string size
        string color
        string fabricType
    }

    ELECTRONICS {
        Long id PK FK
        string brand
        string model
        string warrantyPeriod
        string specifications
    }

    SMARTPHONE {
        Long id PK FK
        string operatingSystem
        int storageCapacity
        int ram
        string processor
        double screenSize
    }

    ORDERS {
        Long id PK
        Long user_id FK
        datetime createdAt
    }

    ITEMS {
        Long id PK
        Long order_id FK
        Long product_id FK
        int quantity
    }

    APP_USER ||--o{ USER_ROLES : has
    ROLE ||--o{ USER_ROLES : assigned

    ORDERS ||--o{ ITEMS : contains
    ORDERS }o--|| USERS : belongs_to
    ITEMS }o--|| PRODUCTS : references

    PRODUCT ||--|| CLOTHES : is_a
    PRODUCT ||--|| ELECTRONICS : is_a
    ELECTRONICS ||--|| SMARTPHONE : is_a
```

### Table Notes

| Table | Database | Service | Purpose |
|-------|----------|---------|---------|
| `app_user` | MySQL (auth db) | Auth Service | Authentication system users |
| `role` | MySQL (auth db) | Auth Service | Roles (ROLE_USER, ROLE_ADMIN) |
| `user_roles` | MySQL (auth db) | Auth Service | User-role join table |
| `users` | MySQL (user db) | User Service | User profiles |
| `product` | MySQL (product db) | Product Service | Parent product table (JOINED) |
| `clothes` | MySQL (product db) | Product Service | Product subclass: clothes |
| `electronics` | MySQL (product db) | Product Service | Product subclass: electronics |
| `smartphone` | MySQL (product db) | Product Service | Electronics subclass: smartphones |
| `orders` | MySQL (order db) | Order Service | Purchase orders |
| `items` | MySQL (order db) | Order Service | Order line items |
| `users` (replica) | MySQL (order db) | Order Service | Local user replica via Kafka |
| `products` (replica) | MySQL (order db) | Order Service | Local product replica via Kafka |

---

## 10. Kafka Event Flow

```mermaid
graph LR
    subgraph "Producers"
        UPS["User Producer\nUser Service"]
        PPS["Product Producer\nProduct Service"]
    end

    subgraph "Kafka"
        T_USER["Topic: user"]
        T_PRODUCT["Topic: product"]
    end

    subgraph "Consumers"
        UCS2["User Consumer\nOrder Service"]
        PCS2["Product Consumer\nOrder Service"]
    end

    subgraph "Local Replicas (Order DB)"
        USERS_REPLICA["users\n(local table)"]
        PRODUCTS_REPLICA["products\n(local table)"]
    end

    UPS -->|"UserCreated\nUserUpdated\nUserDeleted"| T_USER
    PPS -->|"ProductCreated\nProductUpdated\nProductDeleted"| T_PRODUCT

    T_USER -->|"consume"| UCS2
    T_PRODUCT -->|"consume"| PCS2

    UCS2 -->|"sync"| USERS_REPLICA
    PCS2 -->|"sync"| PRODUCTS_REPLICA
```

### Initialization

```mermaid
sequenceDiagram
    participant PPS as Product Service
    participant US as User Service
    participant K as Kafka
    participant OS as Order Service

    Note over PPS,OS: Startup
    PPS->>K: sendInitialProductList(all products)
    US->>K: sendInitialUserLoad(all users)
    K->>OS: ProductMessage[eventType=CREATED]
    K->>OS: UserMessage[eventType=CREATED]
    OS->>OS: saveOrUpdateProduct()
    OS->>OS: processUserMessage()
```

---

## 11. OAuth2 Authentication Flow

```mermaid
sequenceDiagram
    participant F as Frontend :9001
    participant A as Auth Server :8081
    participant G as Gateway :8080
    participant S as Microservice

    Note over F,A: 1. Login
    F->>F: generate PKCE codeVerifier + codeChallenge
    F->>A: GET /oauth2/authorize?response_type=code&client_id=fp_frontend&redirect_uri=/callback&code_challenge=...
    A-->>F: Login page
    F->>A: POST credentials
    A-->>F: redirect /callback?code=AUTHORIZATION_CODE

    Note over F,A: 2. Token Exchange
    F->>A: POST /oauth2/token?grant_type=authorization_code&code=...&code_verifier=...&redirect_uri=/callback
    A-->>F: { access_token, id_token, refresh_token }
    F->>F: store tokens in sessionStorage

    Note over F,S: 3. API Calls (via Gateway)
    F->>G: GET /api/... Authorization: Bearer access_token
    G->>G: validate JWT signature (JWK Set URL from Auth Server)
    G->>G: extract roles from JWT claims
    G->>S: forward request + X-User-Id + X-User-Roles headers
    S-->>G: response
    G-->>F: response

    Note over F,S: 4. Token Expiry
    F->>F: check expiry every 60s
    F->>A: POST /oauth2/token?grant_type=refresh_token&refresh_token=...
    A-->>F: { access_token, id_token, refresh_token }
```

---

## Port and Service Summary

| Service | Internal Port | Host Port | Docker Name |
|---------|--------------|-----------|-------------|
| Config Server | 8888 | 8888 | config-server |
| Discovery Server | 8761 | 8761 | discovery-server |
| Gateway | 8080 | 8080 | gateway |
| Auth Service | 8081 | 8081 | auth-service |
| User Service | 8080 | 9002 | user-service |
| Product Service | 8080 | 9003 | product-service |
| Order Service | 8080 | 9004 | order-service |
| Frontend | 9001 | 9001 | frontend |
| MySQL | 3306 | 3306 | mysql |
| Kafka | 9092 | 9092 | kafka |
