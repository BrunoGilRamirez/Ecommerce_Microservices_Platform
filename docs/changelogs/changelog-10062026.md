# Changelog

All notable changes to the Ecommerce Microservices Platform.

## [Unreleased] — 2026-06-10

### Added
- **Monorepo creation**: Merged 9 individual microservice repositories into a single monorepo using `git read-tree`, preserving full Git history from each source repository.
- **Java 17 setup**: Installed Eclipse Temurin JDK 17.0.19+10 at `/home/codespace/java/jdk-17.0.19+10` and set as default Java.
- **`start.sh`**: Script that clones config repo if missing, builds all Docker images, starts all 12 containers in dependency order, and waits for healthy services.
- **`stop.sh`**: Script that stops and removes all containers.
- **Dockerfiles**: Created multi-stage Dockerfiles for all 7 Java services (`config-server`, `discovery-server`, `auth-service`, `user-service`, `product-service`, `order-service`, `gateway`) using `maven:3.9-eclipse-temurin-17` build stage and `eclipse-temurin:17-jre` runtime.
- **Dockerfile (Frontend)**: Created for the React app using `node:20-alpine`.
- **`docker-compose.yml`**: Single orchestration file defining 12 services (7 Java services, frontend, MySQL, Zookeeper, Kafka, Kafka UI) with healthchecks, host networking for Java services, bind mount for config repo, and startup ordering.
- **Bind mount**: Config Server mounts `../Ecommerce_files_configproperties:/fp_files_configproperties` to serve centralized properties.
- **Healthchecks**: Added to `config-server` (curl actuator) and `mysql` (mysqladmin ping) with `depends_on: condition: service_healthy`.
- **MySQL init script**: `infrastructure/mysql/init/001-init.sql` creates 4 databases (`auth_db`, `user_db`, `product_db`, `order_db`) with dedicated users.
- **`.gitignore`**: Updated to exclude `logs/`, `.pids`, `*.log`, `**/target/`, `**/node_modules/`, and `infrastructure/fp_files_configproperties`.
- **Documentation (6 files)**:
  - `01-architecture.md` — System architecture overview, service communication (HTTP + Kafka), technology stack.
  - `02-container-migration.md` — Migration from Maven-native to Docker, multi-stage builds, WAR packaging, host networking rationale, healthchecks, startup flow.
  - `03-configuration.md` — Config repo structure, per-service properties table, Spring profiles, MySQL databases and credentials.
  - `04-usage-guide.md` — Quick start, service administration commands, troubleshooting guide, file structure, service URLs.
  - `05-docker-reference.md` — Image/container/volume/network inventory, environment variables, useful Docker commands.
  - `06-project-schema.md` — Full project schema with Mermaid diagrams: architecture overview, service layers, entity relationships, Kafka event flow, OAuth2 authentication flow, port summary.

### Changed
- **WAR packaging**: All `pom.xml` files confirmed to use `<packaging>war</packaging>`; Dockerfiles updated from `*.jar` to `*.war`.
- **Spring profile**: All Java services now run with `SPRING_PROFILES_ACTIVE=dev` to load config from Config Server.
- **Doc filenames**: Translated from Spanish to English (e.g., `01-arquitectura.md` → `01-architecture.md`).
- **Doc content**: All documentation translated from Spanish to English.

### Fixed
- **Startup ordering**: Config Server and MySQL failures caused cascading service crashes; added healthchecks with retries to ensure dependencies are healthy before dependent services start.
- **Config Server startup**: Some services started before Config Server was ready, causing `configserver` connection errors. Fixed by adding `depends_on: condition: service_healthy` on Config Server.
- **Infrastructure filtering**: Fixed `InternalServiceFilter` in user-service to handle internal service requests correctly during startup.
- **README.md frontend references**: Updated incorrect port references in frontend documentation.
- **CORS configuration**: Updated allowed origins in Gateway, Auth Service, and Config Server to include all service URLs.

### Infrastructure
- **Docker networking**: All Java services use `network_mode: host` instead of bridge networking to preserve `localhost` URLs in configuration files without modification.
- **Config repository**: Cloned `Ecommerce_files_configproperties` from GitHub as centralized configuration source for all services.
- **Workspace cleanup**: Removed duplicate `apps/`, `infrastructure/`, `services/` directories from workspace root; removed redundant `fp_files_configproperties/`; removed stray `.git/` at root level (real Git repo is inside the monorepo).

### Technical Debt
- Host networking limits portability to Linux hosts. Cross-platform support would require Docker networking and replacing `localhost` references with service names.
- Config Server's `file:///` Git URI depends on bind mount from host; a remote Git URI would be more portable.
- Frontend connects to Auth Server directly (`localhost:8081`) instead of through the Gateway, bypassing centralized auth routing.
