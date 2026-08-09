param(
    [Parameter(Mandatory = $true)][string]$SourceDirectory,
    [Parameter(Mandatory = $true)][string]$OutputFile
)

$foods = Import-Csv -Delimiter ';' -Encoding Default (Join-Path $SourceDirectory 'food.csv')
$finnish = @{}
Import-Csv -Delimiter ';' -Encoding Default (Join-Path $SourceDirectory 'foodname_FI.csv') | ForEach-Object { $finnish[$_.FOODID] = $_.FOODNAME }
$english = @{}
Import-Csv -Delimiter ';' -Encoding Default (Join-Path $SourceDirectory 'foodname_EN.csv') | ForEach-Object { $english[$_.FOODID] = $_.FOODNAME }

function Get-CartioCategory([string]$foodClass, [string]$fiName, [string]$enName) {
    if ($fiName -match '(?i)pakaste|jäätelö|sorbetti' -or $enName -match '(?i)frozen|ice cream|sorbet') { return 'FROZEN' }
    if ($foodClass -match '^(FRU|BER|VEG|MUS|POT)') { return 'FRUITS_VEGETABLES' }
    if ($foodClass -match '^(MIL|YOG|CHE|EGG|CRE)') { return 'DAIRY' }
    if ($foodClass -match '^(BAK|BR|CER|POR|FLO|BUN)') { return 'BREAD_GRAINS' }
    if ($foodClass -match '^(MEA|POU|FIS|SAU|OFF|CRU|SEA)') { return 'MEAT_FISH' }
    if ($foodClass -match '^(JUI|BEV|SOF|DRI|WAT)') { return 'DRINKS' }
    return 'PANTRY'
}

$seen = @{}
$rows = [System.Collections.Generic.List[string]]::new()
$ordered = $foods | Where-Object { $_.FOODTYPE -eq 'FOOD' } | Sort-Object @{ Expression = { [int]$_.FOODID } }
foreach ($food in $ordered) {
    $fiName = (($finnish[$food.FOODID] -replace '[\t\r\n]+', ' ').Split(',')[0]).Trim()
    $enName = (($english[$food.FOODID] -replace '[\t\r\n]+', ' ').Split(',')[0] -replace '(?i)\s+(WITH|WITHOUT|AVERAGE).*$','').Trim()
    if (-not $fiName -or -not $enName -or $fiName -match '^(POISTETTU|REMOVED|\(ARC\))' -or $enName -match '^(POISTETTU|REMOVED|\(ARC\))') { continue }
    if ($fiName -match '\d|%|VALIO|MCDONALD|HESBURGER|ATRIA|HK ' -or $enName -match '\d|%' -or $fiName.Length -gt 32 -or $enName.Length -gt 40) { continue }
    $key = $fiName.ToLowerInvariant()
    if ($seen.ContainsKey($key)) { continue }
    $seen[$key] = $true
    $category = Get-CartioCategory $food.FUCLASS $fiName $enName
    $rows.Add("$category`t$fiName`t$enName")
}

$household = @(
    'Talouspaperi|Paper towel', 'WC-paperi|Toilet paper', 'Astianpesuaine|Dish soap',
    'Pyykinpesuaine|Laundry detergent', 'Huuhteluaine|Fabric softener', 'Yleispuhdistusaine|All-purpose cleaner',
    'Käsisaippua|Hand soap', 'Roskapussi|Trash bag', 'Leivinpaperi|Baking paper', 'Alumiinifolio|Aluminium foil',
    'Tuorekelmu|Cling film', 'Siivoussieni|Cleaning sponge', 'Tiskiharja|Dish brush', 'Patterit|Batteries',
    'Käsidesi|Hand sanitizer'
)
foreach ($entry in $household) {
    $names = $entry.Split('|')
    $rows.Add("HOUSEHOLD`t$($names[0])`t$($names[1])")
}

$header = "# Adapted from Fineli Release 20, THL, CC BY 4.0`r`n# category`tFinnish`tEnglish`r`n"
$content = $header + (($rows -join "`r`n") + "`r`n")
$outputDirectory = Split-Path -Parent $OutputFile
New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
[System.IO.File]::WriteAllText($OutputFile, $content, [System.Text.UTF8Encoding]::new($false))
Write-Output "Imported $($rows.Count) distinct catalog products."
