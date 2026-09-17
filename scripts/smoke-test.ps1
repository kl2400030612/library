$ErrorActionPreference = 'Stop'
$root = 'http://localhost'

function Get-WithRetry($url, $headers = @{}, $attempts = 15) {
  for ($i=1; $i -le $attempts; $i++) {
    try { return Invoke-RestMethod -Uri $url -Headers $headers -Method Get -TimeoutSec 5 }
    catch { if ($i -eq $attempts) { throw }; Start-Sleep -Seconds ([Math]::Min($i,3)) }
  }
}
function Post-WithRetry($url, $body, $headers = @{}, $attempts = 15) {
  for ($i=1; $i -le $attempts; $i++) {
    try { return Invoke-RestMethod -Uri $url -Headers $headers -Method Post -ContentType 'application/json' -Body ($body | ConvertTo-Json -Compress) -TimeoutSec 5 }
    catch { if ($i -eq $attempts) { throw }; Start-Sleep -Seconds ([Math]::Min($i,3)) }
  }
}

Write-Host 'Archvialia runtime smoke test' -ForegroundColor Cyan
Get-WithRetry "${root}:8761/eureka/apps" | Out-Null
Write-Host '[PASS] Eureka reachable'
Get-WithRetry "${root}:8080/actuator/health" | Out-Null
Write-Host '[PASS] Gateway health reachable'
Invoke-WebRequest -Uri "${root}:5173" -UseBasicParsing | Out-Null
Write-Host '[PASS] Frontend reachable'

$student = Post-WithRetry "${root}:8080/api/auth/login" @{email='user@archvialia.local';password='User@12345'}
$studentHeaders = @{Authorization="Bearer $($student.token)"}
Write-Host '[PASS] Student login'
Get-WithRetry "${root}:8080/api/books" $studentHeaders | Out-Null
Write-Host '[PASS] Student can read catalogue'
Get-WithRetry "${root}:8080/api/borrow/me" $studentHeaders | Out-Null
Write-Host '[PASS] Student can read own borrowings'
Get-WithRetry "${root}:8080/api/fines/me" $studentHeaders | Out-Null
Write-Host '[PASS] Student can read own fines'

$books = Get-WithRetry "${root}:8080/api/books" $studentHeaders
$bookId = $books[0].id
$before = (Get-WithRetry "${root}:8080/api/books/$bookId/availability" $studentHeaders).availableCopies
$borrow = Post-WithRetry "${root}:8080/api/borrow" @{bookId=$bookId} $studentHeaders
Write-Host '[PASS] Student can borrow an available book'
$afterBorrow = (Get-WithRetry "${root}:8080/api/books/$bookId/availability" $studentHeaders).availableCopies
if ($afterBorrow -ne ($before - 1)) { throw "Availability did not decrease after borrow. Before=$before After=$afterBorrow" }
Write-Host '[PASS] Availability decreases after borrow'
$returned = Invoke-RestMethod -Uri "${root}:8080/api/borrow/$($borrow.id)/return" -Headers $studentHeaders -Method Put -TimeoutSec 5
Write-Host '[PASS] Student can return the borrowed book'
$afterReturn = (Get-WithRetry "${root}:8080/api/books/$bookId/availability" $studentHeaders).availableCopies
if ($afterReturn -ne $before) { throw "Availability was not restored after return. Before=$before AfterReturn=$afterReturn" }
Write-Host '[PASS] Availability is restored after return'

$facultyEmail = "v11-smoke-$([DateTimeOffset]::UtcNow.ToUnixTimeSeconds())@example.local"
$faculty = Post-WithRetry "${root}:8080/api/auth/register" @{name='V11 Smoke Faculty';email=$facultyEmail;password='Faculty@12345';role='FACULTY'}
Write-Host '[PASS] Faculty registration'
$facultyHeaders = @{Authorization="Bearer $($faculty.token)"}
Get-WithRetry "${root}:8080/api/books" $facultyHeaders | Out-Null
Write-Host '[PASS] Faculty can read catalogue'

$admin = Post-WithRetry "${root}:8080/api/auth/login" @{email='admin@archvialia.local';password='Admin@12345'}
$adminHeaders = @{Authorization="Bearer $($admin.token)"}
Write-Host '[PASS] Admin login'
Get-WithRetry "${root}:8080/api/auth/admin/users" $adminHeaders | Out-Null
Get-WithRetry "${root}:8080/api/borrow/admin/all" $adminHeaders | Out-Null
Get-WithRetry "${root}:8080/api/fines/admin/all" $adminHeaders | Out-Null
Write-Host '[PASS] Admin user/loan/fine access'

Write-Host 'Core circulation workflow verified: borrow → availability decrement → return → availability restore.' -ForegroundColor Green

Write-Host 'Archvialia runtime smoke test completed.' -ForegroundColor Green
