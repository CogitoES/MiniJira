# start_services.ps1
# Starts all services in the services directory in new windows, loading .env variables

$serviceDirs = @(
    "services/auth-service",
    "services/comment-service",
    "services/gateway",
    "services/jira-service",
    "services/project-service",
    "services/task-service"
)

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

# Add diagnostic commands to verify env vars
$diagCommands = @("Write-Host '--- Environment Variables ---' -ForegroundColor Yellow")
foreach ($key in ($envCommands | ForEach-Object { $_.Split(':')[1].Split('=')[0].Trim() })) {
    $diagCommands += "Write-Host '$key = '`$env:$key"
}
$diagCommands += "Write-Host '-----------------------------' -ForegroundColor Yellow"

$envCommandString = if ($envCommands.Count -gt 0) { ($envCommands + $diagCommands -join '; ') + '; ' } else { "" }

Write-Host "Starting all services..." -ForegroundColor Green

foreach ($dir in $serviceDirs) {
    if (Test-Path $dir) {
        $absDir = Convert-Path $dir
        $logFile = Join-Path $absDir "service.log"
        Write-Host "Starting service in $dir... (Logs: $logFile)" -ForegroundColor Cyan
        
        # Construct the full command: Set env, Set Location, Run Gradle
        $fullCommand = "$envCommandString Set-Location '$absDir'; & '$rootGradlew' bootRun | Tee-Object -FilePath '$logFile'"
        
        # Use Start-Process with a new window
        Start-Process powershell.exe -ArgumentList "-NoExit", "-Command", "$fullCommand"
        
        Write-Host "Started service in $dir." -ForegroundColor Gray
    } else {
        Write-Host "Directory $dir not found, skipping." -ForegroundColor Yellow
    }
}

Write-Host "All services startup processes have been initiated in new windows." -ForegroundColor Green
