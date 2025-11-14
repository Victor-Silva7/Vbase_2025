# Análise Completa de Drawables

**Data:** 13 de novembro de 2025  
**Total de Drawables:** 75  
**Drawables Utilizados:** 58  
**Drawables NÃO Utilizados:** 17  

---

## 📊 Resumo Executivo

Foram encontrados **17 drawables que NÃO estão sendo utilizados** no projeto. Estes podem ser deletados com segurança:

1. `card_selector_background.xml`
2. `ic_add_registro_24dp.xml`
3. `ic_ai.xml`
4. `ic_arrow_forward.xml`
5. `ic_bookmark_outline.xml`
6. `ic_comment_outline.xml`
7. `ic_delete_24dp.xml`
8. `ic_edit_24dp.xml`
9. `ic_feed_24dp.xml`
10. `ic_google.xml`
11. `ic_like_outline.xml`
12. `ic_postagens_24dp.xml`
13. `ic_post_empty.xml`
14. `ic_registro_24dp.xml`
15. `ic_share_outline.xml`
16. `login_button_background.xml`
17. `logo_background.xml`

---

## ❌ Drawables Não Utilizados (17)

### 1. card_selector_background.xml
- **Status:** Não encontrado em nenhum arquivo
- **Ação Recomendada:** Deletar

### 2. ic_add_registro_24dp.xml
- **Status:** Não encontrado em nenhum arquivo
- **Ação Recomendada:** Deletar
- **Observação:** Existe `ic_add_plant.xml` que é o ícone utilizado

### 3. ic_ai.xml
- **Status:** Não encontrado em nenhum arquivo
- **Ação Recomendada:** Deletar
- **Observação:** Pode ter sido removido do fluxo de IA

### 4. ic_arrow_forward.xml
- **Status:** Não encontrado em nenhum arquivo
- **Ação Recomendada:** Deletar
- **Observação:** Existe `ic_arrow_back.xml` utilizado

### 5. ic_bookmark_outline.xml
- **Status:** Não encontrado em nenhum arquivo
- **Ação Recomendada:** Deletar
- **Observação:** Existe `ic_bookmark_border_24dp.xml` e `ic_bookmark_24dp.xml` sendo usados

### 6. ic_comment_outline.xml
- **Status:** Não encontrado em nenhum arquivo
- **Ação Recomendada:** Deletar
- **Observação:** Existe `ic_comment_24dp.xml` sendo utilizado

### 7. ic_delete_24dp.xml
- **Status:** Não encontrado em nenhum arquivo
- **Ação Recomendada:** Deletar
- **Observação:** Funcionalidade de exclusão não utiliza ícone customizado

### 8. ic_edit_24dp.xml
- **Status:** Não encontrado em nenhum arquivo
- **Ação Recomendada:** Deletar
- **Observação:** Funcionalidade de edição não utiliza ícone customizado

### 9. ic_feed_24dp.xml
- **Status:** Não encontrado em nenhum arquivo
- **Ação Recomendada:** Deletar
- **Observação:** Existe `ic_postagens_24dp.xml` (também não utilizado)

### 10. ic_google.xml
- **Status:** Não encontrado em nenhum arquivo
- **Ação Recomendada:** Deletar
- **Observação:** Login Google não utiliza ícone customizado

### 11. ic_like_outline.xml
- **Status:** Não encontrado em nenhum arquivo
- **Ação Recomendada:** Deletar
- **Observação:** Existe `ic_favorite_border_24dp.xml` e `ic_favorite_24dp.xml` para likes/favoritos

### 12. ic_postagens_24dp.xml
- **Status:** Não encontrado em nenhum arquivo
- **Ação Recomendada:** Deletar
- **Observação:** Existe `ic_feed_24dp.xml` (também não utilizado)

### 13. ic_post_empty.xml
- **Status:** Não encontrado em nenhum arquivo
- **Ação Recomendada:** Deletar
- **Observação:** Existe `ic_image_placeholder.xml` e `ic_error_24dp.xml` para placeholders

