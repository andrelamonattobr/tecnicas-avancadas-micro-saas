# Spec Revisada 006 – Confirmação de Recebimento Total e Parcial de Pedidos de Compra

- **Ordem de Complexidade:** 6 de 8
- **Dependências:** `spec-revisada-001` (Cadastro e Autenticação de Comerciante), `spec-revisada-002` (Gestão de Fornecedores), `spec-revisada-003` (Gestão de Listas de Pedidos e Produtos) e `spec-revisada-005` (Efetuação e Confirmação de Pedidos de Compra)

---

## Objetivo
Permitir que o comerciante autenticado registre e controle o recebimento físico das mercadorias de seus pedidos de compra confirmados, diferenciando entregas completas (**Recebimento Total**) de entregas incompletas (**Recebimento Parcial**), calculando com exatidão as quantidades acumuladas recebidas, os saldos pendentes por item e os valores financeiros efetivamente entregues.

---

## Escopo
**Inclui:**
- Confirmar o recebimento total de um pedido de compra em uma única ação.
- Registrar o recebimento parcial de um pedido de compra informando as quantidades entregues de cada produto.
- Somar entregas complementares sobre um pedido com recebimento parcial até que atinja a totalidade ou permaneça registrado como entrega parcial.
- Calcular automaticamente a `quantidadeRecebida` acumulada, a `quantidadePendente` por item e o `valorRecebido` do pedido.
- Bloquear o cancelamento de pedidos que já possuam recebimento (parcial ou total) registrado.

**Não inclui:**
- Atribuição de nota de avaliação ao fornecedor pelo pedido (escopo da `spec-revisada-007`).
- Consolidação estatística no painel analítico geral (escopo da `spec-revisada-008`).
- Emissão de notas de devolução fiscal ou estornos bancários.

---

## Atores
- **Cliente (Comerciante):** Autenticado, confere a entrega feita pelo fornecedor e registra no sistema a confirmação de recebimento total ou as quantidades entregues por item no recebimento parcial.
- **Sistema:** Verifica se o pedido pertence ao comerciante autenticado e está apto para recebimento (`CONFIRMADO` ou `RECEBIDO_PARCIAL`), valida os limites das quantidades entregues frente à `quantidade` confirmada do item (`spec-revisada-005`), acumula as quantidades recebidas, calcula saldos pendentes e `valorRecebido`, e define o status como `RECEBIDO_PARCIAL` ou `RECEBIDO_TOTAL`.

---

## Dados

### Recebimento do Pedido de Compra
```text
pedidoCompra:          referência ao Pedido de Compra (spec-revisada-005), obrigatório, pertencente ao comerciante autenticado
statusPedido:          texto (RASCUNHO | CONFIRMADO | RECEBIDO_PARCIAL | RECEBIDO_TOTAL | CANCELADO), obrigatório
dataRecebimento:       data/hora, opcional (obrigatório a partir do primeiro recebimento), atualizado pelo sistema na entrega
valorRecebido:         numérico decimal monetário (2 casas decimais), obrigatório, padrão = 0,00, calculado pelo sistema
observacaoRecebimento: texto, opcional, até 250 caracteres
```

### Conferência de Item do Pedido de Compra
```text
itemPedido:           referência ao Item do Pedido de Compra (spec-revisada-005), obrigatório
quantidade:           numérico decimal positivo, obrigatório (quantidade fechada na confirmação do pedido na spec-revisada-005, imutável)
quantidadeRecebida:   numérico decimal, obrigatório, acumulado entre 0 e quantidade, padrão = 0
quantidadePendente:   numérico decimal, obrigatório, calculado pelo sistema (quantidade - quantidadeRecebida)
```

---

## Regras de Negócio
- **RN-01:** Apenas pedidos de compra pertencentes ao comerciante autenticado (`spec-revisada-001`) e com `statusPedido` igual a `CONFIRMADO` ou `RECEBIDO_PARCIAL` (`spec-revisada-005`) podem receber registro de entrega; pedidos ainda em `RASCUNHO`, `CANCELADO` ou já em `RECEBIDO_TOTAL` têm registros de recebimento bloqueados.
- **RN-02:** Em cada registro de recebimento, a soma das quantidades entregues no ato deve ser estritamente maior que zero (`> 0`), e para cada item a `quantidadeRecebida` acumulada nunca pode ser negativa nem ultrapassar a `quantidade` confirmada do item (`0 <= quantidadeRecebida <= quantidade`).
- **RN-03:** Quando o comerciante acionar a confirmação direta de **Recebimento Total** (ou quando a entrega registrada fizer com que todos os itens atinjam `quantidadeRecebida == quantidade`), o sistema deve definir `quantidadeRecebida = quantidade` e `quantidadePendente = 0` em todos os itens, atualizar `statusPedido = RECEBIDO_TOTAL`, definir `valorRecebido = valorTotal` e gravar `dataRecebimento`.
- **RN-04:** Quando ao menos um item tiver `quantidadeRecebida > 0` e ao menos um item ainda possuir `quantidadeRecebida < quantidade`, o sistema deve definir `statusPedido = RECEBIDO_PARCIAL`, calcular `quantidadePendente = quantidade - quantidadeRecebida` por item, calcular `valorRecebido` como a soma de `(quantidadeRecebida * valorUnitario)` de todos os itens e gravar `dataRecebimento`.
- **RN-05:** Em um pedido com status `RECEBIDO_PARCIAL`, caso o fornecedor realize uma entrega complementar posteriormente, a nova quantidade entregue de cada item é somada à `quantidadeRecebida` já existente, recalculando `quantidadePendente`, `valorRecebido` e transicionando automaticamente para `RECEBIDO_TOTAL` se 100% dos itens forem completados.
- **RN-06:** A partir do momento em que um pedido atinge `RECEBIDO_PARCIAL` ou `RECEBIDO_TOTAL`, ele não pode mais ser marcado como `CANCELADO` (`spec-revisada-005`).

