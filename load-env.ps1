param(
    [string]$EnvFile = ".env"
)

function Resolve-EnvValue ([string]$value) {
    $expanded = $value -replace '\$\{(\w+)\}', '%$1%' -replace '\$(\w+)', '%$1%'
    return [System.Environment]::ExpandEnvironmentVariables($expanded)
}

$resolvedPath = Resolve-Path $EnvFile -ErrorAction SilentlyContinue
if (-not $resolvedPath) {
    Write-Error "File not found: $EnvFile"
    exit 1
}

$filePath = $resolvedPath.Path
$loaded   = [System.Collections.Generic.List[string]]::new()
$skipped  = [System.Collections.Generic.List[string]]::new()
$lineNum  = 0

foreach ($rawLine in Get-Content $filePath -Encoding UTF8) {
    $lineNum++
    $line = $rawLine.Trim()

    if ($line -eq "" -or $line.StartsWith("#")) { continue }

    if ($line -notmatch '^([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*)$') {
        Write-Warning "Line $lineNum skipped (invalid format): $line"
        $skipped.Add($line)
        continue
    }

    $key   = $Matches[1]
    $value = $Matches[2]

    if (($value -match '^"(.*)"$') -or ($value -match "^'(.*)'$")) {
        $value = $Matches[1]
    }

    if ($value -match '^([^#]*?)\s+#.*$') {
        $value = $Matches[1]
    }

    $value = Resolve-EnvValue $value

    [System.Environment]::SetEnvironmentVariable($key, $value, "Process")
    Set-Item -Path "Env:$key" -Value $value
    Write-Host "$key"

    $loaded.Add($key)
}

Write-Host "Done: $($loaded.Count) loaded, $($skipped.Count) skipped."