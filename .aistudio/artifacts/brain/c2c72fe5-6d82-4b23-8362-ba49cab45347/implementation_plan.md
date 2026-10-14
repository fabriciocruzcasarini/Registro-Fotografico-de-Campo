# 📋 Plano de Implementação: Salvamento Parcial & Regra de Envio do "Depois" (Concluído)

A funcionalidade de **salvamento de atividades em andamento sem a foto do DEPOIS** e as regras estritas de bloqueio de envio e exportação foram **implementadas e validadas com sucesso**.

---

## 🎯 Resumo das Modificações Realizadas

### 1. Validação Condicional & Salvamento Parcial (`FieldActivityViewModel.kt`)
- `validateForm(isSendingToWhatsApp: Boolean)`:
  - Se `isSendingToWhatsApp == true`, exige obrigatoriamente a foto do **DEPOIS** com mensagem: *"Para envio ao WhatsApp, a foto do DEPOIS é obrigatória."*
  - Se `isSendingToWhatsApp == false`, a foto do **DEPOIS** é opcional, permitindo salvar o registro pendente em ambos os modos (2 ou 3 fotos).
- `saveActivity(context, shareImmediatelyToWhatsApp)`:
  - Se `state.photoAfterUri == null`, atribui `photoAfterPath = ""` sem falhas e sem carimbar fotos inexistentes.
- `shareExistingActivity`:
  - Se `activity.photoAfterPath.isBlank()`, bloqueia o disparo e alerta: *"Envio bloqueado: adicione a foto do DEPOIS em [Editar] para enviar."*
- `exportSelectedPdf` & `exportSelectedCsv`:
  - Bloqueiam a exportação caso existam registros sem a foto do DEPOIS, orientando o usuário a concluí-los antes de gerar os relatórios.

### 2. Formulário Principal (`FormScreen.kt`)
- **Card da Foto "Depois":**
  - Título atualizado: `Foto DEPOIS (Obrigatória para WhatsApp)`.
  - Subtítulo explicativo: *"Obrigatória para envio via WhatsApp (pode salvar pendente sem ela)"*.
- **Ações Inferiores:**
  - Botão **"Salvar e Enviar no WhatsApp"** (ou *"Atualizar e Enviar no WhatsApp"*) fica desabilitado quando a foto do DEPOIS não estiver presente.
  - Exibição de aviso sutil abaixo do botão: *"Tire a foto do DEPOIS para liberar o envio ao WhatsApp."*
  - Botão **"Salvar para Envio Posterior"** permanece sempre liberado e funcional.

### 3. Visualização & Ações no Histórico (`HistoryScreen.kt`)
- **Card da Atividade (`ActivityHistoryCard`):**
  - Badge de status destacado em cor âmbar: `⏳ Aguardando Foto DEPOIS`.
  - Moldura da foto DEPOIS com placeholder amigável: ícone de câmera/adição e texto `DEPOIS (Pendente)`.
  - Botões **WhatsApp** e **PDF** desabilitados com tratamento de cores e feedback visual.
  - Botão **Editar** ganha destaque com cor primária e texto de ação: *"Completar Depois"*.

### 4. Modal de Fotos com Marca D'Água (`WatermarkPreviewModal.kt`)
- Se a atividade não possuir a foto do DEPOIS, exibe um cartão explicativo: *"Foto do DEPOIS ainda não registrada - Abra a edição do registro para tirar a foto de conclusão e liberar o envio."*
- Botões de envio direto e exportação PDF dentro do modal ficam desabilitados para atividades incompletas.

---

## 🧪 Status de Verificação
- Compilação Android: **Sucesso (0 erros)**
- Salvamento parcial: **Aprovado**
- Bloqueio de envio e exportação sem o "Depois": **Aprovado**
- Fluxo de conclusão posterior via edição: **Aprovado**
