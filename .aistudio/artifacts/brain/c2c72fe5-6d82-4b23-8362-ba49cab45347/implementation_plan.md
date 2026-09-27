# Aumento de 15% na Tipografia e Backgrounds da Marca d'Água

Ajuste no algoritmo de renderização de marca d'água fotográfica (`WatermarkUtil.kt`) para aumentar em 15% o tamanho das fontes e dimensionar proporcionalmente as faixas de fundo (superior e inferior), garantindo maior legibilidade e nitidez nas fotos registradas em campo.

## Modificações Propostas

### 1. Faixa Superior (Etapa da Atividade - ANTES / DURANTE / DEPOIS)
- **Altura da Faixa (`topBarHeight`):** Aumentada em 15% (de `5.7%` para `~6.6%` da altura da foto, com limites de `48px` a `86px`).
- **Tamanho da Fonte (`topTextPaint.textSize`):** Aumentado proporcionalmente em 15% (de `topBarHeight * 0.57` para `topBarHeight * 0.60`, mínimo `23px` em negrito).

### 2. Painel Inferior de Dados Técnicos (Rodapé)
- **Tamanho da Fonte Base (`textSize`):** Aumentado em 15% (de `2.53%` para `2.91%` da altura da imagem, faixa de `21px` a `44px`).
- **Espaçamento entre Linhas (`lineSpacing`):** Ajustado para `textSize * 1.36f` para manter espaçamento confortável e legível.
- **Preenchimento e Margens (`verticalPadding` e `paddingLeft`):** Aumentados para acompanhar a escala da tipografia.
- **Altura do Background Inferior (`bannerHeight`):** Recalculada dinamicamente para acomodar com folga e segurança o novo tamanho de texto.

---

## Arquivo Afetado
- `app/src/main/java/com/example/util/WatermarkUtil.kt`