---

## Critérios de Aceite

**CA-01: Confirmação direta de recebimento total do pedido de compra**
**Dado** um pedido de compra do comerciante autenticado com status `"CONFIRMADO"` e `valorTotal = R$ 600,00`
**Quando** o comerciante acionar a confirmação de recebimento total do pedido
**Então** o sistema define `quantidadeRecebida = quantidade` e `quantidadePendente = 0` em todos os itens
**E** atualiza `statusPedido` para `"RECEBIDO_TOTAL"`, define `valorRecebido = R$ 600,00` e registra `dataRecebimento`.

**CA-02: Registro de recebimento parcial com cálculo proporcional de valor e saldo pendente**
**Dado** um pedido com status `"CONFIRMADO"` contendo `"Refrigerante 2L"` (`10 FD` a `R$ 35,00/FD`) e `"Água Mineral 500ml"` (`20 FD` a `R$ 12,50/FD`)
**Quando** o comerciante registrar o recebimento parcial informando a entrega de `6 FD` de `"Refrigerante 2L"` e `20 FD` de `"Água Mineral 500ml"`
**Então** o sistema atualiza o status do pedido para `"RECEBIDO_PARCIAL"`
**E** registra para `"Refrigerante 2L"` `quantidadeRecebida = 6` e `quantidadePendente = 4`
**E** calcula automaticamente `valorRecebido = R$ 460,00`.

**CA-03: Bloqueio de quantidade recebida acumulada superior à quantidade confirmada**
**Dado** um pedido em status `"RECEBIDO_PARCIAL"` onde o item `"Refrigerante 2L"` possui `quantidade = 10`, `quantidadeRecebida = 6` e `quantidadePendente = 4`
**Quando** o comerciante tentar registrar uma entrega complementar de `5 FD` para esse item (o que totalizaria `11 FD`)
**Então** o sistema rejeita a operação e exibe `"A quantidade recebida acumulada (11) não pode ultrapassar a quantidade confirmada no pedido (10)"`
**E** mantém os saldos anteriores inalterados.

**CA-04: Complementação de pedido parcial atingindo recebimento total**
**Dado** o pedido em status `"RECEBIDO_PARCIAL"` onde restam apenas `4 FD` pendentes de `"Refrigerante 2L"`
**Quando** o comerciante registrar a entrega complementar dos `4 FD` restantes
**Então** o sistema acumula `quantidadeRecebida = 10`, define `quantidadePendente = 0`
**E** atualiza automaticamente o `statusPedido` de `"RECEBIDO_PARCIAL"` para `"RECEBIDO_TOTAL"` e `valorRecebido = R$ 600,00`.

---

## Restrições
- **Restrições de Negócio e Escopo:**
  - Não é permitido alterar preços unitários durante a conferência de recebimento nem registrar recebimento em pedidos cancelados.
  - Esta spec depende exclusivamente das `spec-revisada-001`, `spec-revisada-002`, `spec-revisada-003` e `spec-revisada-005`, e **não pode depender** das `spec-revisada-007` e `spec-revisada-008`.
- **Limites de Stack:**
  - A solução deve compor uma aplicação web SPA em **Angular** (com renderização client-side das informações não confidenciais) comunicando-se com um back-end em **Spring Boot** operando como **API REST Stateless**.
  - A camada de persistência deve utilizar abstração **JPA / Hibernate**, executando inicialmente com banco de dados em memória (in-memory) e mantendo total compatibilidade estrutural para migração ao **PostgreSQL**.
- **Padrões de Repositório:**
  - O código-fonte do projeto deve residir em uma **macro pasta única**, organizada estritamente em duas pastas raiz: `/front` (contendo o projeto SPA Angular) e `/back` (contendo o projeto API REST Spring Boot).
