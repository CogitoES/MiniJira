# start_services.ps1
# Starts Eureka Discovery Server first, waits for it to initialize, then starts all other services in new windows

$rootGradlew = Join-Path (Get-Location) "gradlew.bat"
$envFile = Join-Path (Get-Location) ".env"

# Load .env variables into an array of command strings
$envCommands = @()
if (Test-Path $envFile) {
    Write-Host "Loading .env variables..." -ForegroundColor Cyan
    Get-Content $envFile | Where-Object { $_ -match '=' -and $_ -notmatch '^#' } | ForEach-Object {
        $key, $value = $_.Split('=', 2)
        $envCommands += "`$env:$($key.Trim()) = '$($value.Trim().Trim('"'))'"
    }
}

$envCommandString = if ($envCommands.Count -gt 0) { ($envCommands -join '; ') + '; ' } else { "" }

# 1. Start Eureka Discovery Server first
$discoveryDir = "services/discovery-server"
if (Test-Path $discoveryDir) {
    $absDir = Convert-Path $discoveryDir
    $logFile = Join-Path $absDir "service.log"
    Write-Host "Starting Eureka Discovery Server in $discoveryDir... (Logs: $logFile)" -ForegroundColor Cyan
    
    $fullCommand = "$envCommandString Set-Location '$absDir'; & '$rootGradlew' bootRun | Tee-Object -FilePath '$logFile'"
    Start-Process powershell.exe -ArgumentList "-NoExit", "-Command", "$fullCommand"
    
    Write-Host "Waiting 8 seconds for Eureka Discovery Server to initialize..." -ForegroundColor Yellow
    Start-Sleep -Seconds 8
}

# 2. Start remaining microservices
$serviceDirs = @(
    "services/auth-service",
    "services/comment-service",
    "services/gateway",
    "services/jira-service",
    "services/project-service",
    "services/task-service"
)

Write-Host "Starting remaining microservices..." -ForegroundColor Green

foreach ($dir in $serviceDirs) {
    if (Test-Path $dir) {
        $absDir = Convert-Path $dir
        $logFile = Join-Path $absDir "service.log"
        Write-Host "Starting service in $dir... (Logs: $logFile)" -ForegroundColor Cyan
        
        $fullCommand = "$envCommandString Set-Location '$absDir'; & '$rootGradlew' bootRun | Tee-Object -FilePath '$logFile'"
        Start-Process powershell.exe -ArgumentList "-NoExit", "-Command", "$fullCommand"
        
        Write-Host "Started service in $dir." -ForegroundColor Gray
    } else {
        Write-Host "Directory $dir not found, skipping." -ForegroundColor Yellow
    }
}

Write-Host "All services startup processes have been initiated in new windows." -ForegroundColor Green
