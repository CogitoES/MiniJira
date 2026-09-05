# stop_services.ps1
# Forces all Java processes to terminate (including child processes)
Write-Host "Forcefully stopping all Java-based services..." -ForegroundColor Yellow

# Check if any java processes are running
$javaProcesses = Get-Process -Name java -ErrorAction SilentlyContinue

if ($javaProcesses) {
    # taskkill /F forces the process to stop
    # /IM java.exe targets all java processes
    # /T stops child processes (e.g., Tomcat servers started by Gradle)
    taskkill /F /IM java.exe /T
    Write-Host "Java processes have been terminated." -ForegroundColor Green
} else {
    Write-Host "No Java processes found running." -ForegroundColor Cyan
}

# Optional: Stop Gradle daemons to free up memory
Write-Host "Stopping Gradle daemons..." -ForegroundColor Cyan
.\gradlew --stop

Write-Host "All services and Gradle daemons have been stopped." -ForegroundColor Green
