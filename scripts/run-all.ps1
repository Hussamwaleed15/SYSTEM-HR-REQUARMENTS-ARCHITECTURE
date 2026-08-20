$projectRoot = (Resolve-Path "$PSScriptRoot\..").Path

Write-Host "==========================================================================================" -ForegroundColor Cyan
Write-Host "                        Starting All HR Recruitment Microservices                         " -ForegroundColor Yellow
Write-Host "==========================================================================================" -ForegroundColor Cyan

# 1. Stop any running instances first
& "$PSScriptRoot\stop-all.ps1"

# 2. Build jars
Write-Host "`n[1/2] Building microservice JAR files..." -ForegroundColor Cyan
Push-Location $projectRoot
& ".\gradlew.bat" bootJar -x test
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Build failed! Please resolve compilation errors." -ForegroundColor Red
    Pop-Location
    exit 1
}
Pop-Location

# 3. Microservices list
$services = @(
    @{ Name = "auth";         Title = "Auth-Service [8081]";         Port = 8081; Jar = "$projectRoot\auth\build\libs\auth-0.0.1-SNAPSHOT.jar" },
    @{ Name = "job";          Title = "Job-Service [8082]";          Port = 8082; Jar = "$projectRoot\job\build\libs\job-0.0.1-SNAPSHOT.jar" },
    @{ Name = "candidate";    Title = "Candidate-Service [8083]";    Port = 8083; Jar = "$projectRoot\candidate\build\libs\candidate-0.0.1-SNAPSHOT.jar" },
    @{ Name = "application";  Title = "Application-Service [8084]";  Port = 8084; Jar = "$projectRoot\application\build\libs\application-0.0.1-SNAPSHOT.jar" },
    @{ Name = "ai";           Title = "AI-Service [8085]";           Port = 8085; Jar = "$projectRoot\ai\build\libs\ai-0.0.1-SNAPSHOT.jar" },
    @{ Name = "notification"; Title = "Notification-Service [8086]"; Port = 8086; Jar = "$projectRoot\notification\build\libs\notification-0.0.1-SNAPSHOT.jar" }
)

Write-Host "`n[2/2] Launching all 6 microservices in separate windows..." -ForegroundColor Cyan

foreach ($s in $services) {
    $jarPath = $s.Jar
    $title = $s.Title
    $arg = "/k `"title $title & echo Starting $title... & java -jar `"$jarPath`"`""
    Start-Process -FilePath "cmd.exe" -ArgumentList $arg -WorkingDirectory $projectRoot
    Write-Host ("  + Started " + $s.Title + " on port " + $s.Port) -ForegroundColor Green
}

Write-Host "`nWaiting 15 seconds for all services to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Show status
& "$PSScriptRoot\status.ps1"
