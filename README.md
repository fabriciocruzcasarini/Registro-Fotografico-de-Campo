# 🚧 Registro de Campo - Sinalização Viária & Obras

> **Aplicativo Android Nativo** para apontamento técnico, registro fotográfico georreferenciado e geração de relatórios de serviços de sinalização e manutenção rodoviária.

![Android](https://img.shields.io/badge/Plataforma-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Linguagem-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Room Database](https://img.shields.io/badge/Banco%20de%20Dados-Room%20(SQLite)-FFA000?style=for-the-badge&logo=sqlite&logoColor=white)
![Versão](https://img.shields.io/badge/Vers%C3%A3o-1.0-blue?style=for-the-badge)

---

## 📱 Visão Geral

O **Registro de Campo** foi desenvolvido sob medida para encarregados, engenheiros e fiscais de obras rodoviárias. Ele permite documentar com precisão intervenções viárias (implantação de tachas refletivas, placas de regulamentação/advertência, pintura de faixas, legendagem e dispositivos de segurança) de forma rápida, segura e **100% offline**.

Cada registro fotográfico recebe automaticamente um **carimbo técnico indelével (marca d'água)** contendo todas as variáveis operacionais: quilometragem (KM), rodovia, sentido, pista, coordenadas de satélite (GPS) com precisão métrica, data/hora e nome do encarregado responsável.

---

## ✨ Principais Funcionalidades

### 1. 📋 Apontamento Técnico de Campo
- **Identificação da Obra:** Nome do Encarregado (preservado na sessão de trabalho).
- **Localização Rodoviária:** Rodovia (ex: *BR-101*, *SP-330*), Sentido (*Norte, Sul, Leste, Oeste*) e Tipo de Pista (*Principal, Marginal, Dispositivo*).
- **Trecho em KM:** Entrada com suporte a decimais (*ex: KM 120.5 ao 121.2*) e cálculo automático para serviços pontuais.
- **Tipificação Especializada:**
  - **Tachas Refletivas:** Tachas Monodirecionais, Bidirecionais, Cegas, etc.
  - **Placas de Sinalização:** Regulamentação, Advertência e Indicação (com código e texto).
  - **Demarcação Viária:** Seleção dinâmica de faixas (*LBO-D, LBO-E, LCO, etc.*), Eixo, Cadência e Descrição de Legendas.
- **Observações Gerais:** Campo livre para anotações técnicas e especificações de materiais (ex: uso de resina epóxi).

---

### 2. 📸 Registro Fotográfico & Marca D'Água Automática
- **Flexibilidade de Registro:** Escolha entre **2 Fotos** (*Antes* e *Depois*) ou **3 Fotos** (*Antes*, *Durante* e *Depois*).
- **Estampa Técnica Indelével (Watermark):**
  - **Faixa Superior:** Identificação destacada da etapa (*ANTES [Amarelo], DURANTE [Azul], DEPOIS [Verde]*), atividade e tipologia.
  - **Banner Inferior:** Rodovia, sentido, pista, faixa, cadência, KM, coordenadas GPS (Latitude/Longitude), precisão do sinal em metros, data/hora exata e responsável.
- **Gestão Eficiente de Memória:** Tratamento nativo com reciclagem explícita de Bitmaps (`Bitmap.recycle()`), prevenindo erros de esgotamento de memória (*OutOfMemoryError*) em sessões prolongadas.
- **Proteção Antissobreposição:** Ao editar apenas textos de um relatório salvo, as fotos originais são mantidas sem reaplicar carimbos sobrepostos.

---

### 3. ⏳ Fluxo Inteligente de Salvamento Parcial
- **Salvamento de Serviço em Andamento:** O operador pode registrar o *Antes* (e o *Durante*) e salvar o registro como **Pendente** no banco local sem exigir a foto do *Depois*.
- **Controle Estrito de Envio:**
  - O botão de envio para o **WhatsApp** só é liberado quando a foto do **DEPOIS** for devidamente anexada.
  - Registros incompletos recebem destaque visual no histórico: `⏳ Aguardando Foto DEPOIS`.
  - Ação rápida de conclusão: botão **"Completar Depois"** para reabrir o formulário diretamente.

---

### 4. 🛰️ Geolocalização Inteligente (GPS)
- Integração com o **Fused Location Provider** do Google Play Services e redundância via `LocationManager`.
- **Badge Visual de Precisão:**
  - 🟢 **Alta Precisão (±0-10m)**: Sinal de satélite ideal.
  - 🟡 **Precisão Média (±10-25m)**: Sinal satisfatório.
  - 🔴 **Baixa Precisão (>25m)**: Alerta de sinal fraco.
  - 🟠 **Coordenadas Estimadas (Offline / Sem GPS)**: Alerta com diálogo de confirmação antes do salvamento, prevenindo dados incorretos.

---

### 5. 📑 Exportação e Relatórios
- **Compartilhamento WhatsApp:** Disparo limpo enviando exclusivamente o conjunto de imagens estampadas via `ACTION_SEND_MULTIPLE`.
- **Relatório PDF Técnico:**
  - Layout formal multipáginas no padrão A4.
  - Cabeçalho institucional, tabela de atributos operacionais e galeria de fotos em alta definição.
  - Bloqueio inteligente: impede a geração de relatórios com atividades pendentes de finalização.
- **Planilha CSV (Excel):**
  - Exportação com delimitador de ponto e vírgula (`;`) e codificação **UTF-8 com BOM**, garantindo abertura perfeita no Microsoft Excel no Brasil sem caracteres corrompidos.
- **Limpeza Automática de Cache:** Rotina periódica que expurga relatórios temporários com mais de 7 dias, economizando armazenamento do aparelho.

---

### 6. 🗄️ Histórico e Banco de Dados Local
- **Room Database (SQLite) v7:**
  - Armazenamento 100% local no dispositivo (zero dependência de conexão à internet em campo).
  - Índices otimizados em `timestamp` e `isSent` para filtragem instantânea (*Todos, Pendentes e Enviados*).
  - **Modo de Seleção Múltipla:** Permite selecionar dezenas de itens para exportação unificada em lote ou limpeza.
  - **Exclusão Física:** Ao remover um registro, os arquivos `.jpg` em disco são excluídos para liberar espaço.

---

### 7. 🧭 Usabilidade & Navegação Android
- Suporte total ao **BackHandler** nativo:
  - Pressionar "Voltar" em telas secundárias (*Salvos* ou *Ajustes*) retorna à tela principal de registro.
  - Pressionar "Voltar" no modo de seleção cancela a seleção em lote sem fechar o app.
  - Pressionar "Voltar" durante a edição exibe aviso de confirmação, protegendo o operador contra perdas acidentais de dados.
- Teclado decimal nos campos de quilometragem para compatibilidade total com dispositivos Samsung, Xiaomi, Motorola e Pixel.

---

## 🏗️ Arquitetura e Tecnologias

O projeto adota os padrões modernos recomendados pelo Google para desenvolvimento Android:

- **Linguagem:** Kotlin 100%
- **Interface Declarativa:** Jetpack Compose com Design System **Material Design 3 (M3)**
- **Arquitetura:** MVVM (Model-View-ViewModel) + Repository Pattern + Clean Architecture
- **Gerenciamento de Estado:** Kotlin Coroutines & `StateFlow` / `SharedFlow`
- **Banco de Dados:** AndroidX Room Database com migrações versionadas
- **Carregamento de Imagens:** Coil Compose
- **Injeção de Dependências:** Factory Pattern / Constructor Injection
- **Compatibilidade:** Android 7.0 (API 24) ou superior (Target SDK 35)

---

## 📂 Estrutura do Projeto

```
app/src/main/java/com/example/
├── MainActivity.kt               # Ponto de entrada, navegação por abas e gestão de permissões
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt        # Banco de dados Room e histórico de migrações (v1 -> v7)
│   │   ├── FieldActivityDao.kt   # Consultas e operações SQL otimizadas
│   │   └── PreferencesManager.kt # Armazenamento de preferências do encarregado
│   ├── model/
│   │   └── FieldActivity.kt      # Entidade de dados com atributos de sinalização viária
│   └── repository/
│       └── FieldActivityRepository.kt
├── ui/
│   ├── components/
│   │   ├── PhotoCaptureCard.kt   # Componente de disparo e remoção de fotos
│   │   └── WatermarkPreviewModal.kt # Modal de visualização detalhada com carimbos
│   ├── screens/
│   │   ├── FormScreen.kt         # Formulário dinâmico de apontamento e seções de fotos
│   │   ├── HistoryScreen.kt      # Listagem, filtros, seleção em lote e ações rápidas
│   │   ├── SettingsScreen.kt     # Configurações de operador padrão e contagem de fotos
│   │   └── SplashScreen.kt       # Tela de abertura profissional
│   ├── theme/                    # Paleta de cores, tipografia e formas Material 3
│   └── viewmodel/
│       └── FieldActivityViewModel.kt # Orquestração de regras de negócio e validações
└── util/
    ├── CsvReportExporter.kt      # Gerador de planilhas CSV com codificação UTF-8 BOM
    ├── LocationHelper.kt         # Provedor unificado de geolocalização com fallbacks
    ├── PdfReportExporter.kt      # Motor de renderização de PDF nativo em A4
    ├── ShareUtil.kt              # Integração de compartilhamento com WhatsApp
    └── WatermarkUtil.kt          # Motor gráfico de desenho de marcas d'água em Canvas
```

---

## 🚀 Como Compilar e Executar

### Pré-requisitos
- **Android Studio** (Hedgehog | 2023.1.1 ou superior recomendado)
- **JDK 17**
- Dispositivo Android físico ou Emulador com API 24+

### Passo a Passo
1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/registro-campo-sinalizacao.git
   ```
2. Abra o projeto no **Android Studio**.
3. Aguarde o sincronismo do Gradle (*Sync Project with Gradle Files*).
4. Conecte seu aparelho via USB com a Depuração Ativada.
5. Clique em **Run 'app'** (`Shift + F10`) para instalar o APK.

---

## 🔒 Permissões Utilizadas

- `android.permission.CAMERA`: Captura das fotos de comprovação de serviços.
- `android.permission.ACCESS_FINE_LOCATION`: Obtenção das coordenadas exatas de satélite para o carimbo técnico.
- `android.permission.ACCESS_COARSE_LOCATION`: Localização aproximada para contingência em caso de sinal fraco.

---

## 📄 Licença

Distribuído sob a licença proprietária/corporativa para operações de engenharia e sinalização viária. Consulte os termos internos da sua organização para distribuição.

---
*Desenvolvido com foco em alta performance, robustez em campo e confiabilidade técnica.*
