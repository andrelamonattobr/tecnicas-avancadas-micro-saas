# Spec Revisada 008 – Seção Analítica de Compras e Desempenho de Fornecedores

- **Ordem de Complexidade:** 8 de 8 (Maior complexidade)
- **Dependências:** `spec-revisada-001` (Cadastro e Autenticação de Comerciante), `spec-revisada-002` (Gestão de Fornecedores), `spec-revisada-003` (Gestão de Listas de Pedidos e Produtos), `spec-revisada-004` (Envio de Lista de Pedidos via WhatsApp), `spec-revisada-005` (Efetuação e Confirmação de Pedidos de Compra), `spec-revisada-006` (Confirmação de Recebimento Total e Parcial de Pedidos de Compra) e `spec-revisada-007` (Avaliação de Fornecedor por Pedido de Compra)

---

## Objetivo
Disponibilizar ao comerciante autenticado uma seção Analítica centralizada e isolada para a sua empresa, consolidando as métricas de fornecedores cadastrados, listas de pedidos criadas, pedidos de compra confirmados e recebidos — diferenciando com precisão os recebimentos parciais dos completos — e apresentando a avaliação individual por pedido e a nota média de cada fornecedor (`0,0` a `5,0`).

---

## Escopo
**Inclui:**
- Consolidar exclusivamente os dados pertencentes ao comerciante autenticado (`spec-revisada-001`).
- Exibir indicadores gerais de: fornecedores cadastrados (ativos e inativos), listas de pedidos criadas (ativas e enviadas por WhatsApp) e pedidos de compra (em rascunho, confirmados aguardando entrega, recebidos parciais, recebidos completos e cancelados).
- Diferenciar explicitamente os pedidos válidos em: **Aguardando Recebimento (`CONFIRMADO`)**, **Recebidos Parciais (`RECEBIDO_PARCIAL`)** e **Recebidos Completos (`RECEBIDO_TOTAL`)**, apresentando suas contagens e valores financeiros (`valorTotalConfirmado`, `valorTotalRecebido` e `valorPendenteEntrega`).
- Exibir o quadro analítico individual de cada fornecedor contendo: listas geradas, pedidos confirmados, entregas completas vs. parciais, nota média consolidada (`0,0` a `5,0`) e a listagem das avaliações individuais dadas em cada pedido de compra (`spec-revisada-007`).
- Filtrar os indicadores por período de datas (`dataInicio` e `dataFim`) e/ou por fornecedor específico.

**Não inclui:**
- Criação, edição ou exclusão direta de fornecedores, listas, pedidos, recebimentos ou avaliações dentro da seção Analítica.
- Visualização de métricas de outros comerciantes cadastrados no Micro-SaaS.
- Exportação contábil/fiscal ou algoritmos de previsão de estoque.

---

## Atores
- **Cliente (Comerciante):** Autenticado, acessa a seção Analítica, aplica filtros opcionais de período e fornecedor e acompanha os indicadores de compras, entregas parciais/completas e notas individuais de seus fornecedores.
- **Sistema:** Filtra os registros pelo `comerciante` autenticado, desconsidera pedidos cancelados dos somatórios financeiros ativos, calcula contagens, saldos financeiros e médias de avaliação, segrega entregas parciais e completas e diferencia fornecedores sem avaliação daqueles avaliados com nota `0,0`.

---

## Dados

### Filtros da Seção Analítica
```text
comerciante: referência a Comerciante (spec-revisada-001), obrigatório, obtido automaticamente da sessão autenticada
dataInicio:  data, opcional (filtra listas por dataCriacao e pedidos por dataConfirmacao a partir desta data)
dataFim:     data, opcional, maior ou igual a dataInicio
fornecedor:  referência a Fornecedor (spec-revisada-002), opcional, deve pertencer ao comerciante autenticado
```

