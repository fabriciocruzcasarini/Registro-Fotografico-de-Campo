# 📑 Relatório de Auditoria de QA Sênior - Apontamento de Campo

**Data:** 27 de Setembro de 2026  
**Avaliador:** Engenheiro de QA Sênior  
**Versão do Banco de Dados:** v7 (Room com índices)  
**Objetivo:** Avaliação de conformidade, estabilidade, desempenho sob estresse de baixa memória, ausência de GPS e integridade de dados após as últimas correções.

---

## 1. 📊 Matriz de Risco e Severidade

| ID | Área / Componente | Severidade | Classificação | Descrição do Ponto Auditado |
|---|---|:---:|:---:|---|
| **QA-01** | `FieldActivityViewModel.kt` | 🟠 **Alta** | Risco de Regressão Visual | **Dupla Estampa na Edição:** Ao editar campos textuais de um registro já salvo sem substituir as fotos, o `WatermarkUtil` recebe o arquivo que já possui faixas de carimbo, podendo sobrepor novas faixas sobre as existentes caso o registro seja salvo novamente. |
| **QA-02** | `FieldActivityRepository.kt` | 🟡 **Média** | Gerenciamento de Armazenamento | **Arquivos Órfãos em Disco:** A exclusão de um registro no banco Room remove a tupla do SQLite, porém os arquivos físicos `.jpg` gerados em `DIRECTORY_PICTURES` continuam ocupando armazenamento interno do dispositivo. |
| **QA-03** | `CsvReportExporter.kt` | 🟡 **Média** | Retenção de Cache | **Paridade com Exportador PDF:** O expurgo automático de relatórios com mais de 7 dias foi aplicado com sucesso em `PdfReportExporter.kt`, mas não foi replicado para o cache de arquivos `.csv` temporários em `cacheDir/reports/`. |
| **QA-04** | `HistoryScreen.kt` | 🟢 **Baixa** | Sincronização de Estado de UI | **IDs Residuais na Seleção:** Caso um registro selecionado no modo em lote seja excluído individualmente ou filtrado, o conjunto `selectedActivityIds` mantém a referência ao ID já não mais visível. |
| **QA-05** | `FormScreen.kt` | 🟢 **Baixa** | Usabilidade / Feedback Visual | **Indicador Visual Antecipado:** O diálogo de aviso de GPS ao salvar funciona perfeitamente, mas a barra de status de precisão poderia destacar antecipadamente em tom âmbar/amarelo quando o estado `isLocationFallback` estiver ativo antes do toque no botão salvar. |

---

## 2. 🔍 Análise Detalhada dos Fluxos Auditados

### 2.1. Gestão de Memória e Processamento de Imagens
- **Status:** ✅ **Aprovado com Ressalva (QA-01)**
- **Pontos Positivos:** O uso de blocos `try-finally` com chamada explícita de `Bitmap.recycle()` em `WatermarkUtil.kt` eliminou com sucesso o risco de `OutOfMemoryError` em disparos contínuos da câmera.
- **Ressalva:** Necessidade de verificar se o URI de entrada já aponta para uma imagem pré-processada durante edições de histórico.

### 2.2. Resiliência de Conectividade e GPS
- **Status:** ✅ **Aprovado**
- **Pontos Positivos:** A transição entre busca de satélites e fallback para localização em cache/padrão não trava a UI da aplicação. O diálogo de confirmação impede que registros com coordenadas imprecisas sejam gravados sem a ciência do operador.

### 2.3. Persistência de Dados (Room Database)
- **Status:** ✅ **Aprovado**
- **Pontos Positivos:** A migração `MIGRATION_6_7` foi validada com sucesso, adicionando índices em `timestamp` e `isSent`, garantindo fluidez e rápida resposta na filtragem e paginação da listagem do Histórico.

### 2.4. Integração com WhatsApp e Compartilhamento
- **Status:** ✅ **Aprovado**
- **Pontos Positivos:** A declaração de `<queries>` no `AndroidManifest.xml` garante compatibilidade total com o Android 11+ (API 30+), permitindo envio direto sem telas intermediárias. A remoção de legendas textuais em `ShareUtil.kt` garantiu envio limpo focado exclusivamente nas fotos.

### 2.5. Exportação em Lote (PDF e CSV)
- **Status:** ✅ **Aprovado com Ressalva (QA-03)**
- **Pontos Positivos:** O indicador modal de progresso (`AlertDialog`) bloqueia interações simultâneas e previne cliques duplicados durante a renderização pesada de documentos.

---

## 3. 🏁 Conclusão do Relatório de QA
A aplicação demonstrou excelente robustez arquitetural, bom isolamento de estado via ViewModels e UI responsiva e fluida com Jetpack Compose e Material 3. Nenhuma falha fatal ou crash de inicialização foi detectado. Os pontos catalogados acima servem como registro técnico de auditoria para acompanhamento futuro.
