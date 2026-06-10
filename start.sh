#!/usr/bin/env bash
set -e

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
CONFIG_REPO_DIR="$(dirname "$ROOT_DIR")/Ecommerce_files_configproperties"

echo "========================================="
echo "  Ecommerce Microservices Platform"
echo "========================================="
echo ""

# Ensure config repo exists
if [ ! -d "$CONFIG_REPO_DIR" ]; then
    echo "Cloning config repository..."
    git clone https://github.com/BrunoGilRamirez/Ecommerce_files_configproperties.git "$CONFIG_REPO_DIR"
else
    echo "[OK] Config repo found at $CONFIG_REPO_DIR"
fi

echo ""
echo "--- Building Docker images ---"
cd "$ROOT_DIR"
docker compose build 2>&1 | sed 's/^/  /'

echo ""
echo "--- Starting all services ---"
docker compose up -d --force-recreate --remove-orphans 2>&1 | sed 's/^/  /'

echo ""
echo "--- Waiting for services ---"
echo "  Waiting for MySQL..."
until docker exec fp-mysql mysqladmin ping -h localhost -u root -prootpassword --silent 2>/dev/null; do
    sleep 2
done
echo "  [OK] MySQL ready"

echo "  Waiting for config-server..."
for i in $(seq 1 30); do
    if curl -s http://localhost:8888/actuator/health >/dev/null 2>&1; then
        echo "  [OK] config-server ready"
        break
    fi
    [ "$i" -eq 30 ] && echo "  [!!] config-server not ready"
    sleep 2
done

echo "  Waiting for discovery-server..."
for i in $(seq 1 30); do
    if curl -s http://localhost:8761/eureka/apps >/dev/null 2>&1; then
        echo "  [OK] discovery-server ready"
        break
    fi
    [ "$i" -eq 30 ] && echo "  [!!] discovery-server not ready"
    sleep 2
done

echo ""
echo "========================================="
echo "  All services should be starting up"
echo "========================================="
echo ""
echo "  Frontend:  http://localhost:3000"
echo "  Gateway:   http://localhost:8080"
echo "  Eureka:    http://localhost:8761"
echo "  Config:    http://localhost:8888"
echo "  Kafka UI:  http://localhost:9090"
echo "  MySQL:     localhost:3306"
echo ""
echo "  View logs:  docker compose logs -f"
echo "  Stop all:   bash stop.sh"
echo "========================================="
