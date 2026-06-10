#!/usr/bin/env bash
set -e

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Stopping all services..."

cd "$ROOT_DIR"
docker compose down 2>&1 | sed 's/^/  /'

echo ""
echo "All services stopped."
