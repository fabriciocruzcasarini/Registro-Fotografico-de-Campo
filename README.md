<div align="center">

# 📍 Registro Fotográfico de Campo

**Aplicativo Android de apontamento de serviços de sinalização viária e conservação rodoviária.**

Registre serviços executados em campo com foto geolocalizada, marca d'água
profissional automatizada e exportação de relatórios técnicos em PDF e CSV.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF.svg?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.09-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Android Gradle Plugin](https://img.shields.io/badge/AGP-9.1.1-3DDC84.svg?logo=android)](https://developer.android.com/build)
[![Gradle](https://img.shields.io/badge/Gradle-9.3.1-02303A.svg?logo=gradle)](https://gradle.org)
[![Min SDK](https://img.shields.io/badge/minSdk-24-34A853.svg?logo=android)](https://developer.android.com/tools/releases/platforms)
[![Target SDK](https://img.shields.io/badge/targetSdk-36-34A853.svg?logo=android)](https://developer.android.com/tools/releases/platforms)
[![Room](https://img.shields.io/badge/Room-2.7.0-2E7D32.svg?logo=sqlite)](https://developer.android.com/training/data-storage/room)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

</div>

---

## 📸 Preview

<div align="center">

| Formulário de Campo | Histórico | Relatório PDF |
|:---:|:---:|:---:|
| Cadastro com GPS e fotos | Filtros e seleção múltipla | Layout A4 técnico |
| *Em Breve* | *Em Breve* | *Em Breve* |

</div>

> 💡 **Dica:** adicione screenshots reais em `docs/` e referencie-os aqui com
> `![Formulário](docs/formulario.png)`.

---

## 📖 Sobre o Projeto

O **Registro Fotográfico de Campo** nasceu para resolver um problema real de
construtoras e equipes de manutenção rodoviária: o apontamento de serviços de
sinalização (tachas, placas, defensas, pintura) era feito em papel ou em
planilhas, perdendo-se as evidências fotográficas e as coordenadas do local.

O app resolve isso em três etapas no próprio canteiro de obras:

```
📍 Registrar  ──▶  🖼️ Fotografar  ──▶  📤 Enviar
   dados + GPS       com marca d'água     PDF / CSV / WhatsApp
```

Cada foto é automaticamente **carimbada** com os dados técnicos do serviço
(rodovia, KM, faixa, tipo de elemento, GPS, data e hora), garantindo que a
evidência fotográfica seja autêntica e rastreável. Ao final do dia, o
engenheiro gera um **relatório técnico em PDF** pronto para anexar à
medição ou ordem de serviço, ou uma **planilha CSV** para análise.

### 🏷️ Domínio de aplicação

O vocabulário e os fluxos seguem a terminologia de **sinalização viária e
conservação rodoviária** (DNIT / CONTRAN), e não de um app de formulários
genérico:

| Conceito | Significado no app |
|:---|:---|
| **Tacha** | Tachão refletivo usado para delimitar faixas (mono/bi, branca/amarela/vermelha) |
| **Defensa** | Barreira de contenção lateral (guard-rail, New Jersey) |
| **Placa** | Sinalização vertical — Regulamentação, Advertência ou Indicação |
| **Faixa / Eixo** | Posição longitudinal do elemento na pista |
| **Cadência** | Intervalo de instalação entre elementos (ex.: `40m`) |
| **Km Inicial / Final** | Trecho da rodovia onde o serviço foi executado |

---

## ✨ Funcionalidades

### 📷 Captura Fotográfica com Marca d'Água Automática

- **3 pontos de medição** — `ANTES` (amarelo), `DURANTE` (azul) e `DEPOIS` (verde)
- Fita de identificação colorida no topo + painel técnico fixado no rodapé
- Dados estampados na própria imagem: encarregado, rodovia, sentido, tipo de
  pista, tipo de tacha/placa, faixa, eixo, cadência, KM, **lat/long** e data/hora
- Correção automática de **orientação EXIF** (fotos deitadas em pé)
- Downsample inteligente para **1280px** — economiza memória sem perder nitidez
- Modo **2 ou 3 fotos** configurável nas preferências

### 🧭 Geolocalização

- Captura de **lat/long** via `FusedLocationProvider` (Google Play Services)
- Precisão do sinal GPS exibida em tempo real na barra de status
- Fallback para o último ponto conhecido quando o sinal está fraco

### 📝 Formulário Técnico Inteligente

- **Campos condicionais**: tipos de tacha aparecem só em atividades de tacha;
  campos de placa (tipo, código, texto) só em atividades de placa
- **Faixas dinâmicas** por tipo de atividade (ex.: `LEGENDA` libera a
  descrição da legenda)
- **8 atividades** suportadas: Implantação/Remoção de Tacha, Pintura
  Mecânica/Manual, Implantação/Remoção de Defensa, Implantação/Remoção de Placa
- Autosave em memória durante o preenchimento e **modo de edição** completo
  de registros já salvos

### 🗄️ Persistência Offline

- **100% offline-first** com Room + Flow — nada depende de rede
- Histórico completo com **filtros** por Todos / Pendentes / Enviados
- **Seleção múltipla** para exportação em lote
- Contador de pendências exibido como *badge* na barra de navegação

### 📤 Exportação e Compartilhamento

| Formato | Detalhes |
|:---|:---|
| **PDF** | Layout A4 técnico com cabeçalho institucional, paginação automática, moldura de cada foto e rodapé datado |
| **CSV** | UTF-8 com BOM + separador `;` (padrão Excel Brasil) — abre acentos corretos sem passar por wizard de importação |
| **WhatsApp** | Envio direto das fotos com carimbo via `ACTION_SEND_MULTIPLE`, com fallback para WhatsApp Business e share sheet nativo |

> 🟢 O relatório PDF é gerado **nativamente** com `android.graphics.pdf.PdfDocument`
> — sem dependências de terceiros, sem servidor, ~0 KB de overhead.

---

## 🏗️ Arquitetura

Projeto **single-module** com separação em camadas por pacote, seguindo o
princípio de **Clean Architecture simplificado** para um app de porte médio:

```
┌─────────────────────────────────────────────────────────┐
│  UI (Jetpack Compose)                                    │
│  MainActivity · FormScreen · HistoryScreen               │
│  SettingsScreen · SplashScreen · components/ · theme/    │
└───────────────────────┬─────────────────────────────────┘
                        │ StateFlow / SharedFlow
┌───────────────────────▼─────────────────────────────────┐
│  ViewModel                                              │
│  FieldActivityViewModel · FormUiState · UiEvent         │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│  Data                                                    │
│  Repository → DAO (Room) → AppDatabase                  │
│  PreferencesManager (SharedPreferences)                  │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│  Utils (motor de relatório)                              │
│  WatermarkUtil · PdfReportExporter                      │
│  CsvReportExporter · ShareUtil · LocationHelper         │
└─────────────────────────────────────────────────────────┘
```

### Decisões técnicas notáveis

| Decisão | Motivo |
|:---|:---|
| **StateFlow** em vez de LiveData | Nativo do Kotlin, integration com `collectAsState()` e suporte a `MutableStateFlow.update {}` para atualizações atômicas |
| **`SharedFlow` para `UiEvent`** | Toasts e confirmações são **eventos** (disparados uma única vez), não **estado** — o ViewModel não deve reemitir no restart de config |
| **Sem injeção de dependência** | App de um módulo: a construção manual no `MainActivity` (`ViewModel.Factory`) mantém o build rápido e o código explícito |
| **`PdfDocument` nativo** | Gera PDFs multi-página com controle total de layout sem adicionar ~1 MB de dependência |
| **Geometria responsiva no PDF** | O layout calcula altura dos cards e quebra de página **antes** de desenhar, garantindo paginação sem cortes |
| **BOM `;` + BOM UTF-8 no CSV** | O Excel brasileiro usa `;` como separador e interpreta UTF-8 sem BOM como latin-1, corrompendo os acentos |
| **URI sempre persistido como String** | O `Uri` do Android é parcelável, mas não é serializável de forma estável entre versões de API — o app persiste o **caminho absoluto** |

---

## 🛠️ Stack Técnica

| Camada | Tecnologia | Versão |
|:---|:---|:---|
| Linguagem | Kotlin | 2.2.10 |
| Build | Android Gradle Plugin | 9.1.1 |
| Build | Gradle (wrapper) | 9.3.1 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| UI | `androidx.activity:activity-compose` | 1.10.1 |
| Arquitetura | Lifecycle (Runtime + ViewModel + Compose) | 2.8.7 |
| Banco de dados | Room | 2.7.0 |
| Processamento | Kotlin Coroutines | 1.10.2 |
| Imagens | Coil | 2.7.0 |
| Localização | Play Services Location | 21.3.0 |
| EXIF | `androidx.exifinterface` | 1.3.7 |
| Testes | JUnit · Robolectric · Coroutines Test | 4.13.2 · 4.16.1 · 1.10.2 |
| Testes visuais | Roborazzi | 1.59.0 |
| Build config | Secrets Gradle Plugin | 2.0.1 |

**Configuração do SDK:** `minSdk 24` (Android 7.0) · `targetSdk 36` · `compileSdk 36.1`

---

## 📁 Estrutura de Pastas

```
Registro-Fotografico-de-Campo/
├── app/
│   ├── build.gradle.kts              # Configuração do módulo
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml    # Permissões e FileProvider
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt    # Atividade raiz + navegação por abas
│       │   │   ├── data/
│       │   │   │   ├── db/            # AppDatabase, FieldActivityDao (Room)
│       │   │   │   ├── local/         # PreferencesManager (SharedPreferences)
│       │   │   │   ├── model/         # FieldActivity (Entity)
│       │   │   │   └── repository/    # FieldActivityRepository
│       │   │   ├── ui/
│       │   │   │   ├── components/    # PhotoCaptureCard, WatermarkPreviewModal
│       │   │   │   ├── screens/       # Form, History, Settings, Splash
│       │   │   │   ├── theme/         # Color, Theme, Type
│       │   │   │   └── viewmodel/     # FieldActivityViewModel + FormUiState
│       │   │   └── util/              # Watermark, PDF, CSV, Share, GPS
│       │   └── res/                   # Ícones, temas, strings, file_paths
│       ├── test/                      # Testes unitários (JUnit, Robolectric)
│       └── androidTest/               # Testes instrumentados (Espresso)
├── gradle/
│   ├── libs.versions.toml             # Version Catalog (fonte única de versões)
│   └── wrapper/                       # Gradle Wrapper
├── build.gradle.kts                   # Build script raiz
├── settings.gradle.kts
├── gradle.properties
└── .env.example                       # Segredos (não versionar o .env!)
```

---

## 🚀 Como Executar

### Pré-requisitos

| Ferramenta | Versão |
|:---|:---|
| **Android Studio** | Ladybug ou superior (suporte a AGP 9.x) |
| **JDK** | 17 ou superior (requisito do AGP 9.x) |
| **Android SDK** | Platform 36 + SDK Build-Tools mais recente |

> ⚠️ O `compileSdk` usa um **minor API level** (`36.1`). Instale o *Android 16
> QPR1* via **SDK Manager → SDK Platforms**.

### 1️⃣ Clonar o repositório

```bash
git clone https://github.com/<seu-usuario>/Registro-Fotografico-de-Campo.git
cd Registro-Fotografico-de-Campo
```

### 2️⃣ Criar o keystore de debug

O build de debug usa um keystore local (`debug.keystore`), que está no
`.gitignore` por segurança. **Ele precisa ser gerado localmente:**

```bash
keytool -genkeypair -v \
  -keystore debug.keystore \
  -storepass android \
  -alias androiddebugkey \
  -keypass android \
  -dname "CN=Android Debug,O=Android,C=US" \
  -keyalg RSA -keysize 2048 -validity 10000
```

> Sem esse arquivo, o build de debug falha com `Keystore file not found`.

### 3️⃣ Configurar o `.env` (opcional)

```bash
cp .env.example .env
```

O app **não exige segredos para rodar**. A configuração só é necessária para
builds *release*:

```bash
# .env
KEYSTORE_PATH=/caminho/absoluto/para/my-upload-key.jks
STORE_PASSWORD=your_store_password
KEY_ALIAS=upload
KEY_PASSWORD=your_key_password
```

> 🔐 O `.env` está no `.gitignore` e é lido pelo *Secrets Gradle Plugin*.
> **Nunca** commite credenciais neste arquivo.

### 4️⃣ Compilar e instalar

```bash
# Build de debug
./gradlew assembleDebug

# Instalar em um dispositivo/emulador conectado
./gradlew installDebug
```

**Ou pelo Android Studio:** abra a pasta do projeto e pressione `▶ Run`.

### Comandos úteis

```bash
./gradlew clean                        # Limpar build
./gradlew assembleRelease              # APK de release (assinado)
./gradlew bundleRelease                # AAB para Play Store
./gradlew test                         # Testes unitários
./gradlew recordRoborazziDebug         # Gravar screenshots de teste
./gradlew verifyRoborazziDebug         # Comparar screenshots
```

---

## 🔐 Permissões

O app solicita **duas permissões em runtime** na primeira abertura:

| Permissão | Uso |
|:---|:---|
| `CAMERA` | Capturar as fotos do serviço |
| `ACCESS_FINE_LOCATION` | Registrar as coordenadas do ponto de execução |

Ambas são **opcionais na instalação** (`required="false"` nos *features*): o app
ainda instala em dispositivos sem câmera ou sem GPS, e o formulário continua
funcionando — apenas os campos afetados ficam indisponíveis.

A câmera e o GPS são tratados de forma adaptativa, e o app pede as permissões
somente quando ainda não foram concedidas.

---

## 🧪 Testes

O projeto vem configurado com uma pilha de testes completa:

| Tipo | Ferramenta | Escopo |
|:---|:---|:---|
| **Unitário** | JUnit 4 | Lógica de negócio e mapeamento de estado |
| **Android local** | Robolectric | Componentes Compose sem precisar de emulador |
| **Visual (screenshot)** | Roborazzi | Regressão visual de telas |
| **Instrumentado** | Espresso + Compose UI Test | Fluxos em dispositivo real |
| **Coroutines** | `kotlinx-coroutines-test` | Determinismo de `viewModelScope` |

```bash
./gradlew test                  # Testes unitários + Robolectric
./gradlew connectedAndroidTest  # Requer dispositivo conectado
```

---

## 📊 Permissões de Build

| Item | Configuração |
|:---|:---|
| `applicationId` | `com.aistudio.registrocampo.app.hitgtd` |
| `namespace` | `com.example` |
| `versionName` | `1.0` (`versionCode` 1) |
| R8 / minificação | **Desativado** no release |
| Java/Kotlin bytecode | `VERSION_11` |

> 🔎 **Dica de otimização:** o release está com `isMinifyEnabled = false`.
> Para reduzir significativamente o tamanho do APK antes de publicar, ative
> `isMinifyEnabled = true`, `isShrinkResources = true` e ative o
> `dependenciesInfo { includeInBundle = true }` — já configurado — para reduzir
> o *download* da Play Store via *deliverable-by-split*.

---

## 🗺️ Roadmap

- [ ] Sincronização com backend / API REST
- [ ] Exportação de KML/KMZ para visualização no Google Earth
- [ ] Assinatura digital dos relatórios PDF
- [ ] Modo offline com fila de envio automático
- [ ] Print da atividade em tela cheia (captura via `View.draw`)
- [ ] Escolha de template de marca para o PDF
- [ ] Relatórios consolidados por trecho / por frente de serviço

---

## 🛡️ Observações de Segurança

- Keystores e senhas **nunca** entram no repositório (`.gitignore` + `.env`)
- `android:allowBackup="true"` com regras de exclusão — revise
  `backup_rules.xml` / `data_extraction_rules.xml` antes de publicar em produção
- Fotos são gravadas em **armazenamento específico do app**
  (`getExternalFilesDir`), inacessíveis por outros apps sem permissão
- O compartilhamento usa `FileProvider` com URIs temporárias e
  `FLAG_GRANT_READ_URI_PERMISSION` — **sem** `MANAGE_EXTERNAL_STORAGE`

---

## 🤝 Contribuição

Contribuições são bem-vindas!

1. Faça um **fork** do projeto
2. Crie um branch para sua feature: `git checkout -b feat/nova-funcionalidade`
3. Faça suas alterações e garanta que os testes passam: `./gradlew test`
4. Commite seguindo o padrão **Conventional Commits**:
   ```bash
   git commit -m "feat(watermark): adiciona carimbo customizável"
   ```
5. Abra um **Pull Request** descrevendo as mudanças

**Tipos de commit:** `feat` · `fix` · `refactor` · `docs` · `test` · `chore`

---

## 📄 Licença

Este projeto está protegido por copyright. **A licença ainda não foi
definida** — adicione um arquivo `LICENSE` antes de publicar.

Sugestões usuais para projetos como este:

- **GPL-3.0** — obras derivadas obrigadas a abrir o código

---

<div align="center">

**Feito com ☕ e muito pó de tinta por quem trabalha em campo.**

<sub>
Desenvolvido com Kotlin + Jetpack Compose · 100% offline-first
</sub>

</div>
