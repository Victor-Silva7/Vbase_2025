# Script para analisar drawables não utilizados
$drawablePath = "c:\Users\Victor\Documents\GitHub\Vbase_2025\app\src\main\res\drawable"
$srcPath = "c:\Users\Victor\Documents\GitHub\Vbase_2025\app\src"
$resPath = "c:\Users\Victor\Documents\GitHub\Vbase_2025\app\src\main\res"

# Obter todos os drawables
$drawables = @()
Get-ChildItem -Path "$drawablePath\*.xml" | ForEach-Object {
    $drawables += $_.BaseName
}

Write-Host "Total de drawables encontrados: $($drawables.Count)" -ForegroundColor Cyan
Write-Host ""

# Analisar uso de cada drawable
$unusedDrawables = @()
$usedDrawables = @()

foreach ($drawable in $drawables) {
    # Procurar referências
    $references = @()
    
    # Procurar em arquivos XML
    $xmlMatches = Get-ChildItem -Path "$resPath\**\*.xml" -Recurse | 
        Select-String -Pattern "@drawable/$drawable|android:drawable=`"@drawable/$drawable" | 
        Select-Object Path, LineNumber, Line
    
    # Procurar em arquivos Kotlin/Java
    $codeMatches = Get-ChildItem -Path "$srcPath\**\*.kt", "$srcPath\**\*.java" -Recurse | 
        Select-String -Pattern "@drawable/$drawable|R\.drawable\.$drawable" | 
        Select-Object Path, LineNumber, Line
    
    $allMatches = @()
    if ($xmlMatches) { $allMatches += $xmlMatches }
    if ($codeMatches) { $allMatches += $codeMatches }
    
    if ($allMatches.Count -gt 0) {
        $usedDrawables += [PSCustomObject]@{
            Name = $drawable
            Count = $allMatches.Count
            References = $allMatches
        }
    } else {
        $unusedDrawables += $drawable
    }
}

Write-Host "=== DRAWABLES NÃO UTILIZADOS ===" -ForegroundColor Red
Write-Host "Total: $($unusedDrawables.Count)" -ForegroundColor Yellow
$unusedDrawables | ForEach-Object { Write-Host "  - $_" }

Write-Host ""
Write-Host "=== DRAWABLES UTILIZADOS ===" -ForegroundColor Green
Write-Host "Total: $($usedDrawables.Count)" -ForegroundColor Yellow

$usedDrawables | Sort-Object -Property Count -Descending | ForEach-Object {
    Write-Host "  [$($_.Count) referências] $($_.Name)" -ForegroundColor Green
    $_.References | ForEach-Object {
        $filePath = $_.Path -replace [regex]::Escape("c:\Users\Victor\Documents\GitHub\Vbase_2025\"), ""
        Write-Host "    - $filePath (linha $($_.LineNumber))" -ForegroundColor DarkGray
    }
}

# Salvar relatório em arquivo
$report = "# Análise de Drawables

## Drawables Não Utilizados ($($unusedDrawables.Count))
"

$unusedDrawables | ForEach-Object {
    $report += "`n- **$_**"
}

$report += "`n`n## Drawables Utilizados ($($usedDrawables.Count))`n"

$usedDrawables | Sort-Object -Property Name | ForEach-Object {
    $report += "`n### $($_.Name) ($($_.Count) referências)`n"
    $_.References | ForEach-Object {
        $filePath = $_.Path -replace [regex]::Escape("c:\Users\Victor\Documents\GitHub\Vbase_2025\"), ""
        $report += "- `$filePath (linha $($_.LineNumber))`n"
    }
}

$report | Out-File -FilePath "c:\Users\Victor\Documents\GitHub\Vbase_2025\ANALISE_DRAWABLES.md" -Encoding UTF8

Write-Host ""
Write-Host "Relatório salvo em ANALISE_DRAWABLES.md" -ForegroundColor Cyan
