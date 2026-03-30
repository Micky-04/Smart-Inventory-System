@echo off
title Smart Inventory System

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set PATH=%JAVA_HOME%\bin;%USERPROFILE%\tools\apache-maven-3.9.13\bin;%PATH%

echo Starting Smart Inventory System...
mvn -f "%~dp0pom.xml" compile exec:java -Dexec.mainClass=com.smartinventory.SmartInventoryApp -q

if %ERRORLEVEL% neq 0 (
    echo.
    echo Application failed to start. Press any key to exit.
    pause >nul
)
