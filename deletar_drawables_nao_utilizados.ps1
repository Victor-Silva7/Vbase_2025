# Script para deletar drawables não utilizados
$basePath = "c:\Users\Victor\Documents\GitHub\Vbase_2025\app\src\main\res\drawable"

$unusedFiles = @(
    "card_selector_background.xml",
    "ic_add_registro_24dp.xml",
    "ic_ai.xml",
    "ic_arrow_forward.xml",
    "ic_bookmark_outline.xml",
    "ic_comment_outline.xml",
    "ic_delete_24dp.xml",
    "ic_edit_24dp.xml",
    "ic_feed_24dp.xml",
    "ic_google.xml",
    "ic_like_outline.xml",
    "ic_postagens_24dp.xml",
    "ic_post_empty.xml",
    "ic_registro_24dp.xml",
    "ic_share_outline.xml",
    "login_button_background.xml",
    "logo_background.xml"
)

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Limpeza de Drawables Não Utilizados" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

$deletedCount = 0
$notFoundCount = 0

foreach ($file in $unusedFiles) {
    $fullPath = Join-Path $basePath $file
    if (Test-Path $fullPath) {
        Remove-Item $fullPath -Force
        Write-Host "[✓] Deletado: $file" -ForegroundColor Green
        $deletedCount++
    } else {
        Write-Host "[✗] Não encontrado: $file" -ForegroundColor Yellow
        $notFoundCount++
    }
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Resultado da Limpeza:" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Deletados: $deletedCount" -ForegroundColor Green
Write-Host "Não encontrados: $notFoundCount" -ForegroundColor Yellow
Write-Host ""
Write-Host "Limpeza completa!" -ForegroundColor Cyan
Write-Host ""
