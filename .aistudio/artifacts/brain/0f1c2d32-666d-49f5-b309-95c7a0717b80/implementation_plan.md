# Plano de Implementação: Ajuste da Resolução das Fotos para HD 720p

Ajuste do pipeline de processamento fotográfico para o padrão **HD 720p** (1280x720 / 1280x960), otimizando a velocidade de processamento, reduzindo o tempo de salvamento e facilitando o compartilhamento instantâneo no WhatsApp e relatórios em PDF.

---

## Decisões Confirmadas

- **Padrão de Resolução**: **HD 720p** (máximo de 1280 px de largura por 960 px de altura, mantendo a proporção original do sensor da câmera).
- **Qualidade de Compressão JPEG**: 88% a 90% (proporciona tamanho de arquivo entre ~250 KB e ~450 KB por foto, com preservação da nitidez dos textos técnicos e placas).
- **Compatibilidade com Marca D'Água**: Escalonamento proporcional da faixa superior (*ANTES/DURANTE/DEPOIS*) e da faixa de dados inferior para visualização nítida na resolução HD.

---

## 1. Modificações Propostas

### 1. `WatermarkUtil.kt`
- **Ajuste de Subsampling e Redimensionamento HD**:
  - Atualizar os limites do `loadAndCorrectBitmap` de 1920x1440 para **1280x960 px**.
  - Após a correção da rotação EXIF, se as dimensões ainda excederem 1280x960, realizar `Bitmap.createScaledBitmap` preservando a proporção de aspecto.
- **Calibração Tipográfica da Marca d'Água para HD**:
  - Ajustar o cálculo do tamanho da fonte e altura dos banners com base na altura da imagem HD (altura da faixa superior de ~50px e tamanho de texto de dados inferior proporcional), garantindo leitura sem serrilhado.
- **Gravação Otimizada**:
  - Gravar os arquivos JPEG com compressão de qualidade 88%, reduzindo o consumo de espaço no armazenamento interno.

### 2. `PdfReportExporter.kt`
- Alinhar a decodificação com a resolução HD nativa das fotos salvas, acelerando a renderização das páginas e reduzindo o tamanho final do arquivo `.pdf`.

---

## 2. Plano de Verificação

1. **Compilação**:
   - Executar `compile_applet` para certificar que todo o código compila sem erros.
2. **Validação de Tamanho e Dimensões**:
   - Verificar se as fotos geradas possuem largura máxima de 1280 px e tamanho médio inferior a 500 KB.
3. **Validação Visual da Marca d'Água**:
   - Confirmar que as faixas, textos de geolocalização e cabeçalhos permanecem nítidos e proporcionais na resolução HD.
