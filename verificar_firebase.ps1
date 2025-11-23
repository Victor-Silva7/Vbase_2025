# Script de Verificação Firebase - Vbase 2025
# Execute: .\verificar_firebase.ps1

Write-Host "🔍 VERIFICAÇÃO FIREBASE - VBASE 2025" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

$checksPassados = 0
$checksFalhos = 0

Write-Host "📋 VERIFICANDO ARQUIVOS..." -ForegroundColor Yellow
Write-Host ""

# 1. Verificar google-services.json
Write-Host "1. Arquivo google-services.json existe? " -NoNewline
if (Test-Path "app\google-services.json") {
    Write-Host "✅ SIM" -ForegroundColor Green
    $checksPassados++
} else {
    Write-Host "❌ NÃO ENCONTRADO" -ForegroundColor Red
    $checksFalhos++
}

# 2. Verificar VGroupApplication
Write-Host "2. VGroupApplication existe? " -NoNewline
if (Test-Path "app\src\main\java\com\ifpr\androidapptemplate\VGroupApplication.kt") {
    Write-Host "✅ SIM" -ForegroundColor Green
    $checksPassados++
} else {
    Write-Host "❌ NÃO" -ForegroundColor Red
    $checksFalhos++
}

# 3. Verificar AndroidManifest
Write-Host "3. AndroidManifest contém VGroupApplication? " -NoNewline
$manifestContent = Get-Content "app\src\main\AndroidManifest.xml" -Raw
if ($manifestContent -match 'android:name=".VGroupApplication"') {
    Write-Host "✅ SIM" -ForegroundColor Green
    $checksPassados++
} else {
    Write-Host "❌ NÃO" -ForegroundColor Red
    $checksFalhos++
}

# 4. Verificar se PerfilUsuarioFragment foi corrigido
Write-Host "4. PerfilUsuarioFragment usa 'usuarios'? " -NoNewline
if (Test-Path "app\src\main\java\com\ifpr\androidapptemplate\ui\usuario\PerfilUsuarioFragment.kt") {
    $perfilContent = Get-Content "app\src\main\java\com\ifpr\androidapptemplate\ui\usuario\PerfilUsuarioFragment.kt" -Raw
    if ($perfilContent -match 'getReference\("usuarios"\)') {
        Write-Host "✅ SIM (Corrigido)" -ForegroundColor Green
        $checksPassados++
    } else {
        Write-Host "❌ Ainda usa 'users'" -ForegroundColor Red
        $checksFalhos++
    }
} else {
    Write-Host "❌ Arquivo não encontrado" -ForegroundColor Red
    $checksFalhos++
}

# 5. Verificar se não há mais referências a "users"
Write-Host "5. Não há mais referências a 'users' erradas? " -NoNewline
$userRefs = Select-String -Path "app\src\main\java\com\ifpr\androidapptemplate\**\*.kt" -Pattern 'getReference\("users"\)' -ErrorAction SilentlyContinue
if ($null -eq $userRefs) {
    Write-Host "✅ Correto (0 referências)" -ForegroundColor Green
    $checksPassados++
} else {
    $count = ($userRefs | Measure-Object).Count
    Write-Host "❌ Encontradas $count referências" -ForegroundColor Red
    $checksFalhos++
}

# 6. Verificar FirebaseDatabaseService
Write-Host "6. FirebaseDatabaseService existe? " -NoNewline
if (Test-Path "app\src\main\java\com\ifpr\androidapptemplate\data\firebase\FirebaseDatabaseService.kt") {
    Write-Host "✅ SIM" -ForegroundColor Green
    $checksPassados++
} else {
    Write-Host "❌ NÃO" -ForegroundColor Red
    $checksFalhos++
}

# 7. Verificar FirebaseConfig
Write-Host "7. FirebaseConfig existe? " -NoNewline
if (Test-Path "app\src\main\java\com\ifpr\androidapptemplate\data\firebase\FirebaseConfig.kt") {
    Write-Host "✅ SIM" -ForegroundColor Green
    $checksPassados++
} else {
    Write-Host "❌ NÃO" -ForegroundColor Red
    $checksFalhos++
}

# 8. Verificar regras do Firebase
Write-Host "8. Arquivo de regras firebase-rules-simple.json existe? " -NoNewline
if (Test-Path "firebase-rules-simple.json") {
    Write-Host "✅ SIM" -ForegroundColor Green
    $checksPassados++
} else {
    Write-Host "❌ NÃO" -ForegroundColor Red
    $checksFalhos++
}

# 9. Verificar plugins do Gradle
Write-Host "9. Plugin Google Services configurado? " -NoNewline
$buildGradleContent = Get-Content "app\build.gradle.kts" -Raw
if ($buildGradleContent -match 'google\.services') {
    Write-Host "✅ SIM" -ForegroundColor Green
    $checksPassados++
} else {
    Write-Host "❌ NÃO" -ForegroundColor Red
    $checksFalhos++
}

# 10. Verificar dependências Firebase
Write-Host "10. Dependências Firebase configuradas? " -NoNewline
if ($buildGradleContent -match 'firebase\.database\.ktx') {
    Write-Host "✅ SIM" -ForegroundColor Green
    $checksPassados++
} else {
    Write-Host "❌ NÃO" -ForegroundColor Red
    $checksFalhos++
}

Write-Host ""
Write-Host "====================================" -ForegroundColor Cyan
Write-Host "📊 RESUMO DA VERIFICAÇÃO" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host "Verificações " -NoNewline
Write-Host "passadas" -ForegroundColor Green -NoNewline
Write-Host ": $checksPassados"
Write-Host "Verificações " -NoNewline
Write-Host "falhas" -ForegroundColor Red -NoNewline
Write-Host ": $checksFalhos"
Write-Host ""

if ($checksFalhos -eq 0) {
    Write-Host "✅ TODAS AS VERIFICAÇÕES PASSARAM!" -ForegroundColor Green
    Write-Host ""
    Write-Host "🚀 PRÓXIMOS PASSOS:" -ForegroundColor Yellow
    Write-Host "1. Limpar dados antigos do Firebase Console"
    Write-Host "2. Aplicar regras do firebase-rules-simple.json"
    Write-Host "3. Rodar o app e fazer login"
    Write-Host "4. Tentar registrar uma planta"
    Write-Host "5. Verificar no Firebase Console se salvou em 'usuarios/'"
    Write-Host ""
    Write-Host "📖 Consulte CORRECAO_FIREBASE.md para instruções detalhadas" -ForegroundColor Cyan
    exit 0
} else {
    Write-Host "❌ ALGUMAS VERIFICAÇÕES FALHARAM!" -ForegroundColor Red
    Write-Host ""
    Write-Host "⚠️  AÇÕES NECESSÁRIAS:" -ForegroundColor Yellow
    Write-Host "- Revise os itens marcados com ❌"
    Write-Host "- Consulte CORRECAO_FIREBASE.md para detalhes"
    Write-Host ""
    exit 1
}
