# Скрипт быстрого просмотра последних кредитных договоров и их статусов из PostgreSQL
param(
    [int]$Limit = 5
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Последние оформленные кредитные договоры (PostgreSQL)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$query = "SELECT id, application_id, iin, product_type, approved_amount, monthly_payment, status, created_at FROM credit_agreements ORDER BY created_at DESC LIMIT $Limit;"

docker exec -it kaspi-postgres psql -U kaspi_admin -d credit_db -c "$query"
