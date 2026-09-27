# 📌 Backlog de Issues para GitHub - Apontamento de Campo

Este documento contém todas as issues levantadas pela auditoria de QA, formatadas no padrão do GitHub (com título, labels, severidade, contexto técnico e critérios de aceite).

---

## Issue #1: [BUG][CRITICAL] Risco de OutOfMemoryError (OOM) no processamento de fotos e marca d'água
* **Labels:** `bug`, `critical`, `crash`, `performance`, `backend-local`
* **Severidade:** 🔴 Crítica (Blocker)
* **Componente Afetado:** `com.example.util.WatermarkUtil.kt`

### Descrição do Problema
O método `createWatermarkedPhoto` e `applyWatermarkToBitmap` decodifica bitmaps em resolução alta (`ARGB_8888`), gerando cópias de trabalho (`bitmap.copy`) e matrizes de rotação sem invocar `bitmap.recycle()` nos objetos intermediários. Com fotos tiradas em dispositivos modernos (câmeras de 12MP a 64MP), o salvamento de uma atividade com 3 fotos (Antes, Durante e Depois) estoura a memória do heap Android, causando crash imediato do aplicativo.

### Passos para Reproduzir
1. Configurar o app para 3 fotos por registro nas Configurações.
2. Tirar 3 fotos reais em alta resolução usando a câmera do aparelho.
3. Preencher os dados obrigatórios e tocar em "Salvar".
4. Observar estouro de memória no Logcat (`OutOfMemoryError`).

### Comportamento Esperado
- Os bitmaps intermediários devem ser reciclados explicitamente logo após o processamento.
- Usar `inSampleSize` eficiente e garantir liberação de memória em bloco `finally`.

---

## Issue #2: [BUG][CRITICAL] Fallback silencioso de GPS atribui coordenadas de São Paulo em falta de sinal
* **Labels:** `bug`, `critical`, `compliance`, `gps`
* **Severidade:** 🔴 Crítica (Blocker)
* **Componente Afetado:** `com.example.util.LocationHelper.kt`

### Descrição do Problema
Se o sinal do GPS estiver desabilitado, sem precisão ou a permissão não tiver sido concedida, o método `tryLocationManagerFallback` retorna silenciosamente o ponto estático da Praça da Sé em São Paulo (`-23.550520, -46.633308`). O sistema grava essas coordenadas no banco e na marca d'água da foto sem alertar o encarregado. Em obras rodoviárias reais fora de SP, gera dados cadastrais falsos perante a fiscalização (DER/ANTT).

### Passos para Reproduzir
1. Desligar a localização do dispositivo ou revogar a permissão de GPS.
2. Abrir o formulário e salvar uma atividade.
3. Verificar que o registro gravou as coordenadas de São Paulo sem nenhum alerta ao usuário.

### Comportamento Esperado
- Se o GPS não estiver disponível, exibir aviso claro e bloqueio de confirmação (ex: "Sinal de GPS não localizado. Deseja tentar novamente ou apontar sem GPS?").
- Nunca estampar coordenadas mock como se fossem GPS real.

---

## Issue #3: [BUG][CRITICAL] Falha na detecção e abertura direta do WhatsApp no Android 11+
* **Labels:** `bug`, `critical`, `android-11+`, `integration`, `whatsapp`
* **Severidade:** 🔴 Crítica (Blocker)
* **Componente Afetado:** `com.example.util.ShareUtil.kt`, `AndroidManifest.xml`

### Descrição do Problema
O método `ShareUtil.isPackageInstalled` usa `getPackageInfo("com.whatsapp", 0)`. A partir do Android 11 (API 30), o sistema operacional exige a declaração prévia de pacotes na tag `<queries>` do manifesto. Como o `AndroidManifest.xml` não possui as tags do WhatsApp e WhatsApp Business, o aplicativo não detecta o WhatsApp instalado e cai sempre no seletor genérico do sistema.

### Solução Proposta
Adicionar no `AndroidManifest.xml`:
```xml
<queries>
    <package android:name="com.whatsapp" />
    <package android:name="com.whatsapp.w4b" />
</queries>
```

---

## Issue #4: [BUG][HIGH] Texto da coluna "Local" sobrepõe GPS e Status no relatório PDF
* **Labels:** `bug`, `high`, `export-pdf`, `ui`
* **Severidade:** 🟠 Alta
* **Componente Afetado:** `com.example.util.PdfReportExporter.kt`

### Descrição do Problema
Na segunda linha do card de atividade no PDF gerado (`infoY`), a coluna 1 (`col1X`) desenha o texto de especificações (`specText`: Faixa, Eixo, Cadência, Texto da Placa). Por não possuir limite de largura (`maxWidth`) nem quebra de texto, quando a legenda ou especificação é longa, o texto é desenhado por cima das coordenadas GPS (`col2X`) e do status (`col3X`).