### Indicadores Gerais Consolidados (Somente Leitura)
```text
totalFornecedoresCadastrados: numérico inteiro, obrigatório, fornecedores do comerciante (ativos + inativos)
totalFornecedoresAtivos:      numérico inteiro, obrigatório, fornecedores do comerciante com ativo = true
totalListasCriadas:           numérico inteiro, obrigatório, total de listas de pedidos (blocos de notas) criadas pelo comerciante
totalListasEnviadasWhatsapp:  numérico inteiro, obrigatório, listas do comerciante que já foram enviadas ao menos 1 vez por WhatsApp
pedidosEmRascunho:            numérico inteiro, obrigatório, pedidos de compra iniciados e ainda em statusPedido = RASCUNHO
totalPedidosConfirmados:      numérico inteiro, obrigatório, pedidos válidos efetivados (status CONFIRMADO, RECEBIDO_PARCIAL e RECEBIDO_TOTAL)
pedidosAguardandoRecebimento: numérico inteiro, obrigatório, pedidos com statusPedido = CONFIRMADO
pedidosRecebidosCompletos:    numérico inteiro, obrigatório, pedidos com statusPedido = RECEBIDO_TOTAL
pedidosRecebidosParciais:     numérico inteiro, obrigatório, pedidos com statusPedido = RECEBIDO_PARCIAL
pedidosCancelados:            numérico inteiro, obrigatório, exibido separadamente (statusPedido = CANCELADO)
valorTotalConfirmado:         numérico decimal monetário (2 casas decimais), obrigatório, soma de valorTotal dos pedidos válidos
valorTotalRecebido:           numérico decimal monetário (2 casas decimais), obrigatório, soma de valorRecebido dos pedidos válidos
valorPendenteEntrega:         numérico decimal monetário (2 casas decimais), obrigatório, calculado (valorTotalConfirmado - valorTotalRecebido)
```

### Indicadores e Avaliações Individuais por Fornecedor (Somente Leitura)
```text
fornecedor:                   identificação do Fornecedor (nome, categoria, status ativo)
quantidadeListas:             numérico inteiro, obrigatório, listas criadas para o fornecedor
quantidadePedidosValidos:     numérico inteiro, obrigatório, pedidos não cancelados do fornecedor
quantidadeRecebidosCompletos: numérico inteiro, obrigatório, pedidos do fornecedor com status RECEBIDO_TOTAL
quantidadeRecebidosParciais:  numérico inteiro, obrigatório, pedidos do fornecedor com status RECEBIDO_PARCIAL
mediaAvaliacao:               numérico decimal (1 casa decimal, 0,0 a 5,0), opcional (nulo quando não há pedidos avaliados)
avaliacoesPorPedido:          lista de objetos contendo (codigoPedido, dataConfirmacao, dataRecebimento, statusPedido, nota [0,0 a 5,0])
```

---

## Regras de Negócio
- **RN-01:** A seção Analítica deve agregar **exclusivamente** os registros pertencentes ao `comerciante` autenticado (`spec-revisada-001`), garantindo isolamento absoluto entre diferentes clientes do Micro-SaaS.
- **RN-02:** O indicador `totalPedidosConfirmados` e os somatórios financeiros (`valorTotalConfirmado`, `valorTotalRecebido` e `valorPendenteEntrega`) devem considerar apenas pedidos efetivamente confirmados e válidos (`CONFIRMADO`, `RECEBIDO_PARCIAL` e `RECEBIDO_TOTAL`), excluindo pedidos ainda em `RASCUNHO` ou marcados como `CANCELADO` (`spec-revisada-005`) para não inflar artificialmente os custos do comerciante.
- **RN-03:** O painel deve diferenciar de forma explícita e separada os pedidos válidos em: **Aguardando Recebimento** (`CONFIRMADO`), **Recebidos Parciais** (`RECEBIDO_PARCIAL`) e **Recebidos Completos** (`RECEBIDO_TOTAL`).
- **RN-04:** Na análise de cada fornecedor, o sistema deve apresentar tanto a `mediaAvaliacao` consolidada (`0,0` a `5,0`) quanto a lista individual de notas por pedido de compra (`avaliacoesPorPedido`), exibindo o código do pedido, se o recebimento daquele pedido foi parcial ou completo e a nota atribuída (`0,0` a `5,0`, sem comentário).
- **RN-05:** Fornecedores que ainda não possuem nenhum pedido avaliado devem ser exibidos com a indicação `"Sem avaliação"`, jamais exibindo `0,0` como padrão (pois `0,0` representa uma avaliação real de nota mínima atribuída pelo comerciante).
- **RN-06:** Quando o filtro de período (`dataInicio` e `dataFim`) for informado, `dataFim` deve ser maior ou igual a `dataInicio`; o filtro incide sobre a `dataCriacao` para métricas de listas e sobre a `dataConfirmacao` para métricas de pedidos, recebimentos e avaliações vinculadas.

