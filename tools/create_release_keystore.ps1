$ErrorActionPreference = "Stop"

$keytool = Join-Path $env:ProgramFiles "Android\Android Studio\jbr\bin\keytool.exe"
$keystore = Join-Path $env:USERPROFILE "Cartio-release.jks"

if (-not (Test-Path -LiteralPath $keytool)) {
    throw "Android Studio keytool was not found at $keytool"
}

if (Test-Path -LiteralPath $keystore) {
    throw "A keystore already exists at $keystore. It was not overwritten."
}

Write-Host "Creating Cartio's Play upload key at $keystore"
Write-Host "You will be prompted for passwords privately by keytool. Keep them in a password manager."

& $keytool `
    -genkeypair `
    -v `
    -keystore $keystore `
    -alias cartio `
    -keyalg RSA `
    -keysize 4096 `
    -validity 10000 `
    -dname "CN=Juha-Matti Niiranen, O=Juha-Matti Niiranen, L=Kuopio, C=FI"

if ($LASTEXITCODE -ne 0) {
    throw "keytool failed with exit code $LASTEXITCODE"
}

Write-Host "Keystore created successfully: $keystore"
Write-Host "Back it up securely before publishing the first release."
