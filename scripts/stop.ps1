# Скрипт безопасной остановки всех контейнеров с сохранением данных в volumes
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Остановка Kaspi Reactive Credit Scoring Pipeline" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$rootDir = Split-Path -Parent $scriptDir
Set-Location $rootDir

Write-Host "Остановка и удаление контейнеров (без удаления томов БД)..." -ForegroundColor Yellow
docker compose down

Write-Host "`nВсе контейнеры успешно остановлены." -ForegroundColor Green
Write-Host "Все данные в PostgreSQL, топиках Kafka и Redis сохранены." -ForegroundColor Cyan