---

## Critérios de Aceite

**CA-01: Consolidação diferenciando pedidos confirmados, parciais, completos e ignorando cancelados nos valores**
**Dado** que o comerciante autenticado possui 3 fornecedores, 5 listas criadas e 5 pedidos registrados:
- 1 pedido `"CONFIRMADO"` de `R$ 200,00` (`valorRecebido = R$ 0,00`)
- 1 pedido `"RECEBIDO_PARCIAL"` de `R$ 500,00` (`valorRecebido = R$ 300,00`)
- 2 pedidos `"RECEBIDO_TOTAL"` somando `R$ 800,00` (`valorRecebido = R$ 800,00`)
- 1 pedido `"CANCELADO"` de `R$ 400,00`
**Quando** o comerciante acessar a seção `"Analítica"`
**Então** o sistema exibe `totalPedidosConfirmados = 4`, `pedidosAguardandoRecebimento = 1`, `pedidosRecebidosParciais = 1`, `pedidosRecebidosCompletos = 2` e `pedidosCancelados = 1`
**E** apresenta `valorTotalConfirmado = R$ 1.500,00`, `valorTotalRecebido = R$ 1.100,00` e `valorPendenteEntrega = R$ 400,00`.

**CA-02: Exibição da avaliação individual por pedido e da média do fornecedor**
**Dado** que o fornecedor `"Distribuidora Central"` possui o pedido `"PC-0001"` (`RECEBIDO_TOTAL`) avaliado com nota `5,0` e o pedido `"PC-0002"` (`RECEBIDO_PARCIAL`) avaliado com nota `3,0`
**Quando** o comerciante consultar o quadro analítico do fornecedor `"Distribuidora Central"`
**Então** o sistema exibe `mediaAvaliacao = 4,0`, `quantidadeRecebidosCompletos = 1` e `quantidadeRecebidosParciais = 1`
**E** lista individualmente as avaliações por pedido: `"PC-0001 (RECEBIDO_TOTAL) - Nota: 5,0"` e `"PC-0002 (RECEBIDO_PARCIAL) - Nota: 3,0"`.

**CA-03: Isolamento de dados analíticos entre diferentes comerciantes**
**Dado** que o comerciante `"João"` possui 4 pedidos confirmados e outra comerciante `"Maria"` possui 10 pedidos confirmados no sistema
**Quando** `"João"` acessar a seção `"Analítica"` em sua conta autenticada
**Então** o sistema contabiliza e exibe apenas os 4 pedidos pertencentes a `"João"`.

**CA-04: Distinção entre fornecedor avaliado com nota 0,0 e fornecedor sem avaliações**
**Dado** que o `"Fornecedor Alfa"` possui 1 pedido avaliado com nota `0,0` e o `"Fornecedor Beta"` não possui nenhum pedido avaliado
**Quando** o comerciante visualizar o desempenho dos fornecedores na seção `"Analítica"`
**Então** o `"Fornecedor Alfa"` é apresentado com `mediaAvaliacao = 0,0`
**E** o `"Fornecedor Beta"` é apresentado com `"Sem avaliação"`.

---

## Restrições
- **Restrições de Negócio e Escopo:**
  - A seção Analítica é estritamente consultiva (somente leitura) e isolada por comerciante autenticado.
  - Esta spec situa-se no topo da cadeia de complexidade e depende das specs anteriores (`spec-revisada-001` a `spec-revisada-007`).
- **Limites de Stack:**
  - A solução deve compor uma aplicação web SPA em **Angular** (com renderização client-side das informações não confidenciais) comunicando-se com um back-end em **Spring Boot** operando como **API REST Stateless**.
  - A camada de persistência deve utilizar abstração **JPA / Hibernate**, executando inicialmente com banco de dados em memória (in-memory) e mantendo total compatibilidade estrutural para migração ao **PostgreSQL**.
- **Padrões de Repositório:**
  - O código-fonte do projeto deve residir em uma **macro pasta única**, organizada estritamente em duas pastas raiz: `/front` (contendo o projeto SPA Angular) e `/back` (contendo o projeto API REST Spring Boot).
