# test_all.ps1
# Runs all tests in the repository by iterating through all services.

$serviceDirs = @(
    "services/auth-service",
    "services/comment-service",
    "services/gateway",
    "services/project-service",
    "services/task-service"
    # jira-service excluded as requested
)

$root = Get-Location

foreach ($dir in $serviceDirs) {
    if (Test-Path $dir) {
        Write-Host "Running tests in $dir..." -ForegroundColor Cyan
        
        # Run tests for the service
        # Using :service:test to target the test task of that specific project
        $gradleProject = $dir.Replace("/", ":")
        
        Set-Location $dir
        & "$root\gradlew.bat" test
        
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Tests failed in $dir" -ForegroundColor Red
            $failedServices += $dir
        } else {
            Write-Host "Tests passed in $dir" -ForegroundColor Green
        }
        Set-Location $root
    } else {
        Write-Host "Directory $dir not found, skipping." -ForegroundColor Yellow
    }
}

Write-Host "-----------------------------" -ForegroundColor Yellow
if ($failedServices.Count -eq 0) {
    Write-Host "All tests passed successfully!" -ForegroundColor Green
} else {
    Write-Host "Tests failed in the following services: $($failedServices -join ', ')" -ForegroundColor Red
    exit 1
}
