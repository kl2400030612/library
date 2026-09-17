$ErrorActionPreference = 'Stop'
Write-Host 'Archvialia V10 preflight'
docker compose config --quiet
Write-Host '[OK] Compose configuration'
$ps = docker compose ps --format json | ConvertFrom-Json
Write-Host "[INFO] Current containers: $($ps.Count)"
Write-Host 'Build all images with: docker compose build --progress=plain'
Write-Host 'Start stack with: docker compose up -d'
Write-Host 'Verify services with: docker compose ps'
Write-Host 'Verify Eureka with: curl http://localhost:8761/eureka/apps'
Write-Host 'Verify gateway with: curl http://localhost:8080/actuator/health'
Write-Host 'Verify frontend with: curl http://localhost:5173'