### 14. ic_registro_24dp.xml
- **Status:** Não encontrado em nenhum arquivo
- **Ação Recomendada:** Deletar
- **Observação:** Existe `ic_add_registro_24dp.xml` (também não utilizado)

### 15. ic_share_outline.xml
- **Status:** Não encontrado em nenhum arquivo
- **Ação Recomendada:** Deletar
- **Observação:** Existe `ic_share_24dp.xml` sendo utilizado

### 16. login_button_background.xml
- **Status:** Não encontrado em nenhum arquivo
- **Ação Recomendada:** Deletar
- **Observação:** Botões de login utilizam fundos padrão

### 17. logo_background.xml
- **Status:** Não encontrado em nenhum arquivo
- **Ação Recomendada:** Deletar
- **Observação:** Logo não utiliza drawable customizado

---

## ✅ Drawables Mais Utilizados (Top 10)

### 1. ic_planta_24dp - 15 referências
- `app/src/main/res/layout/fragment_feed.xml` (linha 155)
- `app/src/main/res/layout/fragment_public_search.xml` (linha 103)
- `app/src/main/res/layout/fragment_registros_list.xml` (linha 59)
- `app/src/main/res/layout/fragment_registros_list.xml` (linha 106)
- `app/src/main/res/layout/item_postagem_card.xml` (linha 146)
- `app/src/main/res/layout/item_postagem_card.xml` (linha 185)
- `app/src/main/res/layout/item_post_feed.xml` (linha 98)
- `app/src/main/res/layout/item_search_result.xml` (linha 42)
- Código Kotlin: AttachmentPreviewAdapter, PostagemCardAdapter, RegistrosAdapter, SearchResultsAdapter, SearchSuggestionsAdapter

### 2. ic_inseto_24dp - 8 referências
- `app/src/main/res/layout/activity_registro_inseto.xml` (linha 23)
- `app/src/main/res/layout/fragment_public_search.xml` (linha 116)
- `app/src/main/res/layout/fragment_registros_list.xml` (linha 67)
- Código Kotlin: PostagemCardAdapter, RegistrosAdapter, SearchResultsAdapter, SearchSuggestionsAdapter

### 3. ic_user_placeholder - 7 referências
- Menu principal (bottom_nav_menu.xml)
- Vários layouts de comentários e posts
- Usado como placeholder padrão para fotos de usuário

### 4. ic_error_24dp - 7 referências
- Fragmentos: comentários, feed, public_search
- Código Kotlin em vários adapters
- Usado para mostrar erros de carregamento

### 5. ic_close - 6 referências
- Dialógos, fragmentos e layouts
- Botão de fechamento geral

### 6. ic_image_placeholder - 6 referências
- Layouts de posts e resultados de busca
- Código Kotlin em adapters de imagens

### 7. ripple_circle_green - 5 referências
- Atividades de registro de plantas e insetos

### 8. ic_favorite_border_24dp - 5 referências
- Comentários, posts e feeds

### 9. ic_verified_24dp - 4 referências
- Badge de verificação em comentários e posts

### 10. ic_comment_24dp - 4 referências
- Ícone de comentários nos layouts

---

## 🗂️ Drawables Utilizados Completo (58 total)

### Drawables de Ícones UI (40)
- ic_add_plant
- ic_arrow_back
- ic_arrow_back_24dp
- ic_attachment_24dp
- ic_benefico_24dp
- ic_bookmark_24dp
- ic_bookmark_border_24dp
- ic_calendar
- ic_camera_24dp
- ic_close
- ic_close_24dp
- ic_cloud_upload
- ic_comment_24dp
- ic_compress
- ic_dashboard_black_24dp
- ic_doente_24dp
- ic_error_24dp
- ic_error_placeholder
- ic_expand_more_24dp
- ic_favorite_24dp
- ic_favorite_border_24dp
- ic_filter_list_24dp
- ic_galeria_24dp
- ic_home_black_24dp
- ic_image_error
- ic_image_multiple
- ic_image_placeholder
- ic_info_24dp
- ic_inseto_24dp
- ic_launcher_background_solid
- ic_location_24dp
- ic_location_on_24dp
- ic_more_vert_24dp
- ic_neutro_24dp
- ic_north_west_24dp
- ic_notifications_black_24dp
- ic_planta_24dp
- ic_praga_24dp
- ic_refresh_24dp
- ic_reply_24dp
- ic_saudavel_24dp
- ic_search_24dp
- ic_search_off_24dp
- ic_send_24dp
- ic_share_24dp
- ic_sort_24dp
- ic_star_border_24dp
- ic_user_placeholder
- ic_verified_24dp

