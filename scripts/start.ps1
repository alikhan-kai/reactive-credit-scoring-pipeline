# Скрипт сборки и безопасного запуска всех сервисов платформы скоринга
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Запуск Kaspi Reactive Credit Scoring Pipeline" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$rootDir = Split-Path -Parent $scriptDir
Set-Location $rootDir

Write-Host "`n1. Проверка артефактов микросервисов..." -ForegroundColor Yellow
$needBuild = $false
$services = @("credit-gateway-service", "scoring-orchestrator-service", "external-mock-service", "credit-agreement-service", "audit-notification-service")

foreach ($svc in $services) {
    $jars = Get-ChildItem -Path "$rootDir\$svc\build\libs\*.jar" -ErrorAction SilentlyContinue
    if (-not $jars) {
        $needBuild = $true
        break
    }
}

if ($needBuild) {
    Write-Host "Обнаружены отсутствующие jar-файлы. Запуск сборки Gradle..." -ForegroundColor Yellow
    .\gradlew.bat bootJar -x test
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Ошибка при сборке проекта через Gradle." -ForegroundColor Red
        exit $LASTEXITCODE
    }
} else {
    Write-Host "Все jar-артефакты собраны." -ForegroundColor Green
}

Write-Host "`n2. Запуск контейнеров через Docker Compose..." -ForegroundColor Yellow
docker compose up -d

Write-Host "`n3. Статус запуска контейнеров:" -ForegroundColor Yellow
docker compose ps

Write-Host "`nПлатформа запущена и готова к обработке кредитных заявок!" -ForegroundColor Green
Write-Host "- Входной API Шлюз:  http://localhost:8080/v1/credit/apply" -ForegroundColor White
Write-Host "- Kafka UI:          http://localhost:8088" -ForegroundColor White
Write-Host "- Grafana:           http://localhost:3000 (admin / admin)" -ForegroundColor White
Write-Host "- Prometheus:        http://localhost:9090" -ForegroundColor White
Write-Host "- Zipkin Tracing:    http://localhost:9411" -ForegroundColor White
