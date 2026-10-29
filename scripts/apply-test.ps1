# Скрипт отправки тестовой кредитной заявки на шлюз с красивым выводом
param(
    [string]$Iin = "950315350127",
    [double]$Amount = 500000,
    [int]$TermMonths = 12,
    [string]$Product = "INSTALLMENT_0_0_12",
    [string]$Phone = "+77011234567"
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Подача кредитной заявки в Kaspi Scoring Pipeline" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$idempotencyKey = "manual-test-" + [Guid]::NewGuid().ToString().Substring(0, 8)

$bodyObj = @{
    iin = $Iin
    requestedAmount = $Amount
    termMonths = $TermMonths
    productType = $Product
    phoneNumber = $Phone
}

$jsonBody = $bodyObj | ConvertTo-Json

Write-Host "Параметры заявки:" -ForegroundColor Yellow
Write-Host "- ИИН:              $Iin" -ForegroundColor White
Write-Host "- Сумма:            $Amount ₸" -ForegroundColor White
Write-Host "- Срок:             $TermMonths мес." -ForegroundColor White
Write-Host "- Продукт:          $Product" -ForegroundColor White
Write-Host "- Ключ идемпотент.: $idempotencyKey" -ForegroundColor DarkGray
Write-Host ""

try {
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $response = Invoke-RestMethod -Uri "http://localhost:8080/v1/credit/apply" `
        -Method Post `
        -Headers @{
            "Content-Type" = "application/json"
            "X-Idempotency-Key" = $idempotencyKey
        } `
        -Body $jsonBody
    $stopwatch.Stop()

    Write-Host "Ответ скоринговой платформы (Время: $($stopwatch.ElapsedMilliseconds) ms):" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 5
} catch {
    Write-Host "Ошибка при отправке заявки:" -ForegroundColor Red
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $errBody = $reader.ReadToEnd()
        Write-Host $errBody -ForegroundColor Red
    } else {
        Write-Host $_.Exception.Message -ForegroundColor Red
    }
}
