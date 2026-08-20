@echo off
title HR Recruitment Services - Stop Docker
color 0C

echo Stopping all Docker containers for HR Recruitment Services...
docker compose down

echo.
echo All containers stopped!
pause
