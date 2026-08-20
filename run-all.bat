@echo off
setlocal enabledelayedexpansion
title HR Recruitment Microservices Launcher
color 0A

echo ===============================================================================
echo                HR Recruitment Microservices - Launcher
echo ===============================================================================
echo.

echo [1/3] Stopping any services currently running on ports 8081-8086...
powershell -ExecutionPolicy Bypass -File "%~dp0scripts\stop-all.ps1"

echo.
echo [2/3] Building all 6 microservices (bootJar)...
call "%~dp0gradlew.bat" bootJar -x test
if errorlevel 1 (
    color 0C
    echo.
    echo [ERROR] Build failed! Please check compilation errors above.
    pause
    exit /b 1
)

echo.
echo [3/3] Launching all 6 services in separate windows...
echo.

cd /d "%~dp0"

start "Auth-Service [8081]" cmd /k "title Auth-Service [8081] & color 0B & echo Starting Auth-Service on port 8081... & java -jar auth\build\libs\auth-0.0.1-SNAPSHOT.jar"
start "Job-Service [8082]" cmd /k "title Job-Service [8082] & color 0E & echo Starting Job-Service on port 8082... & java -jar job\build\libs\job-0.0.1-SNAPSHOT.jar"
start "Candidate-Service [8083]" cmd /k "title Candidate-Service [8083] & color 0A & echo Starting Candidate-Service on port 8083... & java -jar candidate\build\libs\candidate-0.0.1-SNAPSHOT.jar"
start "Application-Service [8084]" cmd /k "title Application-Service [8084] & color 0D & echo Starting Application-Service on port 8084... & java -jar application\build\libs\application-0.0.1-SNAPSHOT.jar"
start "AI-Service [8085]" cmd /k "title AI-Service [8085] & color 09 & echo Starting AI-Service on port 8085... & java -jar ai\build\libs\ai-0.0.1-SNAPSHOT.jar"
start "Notification-Service [8086]" cmd /k "title Notification-Service [8086] & color 03 & echo Starting Notification-Service on port 8086... & java -jar notification\build\libs\notification-0.0.1-SNAPSHOT.jar"

echo ===============================================================================
echo  All 6 microservices have been launched in separate terminal windows!
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
echo Waiting 15 seconds to check status...
powershell -Command "Start-Sleep -Seconds 15"
call "%~dp0status.bat"
