$ErrorActionPreference = 'Stop'
$adb = 'E:\Works\tools\kano\android-sdk\platform-tools\adb.exe'
$outDir = 'E:\Works\Kano\docs\validation\2026-09-30'
if (-not (Test-Path $outDir)) { New-Item -ItemType Directory -Path $outDir -Force | Out-Null }

function Snap($name) {
    Start-Sleep -Milliseconds 600
    & $adb -s emulator-5554 shell screencap -p /sdcard/sc.png
    & $adb -s emulator-5554 pull /sdcard/sc.png "$outDir\$name" | Out-Null
    Write-Output "Captured $name"
}

# 1. Home
Snap "emulator-home.png"

# 2. Open Hamburger menu (top-right at x=900, y=220 on 1080x2340)
& $adb -s emulator-5554 shell input tap 900 220
Snap "emulator-menu.png"

# Close menu (tap back)
& $adb -s emulator-5554 shell input keyevent 4
Start-Sleep -Milliseconds 600

# 3. Device (nav item 2: x=315, y=2075)
& $adb -s emulator-5554 shell input tap 315 2075
Snap "emulator-device.png"

# 4. Media (nav item 3: x=465, y=2075)
& $adb -s emulator-5554 shell input tap 465 2075
Snap "emulator-media.png"

# 5. Vault (nav item 4: x=615, y=2075)
& $adb -s emulator-5554 shell input tap 615 2075
Snap "emulator-vault.png"

# 6. Style (nav item 5: x=765, y=2075)
& $adb -s emulator-5554 shell input tap 765 2075
Snap "emulator-style.png"

# 7. Care (nav item 6: x=915, y=2075)
& $adb -s emulator-5554 shell input tap 915 2075
Snap "emulator-care.png"

# Return to Home (nav item 1: x=165, y=2075)
& $adb -s emulator-5554 shell input tap 165 2075
Write-Output "All validation captures completed."
