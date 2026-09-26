# Novo Layout do Relatório Técnico em PDF

Reestruturação do relatório PDF para exibir as fotos de cada registro em uma única linha horizontal (mesmo com 3 fotos: Antes, Durante e Depois), sem textos ou tarjas sobrepostos nas fotos, organizando múltiplos registros em linhas consecutivas (um abaixo do outro) com paginação inteligente.

## Decisões Confirmadas

1. **Cabeçalho Técnico Principal Mantido:**
   - Topo da página com cabeçalho corporativo ("REGISTRO DE CAMPO - RELATÓRIO TÉCNICO", data de emissão e contagem total de registros).
   - Rodapé em todas as páginas com numeração "Página X de Y".

2. **Remoção de Informações Sobrepostas nas Fotos:**
   - Fotos exibidas com moldura limpa e cantos arredondados, sem tarjas coloridas no topo e sem etiquetas de texto/KM no rodapé da imagem (a marca d'água técnica gerada nas fotos já contém todas as informações com alta legibilidade).

3. **Fotos de Cada Registro em Uma Única Linha:**
   - **Com 2 fotos:** 2 fotos posicionadas lado a lado em 1 linha (largura de 50% cada).
   - **Com 3 fotos:** 3 fotos posicionadas lado a lado em 1 única linha (largura de 33.3% cada, Antes, Durante e Depois).

4. **Múltiplos Registros em Linhas Consecutivas:**
   - Cada registro possui seu resumo técnico compacto com Rodovia, KM, Faixa, Encarregado, GPS, Data e Observações, seguido imediatamente pela linha de fotos.
   - Caso haja mais registros selecionados, eles são posicionados nas linhas abaixo na mesma página, criando novas páginas A4 automaticamente conforme o preenchimento da folha.

---

## Estrutura Visual do Relatório

```
┌────────────────────────────────────────────────────────────────────────┐
│  REGISTRO DE CAMPO - RELATÓRIO TÉCNICO                                 │
│  Emissão: 26/09/2026 10:30  •  Total de Registros: 4                   │
├────────────────────────────────────────────────────────────────────────┤
│ ┌─ REGISTRO #1: IMPLANTAÇÃO TACHA ───────────────────────────────────┐ │
│ │ Rodovia: BR-101 | KM 120.5 ao 121.2 | Sentido: Norte | Pista: Princ│ │
│ │ Encarregado: João Silva | Faixa: LBO-D | GPS: Lat -23.55, Long -46 │ │
│ ├────────────────────────────────────────────────────────────────────┤ │
│ │  ┌───────────────┐     ┌───────────────┐     ┌───────────────┐     │ │
│ │  │               │     │               │     │               │     │ │
│ │  │  Foto Antes   │     │ Foto Durante  │     │  Foto Depois  │     │ │
│ │  │               │     │               │     │               │     │ │
│ │  └───────────────┘     └───────────────┘     └───────────────┘     │ │
│ └────────────────────────────────────────────────────────────────────┘ │
│                                                                        │
│ ┌─ REGISTRO #2: PINTURA MANUAL ──────────────────────────────────────┐ │
│ │ Rodovia: SP-330 | KM 45.0 ao 45.8 | Sentido: Sul | Pista: Expressa │ │
│ │ Encarregado: Carlos | Faixa: LEGENDA (PARE) | GPS: Lat ..., Long...│ │
│ ├────────────────────────────────────────────────────────────────────┤ │
│ │  ┌───────────────────────────┐     ┌───────────────────────────┐   │ │
│ │  │                           │     │                           │   │ │
│ │  │        Foto Antes         │     │        Foto Depois        │   │ │
│ │  │                           │     │                           │   │ │
│ │  └───────────────────────────┘     └───────────────────────────┘   │ │
│ └────────────────────────────────────────────────────────────────────┘ │
│                                                                        │
│ ── Assinatura Encarregado ──────────── Assinatura Fiscalização ─────── │
│ Aplicativo Registro de Campo                             Página 1 de 2 │
└────────────────────────────────────────────────────────────────────────┘
```

---

## Arquivos Modificados
- `app/src/main/java/com/example/util/PdfReportExporter.kt`
