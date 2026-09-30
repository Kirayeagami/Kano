param([switch]$Connected)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$portable = 'E:\Works\tools\kano'
if (-not $env:JAVA_HOME -and (Test-Path "$portable\jdk")) {
    $jdkDirectory = Get-ChildItem "$portable\jdk" -Directory | Select-Object -First 1
    $env:JAVA_HOME = $jdkDirectory.FullName
}
if (Test-Path $portable) {
    $env:GRADLE_USER_HOME = "$portable\gradle-cache"
    $env:ANDROID_USER_HOME = "$portable\android-user"
    # AGP rejects conflicting legacy and current Android preference locations.
    Remove-Item Env:ANDROID_SDK_HOME -ErrorAction SilentlyContinue
    Remove-Item Env:ANDROID_PREFS_ROOT -ErrorAction SilentlyContinue
}
$tasks = @(':core:test', ':app:testDebugUnitTest', ':app:lintDebug', ':app:assembleDebug', ':app:assembleDebugAndroidTest')
if ($Connected) { $tasks += ':app:connectedDebugAndroidTest' }
Push-Location $projectRoot
try {
    & '.\gradlew.bat' @tasks '--console=plain'
    if ($LASTEXITCODE -ne 0) { throw "Gradle validation failed with exit code $LASTEXITCODE" }
    [xml]$manifest = Get-Content 'app/build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml'
    $androidNamespace = 'http://schemas.android.com/apk/res/android'
    $forbidden = @('android.permission.INTERNET', 'android.permission.MANAGE_EXTERNAL_STORAGE',
        'android.permission.QUERY_ALL_PACKAGES', 'android.permission.WRITE_EXTERNAL_STORAGE')
    foreach ($permission in $manifest.manifest.'uses-permission') {
        if ($permission.GetAttribute('name', $androidNamespace) -in $forbidden) {
            throw 'Merged manifest violates the scoped-gallery/local-only boundary.'
        }
    }
    $legacyRead = @($manifest.manifest.'uses-permission') | Where-Object { $_.GetAttribute('name', $androidNamespace) -eq 'android.permission.READ_EXTERNAL_STORAGE' }
    if ($legacyRead -and $legacyRead.GetAttribute('maxSdkVersion', $androidNamespace) -ne '32') {
        throw 'Legacy media read access must be bounded to API 32.'
    }
    if ($manifest.manifest.application.GetAttribute('allowBackup', $androidNamespace) -ne 'false') {
        throw 'Backup must remain disabled for this development slice.'
    }
    Write-Output 'Merged-manifest privacy boundary verified.'
} finally { Pop-Location }
