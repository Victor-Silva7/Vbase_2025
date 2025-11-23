#!/bin/bash

# Script de Verificação Firebase - Vbase 2025
# Execute este script para verificar se as correções foram aplicadas

echo "🔍 VERIFICAÇÃO FIREBASE - VBASE 2025"
echo "===================================="
echo ""

# Cores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Contador de verificações
CHECKS_PASSED=0
CHECKS_FAILED=0

echo "📋 VERIFICANDO ARQUIVOS..."
echo ""

# 1. Verificar google-services.json
echo -n "1. Arquivo google-services.json existe? "
if [ -f "app/google-services.json" ]; then
    echo -e "${GREEN}✅ SIM${NC}"
    CHECKS_PASSED=$((CHECKS_PASSED + 1))
else
    echo -e "${RED}❌ NÃO ENCONTRADO${NC}"
    CHECKS_FAILED=$((CHECKS_FAILED + 1))
fi

# 2. Verificar VGroupApplication
echo -n "2. VGroupApplication existe? "
if [ -f "app/src/main/java/com/ifpr/androidapptemplate/VGroupApplication.kt" ]; then
    echo -e "${GREEN}✅ SIM${NC}"
    CHECKS_PASSED=$((CHECKS_PASSED + 1))
else
    echo -e "${RED}❌ NÃO${NC}"
    CHECKS_FAILED=$((CHECKS_FAILED + 1))
fi

# 3. Verificar AndroidManifest
echo -n "3. AndroidManifest contém VGroupApplication? "
if grep -q "android:name=\".VGroupApplication\"" app/src/main/AndroidManifest.xml; then
    echo -e "${GREEN}✅ SIM${NC}"
    CHECKS_PASSED=$((CHECKS_PASSED + 1))
else
    echo -e "${RED}❌ NÃO${NC}"
    CHECKS_FAILED=$((CHECKS_FAILED + 1))
fi

# 4. Verificar se PerfilUsuarioFragment foi corrigido
echo -n "4. PerfilUsuarioFragment usa 'usuarios'? "
if grep -q "getReference(\"usuarios\")" app/src/main/java/com/ifpr/androidapptemplate/ui/usuario/PerfilUsuarioFragment.kt; then
    echo -e "${GREEN}✅ SIM (Corrigido)${NC}"
    CHECKS_PASSED=$((CHECKS_PASSED + 1))
else
    echo -e "${RED}❌ Ainda usa 'users'${NC}"
    CHECKS_FAILED=$((CHECKS_FAILED + 1))
fi

# 5. Verificar se não há mais referências a "users"
echo -n "5. Não há mais referências a 'users' erradas? "
USER_REFS=$(grep -r "getReference(\"users\")" app/src/main/java/ 2>/dev/null | wc -l)
if [ "$USER_REFS" -eq 0 ]; then
    echo -e "${GREEN}✅ Correto (0 referências)${NC}"
    CHECKS_PASSED=$((CHECKS_PASSED + 1))
else
    echo -e "${RED}❌ Encontradas $USER_REFS referências${NC}"
    CHECKS_FAILED=$((CHECKS_FAILED + 1))
fi

# 6. Verificar FirebaseDatabaseService
echo -n "6. FirebaseDatabaseService existe? "
if [ -f "app/src/main/java/com/ifpr/androidapptemplate/data/firebase/FirebaseDatabaseService.kt" ]; then
    echo -e "${GREEN}✅ SIM${NC}"
    CHECKS_PASSED=$((CHECKS_PASSED + 1))
else
    echo -e "${RED}❌ NÃO${NC}"
    CHECKS_FAILED=$((CHECKS_FAILED + 1))
fi

# 7. Verificar FirebaseConfig
echo -n "7. FirebaseConfig existe? "
if [ -f "app/src/main/java/com/ifpr/androidapptemplate/data/firebase/FirebaseConfig.kt" ]; then
    echo -e "${GREEN}✅ SIM${NC}"
    CHECKS_PASSED=$((CHECKS_PASSED + 1))
else
    echo -e "${RED}❌ NÃO${NC}"
    CHECKS_FAILED=$((CHECKS_FAILED + 1))
fi

# 8. Verificar regras do Firebase
echo -n "8. Arquivo de regras firebase-rules-simple.json existe? "
if [ -f "firebase-rules-simple.json" ]; then
    echo -e "${GREEN}✅ SIM${NC}"
    CHECKS_PASSED=$((CHECKS_PASSED + 1))
else
    echo -e "${RED}❌ NÃO${NC}"
    CHECKS_FAILED=$((CHECKS_FAILED + 1))
fi

# 9. Verificar plugins do Gradle
echo -n "9. Plugin Google Services configurado? "
if grep -q "google.services" app/build.gradle.kts; then
    echo -e "${GREEN}✅ SIM${NC}"
    CHECKS_PASSED=$((CHECKS_PASSED + 1))
else
    echo -e "${RED}❌ NÃO${NC}"
    CHECKS_FAILED=$((CHECKS_FAILED + 1))
fi

# 10. Verificar dependências Firebase
echo -n "10. Dependências Firebase configuradas? "
if grep -q "firebase.database.ktx" app/build.gradle.kts; then
    echo -e "${GREEN}✅ SIM${NC}"
    CHECKS_PASSED=$((CHECKS_PASSED + 1))
else
    echo -e "${RED}❌ NÃO${NC}"
    CHECKS_FAILED=$((CHECKS_FAILED + 1))
fi

echo ""
echo "===================================="
echo "📊 RESUMO DA VERIFICAÇÃO"
echo "===================================="
echo -e "Verificações ${GREEN}passadas${NC}: $CHECKS_PASSED"
echo -e "Verificações ${RED}falhas${NC}: $CHECKS_FAILED"
echo ""

if [ $CHECKS_FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ TODAS AS VERIFICAÇÕES PASSARAM!${NC}"
    echo ""
    echo "🚀 PRÓXIMOS PASSOS:"
    echo "1. Limpar dados antigos do Firebase Console"
    echo "2. Aplicar regras do firebase-rules-simple.json"
    echo "3. Rodar o app e fazer login"
    echo "4. Tentar registrar uma planta"
    echo "5. Verificar no Firebase Console se salvou em 'usuarios/'"
    echo ""
    exit 0
else
    echo -e "${RED}❌ ALGUMAS VERIFICAÇÕES FALHARAM!${NC}"
    echo ""
    echo -e "${YELLOW}⚠️  AÇÕES NECESSÁRIAS:${NC}"
    echo "- Revise os itens marcados com ❌"
    echo "- Consulte CORRECAO_FIREBASE.md para detalhes"
    echo ""
    exit 1
fi