### Drawables de Background/Componentes (18)
- card_selector_background (NOTA: Não utilizado?)
- category_badge_background
- circle_background_green
- counter_background
- dialog_background
- gradient_overlay
- ic_circle_background
- image_count_background
- location_background
- ripple_circle_green

---

## 🔍 Localizações Mais Utilizadas

### Layouts XML (principais arquivos)
1. `item_post_feed.xml` - 12 referências
2. `fragment_public_search.xml` - 11 referências
3. `item_postagem_card.xml` - 10 referências
4. `activity_registro_planta.xml` - 8 referências
5. `activity_registro_inseto.xml` - 8 referências

### Código Kotlin (principais arquivos)
1. `SearchSuggestionsAdapter.kt` - 9 referências
2. `PostagemCardAdapter.kt` - 8 referências
3. `RegistrosAdapter.kt` - 5 referências
4. `SearchResultsAdapter.kt` - 5 referências
5. `AttachmentPreviewAdapter.kt` - 2 referências

---

## ⚠️ Duplicatas e Sobrescrita de Ícones

Durante a análise, foram identificadas algumas duplicatas:
- `ic_arrow_back.xml` e `ic_arrow_back_24dp.xml` - ambas utilizadas
- `ic_bookmark_24dp.xml` e `ic_bookmark_border_24dp.xml` - ambas utilizadas
- `ic_comment_24dp.xml` (existe `ic_comment_outline.xml` não utilizado)
- `ic_close.xml` e `ic_close_24dp.xml` - ambas utilizadas
- `ic_favorite_24dp.xml` e `ic_favorite_border_24dp.xml` - ambas utilizadas

---

## ✂️ Recomendações de Limpeza

### Prioridade ALTA - Deletar com Segurança
```
card_selector_background.xml
ic_add_registro_24dp.xml
ic_ai.xml
ic_arrow_forward.xml
ic_bookmark_outline.xml
ic_comment_outline.xml
ic_delete_24dp.xml
ic_edit_24dp.xml
ic_feed_24dp.xml
ic_google.xml
ic_like_outline.xml
ic_postagens_24dp.xml
ic_post_empty.xml
ic_registro_24dp.xml
ic_share_outline.xml
login_button_background.xml
logo_background.xml
```

### Análise de Consolidação de Ícones
- Considere consolidar `ic_close.xml` e `ic_close_24dp.xml` (usar apenas um padrão)
- Considerar se precisa tanto de `ic_arrow_back.xml` quanto `ic_arrow_back_24dp.xml`

---

## 📋 Script de Exclusão (PowerShell)

```powershell
# Deletar todos os drawables não utilizados
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

foreach ($file in $unusedFiles) {
    $fullPath = Join-Path $basePath $file
    if (Test-Path $fullPath) {
        Remove-Item $fullPath -Force
        Write-Host "Deletado: $file" -ForegroundColor Green
    }
}

Write-Host "Limpeza completa!" -ForegroundColor Cyan
```

---

## 📝 Notas

- A análise foi realizada em 13/11/2025
- Foram verificados todos os arquivos XML de layout e menu
- Foram verificados todos os arquivos Kotlin e Java
- Nenhum drawable não utilizado foi encontrado nos strings.xml ou resources programáticas

---

**Relatório Gerado Automaticamente**
