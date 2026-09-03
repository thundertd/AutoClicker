#!/usr/bin/env pwsh

# Android Auto Clicker - Debug Test Script
# This script will:
# 1. Build APK
# 2. Clear logcat
# 3. Install APK
# 4. Start logcat capture in background
# 5. Wait for user to trigger action
# 6. Save logcat output

$ANDROID_HOME = [System.Environment]::GetEnvironmentVariable("ANDROID_HOME", "User")
if (-not $ANDROID_HOME) {
    $ANDROID_HOME = "C:\Android\sdk"
}

$ADB = "$ANDROID_HOME\platform-tools\adb.exe"
$GRADLE = ".\gradlew.bat"

Write-Host "=== Android Auto Clicker Debug Test ===" -ForegroundColor Cyan
Write-Host "ADB Path: $ADB" -ForegroundColor Yellow
Write-Host "Gradle: $GRADLE" -ForegroundColor Yellow
Write-Host ""

# Step 1: Build
Write-Host "[1/5] Building Debug APK..." -ForegroundColor Green
& $GRADLE assembleDebug --quiet
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Build failed" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Build successful" -ForegroundColor Green
Write-Host ""

# Step 2: Check device connection
Write-Host "[2/5] Checking device connection..." -ForegroundColor Green
$devices = & $ADB devices | Select-Object -Skip 1 | Where-Object {$_ -match '\w+'} | Select-Object -First 1
if (-not $devices) {
    Write-Host "⚠ No device connected. Please connect a device or start an emulator." -ForegroundColor Yellow
    Write-Host "You can still view logs manually with: $ADB logcat -s AutoClickService" -ForegroundColor Yellow
    exit 0
}
Write-Host "✓ Device found: $devices" -ForegroundColor Green
Write-Host ""

# Step 3: Clear logcat and start capture
Write-Host "[3/5] Clearing logcat and starting capture..." -ForegroundColor Green
& $ADB logcat -c
Write-Host "✓ Logcat cleared" -ForegroundColor Green
Write-Host ""

# Step 4: Install APK
Write-Host "[4/5] Installing APK..." -ForegroundColor Green
$APK_PATH = ".\app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $APK_PATH)) {
    Write-Host "ERROR: APK not found at $APK_PATH" -ForegroundColor Red
    exit 1
}

& $ADB install -r $APK_PATH
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Installation failed" -ForegroundColor Red
    exit 1
}
Write-Host "✓ APK installed" -ForegroundColor Green
Write-Host ""

# Step 5: Start capturing logcat in background and launch app
Write-Host "[5/5] Starting logcat capture and launching app..." -ForegroundColor Green
Write-Host ""

# Start logcat capture in background
$logFile = ".\logcat_output_$(Get-Date -Format 'yyyy-MM-dd_HHmmss').txt"
Write-Host "Logcat will be saved to: $logFile" -ForegroundColor Yellow
& $ADB logcat -s AutoClickService > $logFile &

# Give logcat time to start
Start-Sleep -Seconds 1

# Launch app
Write-Host "Launching app..." -ForegroundColor Cyan
& $ADB shell am start -n com.github.nestorm001.autoclicker/.ActionListActivity

Write-Host ""
Write-Host "=== Instructions ===" -ForegroundColor Cyan
Write-Host "1. Wait for app to open"
Write-Host "2. Grant all required permissions when prompted"
Write-Host "3. Tap the 'Chi tiết' (Detail) button on any action"
Write-Host "4. Observe the logs and floating overlay"
Write-Host ""
Write-Host "Press ENTER when done to stop logcat capture..." -ForegroundColor Yellow
Read-Host

Write-Host ""
Write-Host "Stopping logcat..." -ForegroundColor Green
& $ADB logcat -c

Write-Host ""
Write-Host "=== Test Complete ===" -ForegroundColor Green
Write-Host "Logcat output saved to: $logFile" -ForegroundColor Cyan
Write-Host ""
Write-Host "View the log file with:" -ForegroundColor Yellow
Write-Host "  notepad $logFile" -ForegroundColor Gray
