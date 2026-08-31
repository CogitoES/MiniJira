# stop_services.ps1
# Forces all Java processes to terminate (including child processes)
Write-Host "Forcefully stopping all Java-based services..." -ForegroundColor Yellow

# taskkill /F forces the process to stop
# /IM java.exe targets all java processes
# /T stops child processes (e.g., Tomcat servers started by Gradle)
taskkill /F /IM java.exe /T

# Optional: Stop Gradle daemons to free up memory
Write-Host "Stopping Gradle daemons..." -ForegroundColor Cyan
.\gradlew --stop

Write-Host "All services and Gradle daemons have been stopped." -ForegroundColor Green
