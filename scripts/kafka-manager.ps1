# Script para gestionar Kafka con Docker para el proyecto FP
# Uso: .\kafka-manager.ps1 [start|stop|restart|status|logs|topics]

param(
    [Parameter(Position=0)]
    [ValidateSet("start", "stop", "restart", "status", "logs", "topics", "create-topics", "help")]
    [string]$Action = "help"
)

$ComposeFile = "docker-compose.yml"

function ShowHelp {
    Write-Host "=== Kafka Manager para Proyecto FP ===" -ForegroundColor Green
    Write-Host ""
    Write-Host "Comandos disponibles:" -ForegroundColor Yellow
    Write-Host "  start         - Inicia Kafka y Zookeeper"
    Write-Host "  stop          - Detiene Kafka y Zookeeper"
    Write-Host "  restart       - Reinicia los servicios"
    Write-Host "  status        - Muestra el estado de los contenedores"
    Write-Host "  logs          - Muestra los logs de Kafka"
    Write-Host "  topics        - Lista los topics existentes"
    Write-Host "  create-topics - Crea los topics necesarios para los microservicios"
    Write-Host "  help          - Muestra esta ayuda"
    Write-Host ""
    Write-Host "Ejemplo: .\kafka-manager.ps1 start" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "URLs útiles:" -ForegroundColor Yellow
    Write-Host "  Kafka UI: http://localhost:9090"
    Write-Host "  Kafka Bootstrap Servers: localhost:9092"
}

function StartKafka {
    Write-Host "Iniciando Kafka y Zookeeper..." -ForegroundColor Green
    docker-compose -f $ComposeFile up -d
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Kafka iniciado correctamente" -ForegroundColor Green
        Write-Host "Kafka UI disponible en: http://localhost:9090" -ForegroundColor Cyan
        Write-Host "Bootstrap Servers: localhost:9092" -ForegroundColor Cyan

        # Esperar a que Kafka esté listo
        Write-Host "Esperando que Kafka este listo..." -ForegroundColor Yellow
        Start-Sleep -Seconds 10
        
        # Crear topics automáticamente
        CreateTopics
    } else {
        Write-Host "Error al iniciar Kafka" -ForegroundColor Red
    }
}

function StopKafka {
    Write-Host "Deteniendo Kafka y Zookeeper..." -ForegroundColor Yellow
    docker-compose -f $ComposeFile down
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Kafka detenido correctamente" -ForegroundColor Green
    } else {
        Write-Host "Error al detener Kafka" -ForegroundColor Red
    }
}

function RestartKafka {
    Write-Host "Reiniciando Kafka..." -ForegroundColor Yellow
    StopKafka
    Start-Sleep -Seconds 5
    StartKafka
}

function ShowStatus {
    Write-Host "Estado de los contenedores Kafka:" -ForegroundColor Green
    docker-compose -f $ComposeFile ps
    Write-Host ""
    Write-Host "Verificando conectividad..." -ForegroundColor Yellow
    docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --list 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Kafka está funcionando correctamente" -ForegroundColor Green
    } else {
        Write-Host "Kafka no responde" -ForegroundColor Red
    }
}

function ShowLogs {
    Write-Host "Mostrando logs de Kafka (Ctrl+C para salir):" -ForegroundColor Green
    docker-compose -f $ComposeFile logs -f kafka
}

function ListTopics {
    Write-Host "Topics existentes en Kafka:" -ForegroundColor Green
    docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --list
}

function CreateTopics {
    Write-Host "Creando topics necesarios para los microservicios..." -ForegroundColor Green
    
    $topics = @(
        "product",
        "user",
        "order"
    )
    
    foreach ($topic in $topics) {
        Write-Host "   Creando topic: $topic" -ForegroundColor Cyan
        docker exec fp-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic $topic --partitions 3 --replication-factor 1 --if-not-exists
    }
    
    Write-Host "Topics creados correctamente" -ForegroundColor Green
    Write-Host ""
    ListTopics
}

# Verificar si Docker está ejecutándose
try {
    docker version | Out-Null
} catch {
    Write-Host "Docker no está ejecutándose o no está instalado" -ForegroundColor Red
    exit 1
}

# Verificar si docker-compose existe
if (!(Test-Path $ComposeFile)) {
    Write-Host "No se encuentra el archivo $ComposeFile" -ForegroundColor Red
    Write-Host "   Asegúrate de ejecutar este script desde el directorio del proyecto" -ForegroundColor Yellow
    exit 1
}

# Ejecutar acción
switch ($Action) {
    "start" { StartKafka }
    "stop" { StopKafka }
    "restart" { RestartKafka }
    "status" { ShowStatus }
    "logs" { ShowLogs }
    "topics" { ListTopics }
    "create-topics" { CreateTopics }
    "help" { ShowHelp }
    default { ShowHelp }
}