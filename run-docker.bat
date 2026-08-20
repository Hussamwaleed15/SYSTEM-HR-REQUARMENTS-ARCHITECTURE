@echo off
title HR Recruitment Services - Docker Launcher
color 0A

echo ===============================================================================
echo                HR Recruitment Microservices - Docker Runner
echo ===============================================================================
echo.

echo [1/2] Building JAR files with Gradle...
call "%~dp0gradlew.bat" bootJar -x test
if errorlevel 1 (
    color 0C
    echo.
    echo [ERROR] Gradle build failed!
    pause
    exit /b 1
)

echo.
echo [2/2] Building and starting Docker containers...
docker compose up --build -d

if errorlevel 1 (
    color 0C
    echo.
    echo [ERROR] Docker Compose failed to start containers!
    pause
    exit /b 1
)

echo.
echo ===============================================================================
echo  All 6 microservices are running in Docker!
echo ===============================================================================
echo  Service Name           Port       Base URL
echo  -----------------------------------------------------------------------------
echo  Auth Service           8081       http://localhost:8081/api/auth
echo  Job Service            8082       http://localhost:8082/api/jobs
echo  Candidate Service      8083       http://localhost:8083/api/candidates
echo  Application Service    8084       http://localhost:8084/api/applications
echo  AI Service             8085       http://localhost:8085/api/ai
echo  Notification Service   8086       http://localhost:8086/api/notifications
echo ===============================================================================
echo.
echo To view logs:  docker compose logs -f
echo To stop:       docker compose down
echo.
pause
