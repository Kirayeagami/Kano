Add-Type -AssemblyName System.Drawing

$srcPath = "C:\Users\kiray\.gemini\antigravity\brain\43edc6b3-fd21-4161-892b-b00bf2ecbcb1\.user_uploaded\media_1790728945170.jpg"
if (-not (Test-Path $srcPath)) {
    Write-Error "Source image not found: $srcPath"
    exit 1
}

$srcImg = [System.Drawing.Image]::FromFile($srcPath)

# 1. Master Logo in drawable-nodpi
$drawableDir = "app\src\main\res\drawable-nodpi"
if (-not (Test-Path $drawableDir)) { New-Item -ItemType Directory -Path $drawableDir -Force | Out-Null }
$masterPath = Join-Path $drawableDir "kano_logo.png"
$srcImg.Save($masterPath, [System.Drawing.Imaging.ImageFormat]::Png)
Write-Host "Saved master logo to $masterPath"

# Densities for adaptive icon foreground (108dp base)
$densities = @(
    @{ Name = "mdpi"; Fg = 108; Legacy = 48 },
    @{ Name = "hdpi"; Fg = 162; Legacy = 72 },
    @{ Name = "xhdpi"; Fg = 216; Legacy = 96 },
    @{ Name = "xxhdpi"; Fg = 324; Legacy = 144 },
    @{ Name = "xxxhdpi"; Fg = 432; Legacy = 192 }
)

foreach ($entry in $densities) {
    $dir = "app\src\main\res\mipmap-$($entry.Name)"
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    
    # Foreground: 108dp canvas with image scaled to safe zone (~76% size, centered)
    $fgSize = [int]$entry.Fg
    $fgBmp = New-Object System.Drawing.Bitmap $fgSize, $fgSize
    $g = [System.Drawing.Graphics]::FromImage($fgBmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.Clear([System.Drawing.Color]::Transparent)
    
    $drawSize = [int]($fgSize * 0.78)
    $offset = [int](($fgSize - $drawSize) / 2)
    $g.DrawImage($srcImg, $offset, $offset, $drawSize, $drawSize)
    $g.Dispose()
    
    $fgPath = Join-Path $dir "ic_launcher_foreground.png"
    $fgBmp.Save($fgPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $fgBmp.Dispose()
    
    # Legacy launcher icon: full squircle scaled to standard launcher icon size
    $legSize = [int]$entry.Legacy
    $legBmp = New-Object System.Drawing.Bitmap $legSize, $legSize
    $g2 = [System.Drawing.Graphics]::FromImage($legBmp)
    $g2.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g2.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g2.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g2.Clear([System.Drawing.Color]::Transparent)
    $g2.DrawImage($srcImg, 0, 0, $legSize, $legSize)
    $g2.Dispose()
    
    $legPath = Join-Path $dir "ic_launcher.png"
    $legBmp.Save($legPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $legRoundPath = Join-Path $dir "ic_launcher_round.png"
    $legBmp.Save($legRoundPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $legBmp.Dispose()
    
    Write-Host "Generated $($entry.Name) icons: fg=$fgSize px, legacy=$legSize px"
}

$srcImg.Dispose()
Write-Host "All icon assets successfully created."
