#!/bin/bash
# 📋 VERIFICAÇÃO RÁPIDA DE ARQUIVOS

echo "🔍 Verificando arquivos implementados..."
echo ""

# Lista de arquivos
files=(
    "app/src/main/java/com/ifpr/androidapptemplate/data/model/PostagemModels.kt"
    "app/src/main/java/com/ifpr/androidapptemplate/data/firebase/FirebaseDatabaseService.kt"
    "app/src/main/java/com/ifpr/androidapptemplate/ui/registro/RegistroPlantaViewModel.kt"
    "app/src/main/java/com/ifpr/androidapptemplate/ui/registro/RegistroInsetoViewModel.kt"
    "app/src/main/java/com/ifpr/androidapptemplate/ui/postagens/PostagensViewModel.kt"
    "app/src/main/java/com/ifpr/androidapptemplate/ui/postagens/PostagensAdapter.kt"
    "app/src/main/java/com/ifpr/androidapptemplate/ui/postagens/PostagensFragment.kt"
    "app/src/main/res/layout/fragment_postagens.xml"
)

for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ $file (NÃO ENCONTRADO)"
    fi
done

echo ""
echo "📚 Arquivos de documentação criados:"
files_docs=(
    "RESUMO_IMPLEMENTACAO_POSTAGENS.md"
    "DIAGRAMAS_VISUAIS.md"
    "IMPLEMENTACAO_POSTAGENS_COMPLETA.md"
    "GUIA_TESTE_POSTAGENS.md"
    "FLUXO_REGISTROS_POSTAGENS.md"
    "CHECKLIST_PRE_COMPILACAO.md"
)

for file in "${files_docs[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ $file (NÃO ENCONTRADO)"
    fi
done

echo ""
echo "✨ Verificação concluída!"
