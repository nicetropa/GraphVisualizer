@echo off
REM Compile et lance le visualiseur de graphe
REM Prerequis : JDK 11+ installe et dans le PATH

set SRC_DIR=src
set OUT_DIR=out

echo ==^> Compilation...
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

for /r "%SRC_DIR%" %%f in (*.java) do set SOURCES=!SOURCES! "%%f"
setlocal enabledelayedexpansion

set SOURCES=
for /r "%SRC_DIR%" %%f in (*.java) do set SOURCES=!SOURCES! "%%f"

javac -d "%OUT_DIR%" -sourcepath "%SRC_DIR%" %SOURCES%

if %errorlevel% neq 0 (
    echo Erreur de compilation.
    pause
    exit /b 1
)

echo ==^> Lancement...
java -cp "%OUT_DIR%" main.Main