### Comportamento Esperado
- Remover a coluna "Status" (Pendente / Enviado WhatsApp), pois é irrelevante em relatórios impressos para auditoria de obra.
- Expandir a largura da coluna "Local" e aplicar quebra/truncamento para não colidir com o GPS.

---

## Issue #5: [FEATURE][HIGH] Adicionar mensagem técnica no compartilhamento de fotos para WhatsApp
* **Labels:** `enhancement`, `high`, `integration`, `whatsapp`
* **Severidade:** 🟠 Alta
* **Componente Afetado:** `com.example.util.ShareUtil.kt`

### Descrição do Problema
Ao enviar as fotos de um apontamento para o WhatsApp via `ACTION_SEND_MULTIPLE`, o app passa apenas as imagens no `EXTRA_STREAM`, mas não anexa mensagem de texto no `Intent.EXTRA_TEXT`. As fotos chegam no WhatsApp sem descrição da obra (Rodovia, KM, Sentido, Pista, Atividade, Encarregado).

### Comportamento Esperado
Anexar texto formatado no `Intent.EXTRA_TEXT` com o resumo da atividade, facilitando a identificação imediata pela equipe de fiscalização no grupo.

---

## Issue #6: [BUG][HIGH] Nome do encarregado é limpo após salvar atividade consecutiva
* **Labels:** `bug`, `high`, `ux`, `form`
* **Severidade:** 🟠 Alta
* **Componente Afetado:** `com.example.ui.viewmodel.FieldActivityViewModel.kt`

### Descrição do Problema
Após o método `saveActivity`, o formulário chama `createDefaultFormState()`. Se o usuário não configurou previamente um "Nome Padrão" nas Configurações, o campo `operatorName` é redefinido para vazio `""`. Em uma jornada onde o mesmo encarregado faz dezenas de lançamentos por dia, ele é forçado a redigitar o nome completo em cada lançamento.

### Comportamento Esperado
Preservar o último `operatorName` digitado na sessão atual ao resetar o formulário para um novo registro.

---

## Issue #7: [IMPROVEMENT][MEDIUM] Validação de KM Inicial x KM Final para serviços pontuais
* **Labels:** `enhancement`, `medium`, `validation`, `form`
* **Severidade:** 🟡 Média
* **Componente Afetado:** `com.example.ui.viewmodel.FieldActivityViewModel.kt`

### Descrição do Problema
O formulário exige obrigatoriamente o preenchimento de `kmStart` e `kmEnd`. Serviços pontuais (como placas de regulamentação ou tachões isolados) possuem apenas um marco quilométrico único.

### Comportamento Esperado
- Permitir que em atividades pontuais o KM Final seja opcional ou preenchido automaticamente com o mesmo valor do KM Inicial.

---

## Issue #8: [PERFORMANCE][MEDIUM] Falta de diálogo de progresso durante exportação em lote de PDF
* **Labels:** `performance`, `medium`, `ui`, `export-pdf`
* **Severidade:** 🟡 Média
* **Componente Afetado:** `com.example.ui.screens.HistoryScreen.kt`

### Descrição do Problema
A exportação em lote carrega e renderiza dezenas de arquivos pesados em background. Atualmente, o feedback visual é apenas um pequeno indicador no canto da tela, permitindo que o usuário dê múltiplos cliques no botão ou navegue entre abas enquanto o PDF está sendo gerado.

### Comportamento Esperado
Exibir diálogo modal bloqueante com `CircularProgressIndicator` e texto de status ("Gerando PDF... Aguarde").

---

## Issue #9: [MAINTENANCE][MEDIUM] Limpeza periódica de relatórios antigos no diretório de cache
* **Labels:** `maintenance`, `medium`, `storage`
* **Severidade:** 🟡 Média
* **Componente Afetado:** `com.example.util.PdfReportExporter.kt`, `CsvReportExporter.kt`

### Descrição do Problema
Cada exportação de PDF e CSV gera novos arquivos em `context.cacheDir/reports/`. Não existe rotina de expurgo de arquivos com mais de 7 dias, acumulando espaço desnecessário em aparelhos de campo de memória reduzida.

---

## Issue #10: [REFACTOR][LOW] Modularizar o componente FormScreen.kt (> 1.300 linhas)
* **Labels:** `refactor`, `low`, `code-quality`
* **Severidade:** 🟢 Baixa
* **Componente Afetado:** `com.example.ui.screens.FormScreen.kt`

### Descrição do Problema
O arquivo `FormScreen.kt` ultrapassa 1.300 linhas, acumulando lógica de seletores de rodovia, faixas dinâmicas, acordeons de coordenadas, cards de fotos e modais em um único arquivo, dificultando manutenções futuras e testes unitários.
