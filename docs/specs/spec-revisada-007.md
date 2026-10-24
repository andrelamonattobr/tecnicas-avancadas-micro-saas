# Spec Revisada 007 – Avaliação de Fornecedor por Pedido de Compra

- **Ordem de Complexidade:** 7 de 8
- **Dependências:** `spec-revisada-001` (Cadastro e Autenticação de Comerciante), `spec-revisada-002` (Gestão de Fornecedores), `spec-revisada-005` (Efetuação e Confirmação de Pedidos de Compra) e `spec-revisada-006` (Confirmação de Recebimento Total e Parcial de Pedidos de Compra)

---

## Objetivo
Permitir que o comerciante autenticado avalie individualmente o desempenho de cada fornecedor por pedido de compra recebido (parcial ou totalmente), atribuindo exclusivamente uma nota numérica de `0,0` a `5,0` (sem comentários), além de permitir o ajuste da nota atribuída e consolidar automaticamente a média histórica do fornecedor.

---

## Escopo
**Inclui:**
- Registrar uma avaliação numérica entre `0,0` e `5,0` (com precisão de 1 casa decimal e sem comentário) vinculada a um pedido de compra recebido (`RECEBIDO_PARCIAL` ou `RECEBIDO_TOTAL`) do comerciante autenticado.
- Permitir a atualização (retificação) da nota de uma avaliação já existente no pedido (por exemplo, após a complementação de uma entrega parcial), mantendo o limite de 1 avaliação por pedido.
- Recalcular automaticamente a `mediaAvaliacao` (arredondada para 1 casa decimal) e o `totalAvaliacoes` do fornecedor a cada registro ou atualização de nota.
- Consultar a nota individual atribuída em cada pedido de compra e a média consolidada do fornecedor.

**Não inclui:**
- Qualquer campo de comentário em texto, justificativa escrita, tags ou anexos.
- Avaliação de fornecedor desvinculada de um pedido de compra recebido.
- Consolidação de gráficos e cruzamento global de métricas na seção Analítica (escopo da `spec-revisada-008`).

---

## Atores
- **Cliente (Comerciante):** Autenticado, atribui ou atualiza a nota numérica entre `0,0` e `5,0` referente ao desempenho do fornecedor em um pedido de compra recebido.
- **Sistema:** Verifica a propriedade do pedido e se seu status é `RECEBIDO_PARCIAL` ou `RECEBIDO_TOTAL`, valida o intervalo (`0,0` a `5,0`), a precisão máxima de 1 casa decimal e a ausência de comentários, garante 1 única avaliação por pedido e recalcula imediatamente a média consolidada do fornecedor.

---

## Dados

### Avaliação de Pedido de Compra
```text
comerciante:     referência a Comerciante (spec-revisada-001), obrigatório, preenchido com o comerciante autenticado
pedidoCompra:    referência ao Pedido de Compra (spec-revisada-005 / spec-revisada-006), obrigatório, único (1 avaliação por pedido)
fornecedor:      referência ao Fornecedor (spec-revisada-002), obrigatório, obtido automaticamente do pedido de compra
nota:            numérico decimal (máximo 1 casa decimal, passo 0,1), obrigatório, intervalo fechado de 0,0 até 5,0
dataAvaliacao:   data/hora, obrigatório, gerado/atualizado automaticamente pelo sistema no momento do registro
```

### Consolidação de Avaliação no Fornecedor
```text
mediaAvaliacao:  numérico decimal (1 casa decimal), opcional (nulo enquanto totalAvaliacoes = 0), intervalo de 0,0 até 5,0, calculado pelo sistema
totalAvaliacoes: numérico inteiro, obrigatório, maior ou igual a 0, padrão = 0, calculado pelo sistema
```

---

## Regras de Negócio
- **RN-01:** Uma avaliação só pode ser registrada ou alterada para um pedido de compra pertencente ao comerciante autenticado (`spec-revisada-001`) cujo `statusPedido` seja `RECEBIDO_PARCIAL` ou `RECEBIDO_TOTAL` (`spec-revisada-006`); pedidos em `RASCUNHO`, `CONFIRMADO` ou `CANCELADO` não podem ser avaliados.
- **RN-02:** Cada pedido de compra possui no máximo 1 (uma) avaliação vinculada; não é permitido criar múltiplos registros de avaliação para o mesmo pedido.
- **RN-03:** O valor da `nota` deve situar-se estritamente entre `0,0` e `5,0`, com no máximo 1 casa decimal (ex.: `0,0`, `2,5`, `4,8`, `5,0`); valores fora do intervalo ou com mais de 1 casa decimal (ex.: `4,55`) devem ser rejeitados.
- **RN-04:** A avaliação deve conter exclusivamente a `nota` numérica, sendo vedada a presença de comentários ou observações textuais.
- **RN-05:** Caso o comerciante atualize a nota de um pedido já avaliado (por exemplo, após o fornecedor entregar o saldo restante de um pedido parcial), o sistema atualiza a `nota` e a `dataAvaliacao` do registro existente, mantendo `totalAvaliacoes` inalterado e recalculando a `mediaAvaliacao` do fornecedor.
- **RN-06:** O cálculo da `mediaAvaliacao` do fornecedor deve corresponder à média aritmética simples das notas de todos os pedidos avaliados daquele fornecedor, arredondada para 1 casa decimal (critério meio para cima).

---

## Critérios de Aceite

**CA-01: Registro de avaliação válida em pedido recebido e cálculo da média do fornecedor**
**Dado** que o fornecedor `"Distribuidora Central"` já possui 1 pedido avaliado com nota `4,0` e tem um segundo pedido com status `"RECEBIDO_TOTAL"` ainda não avaliado
**Quando** o comerciante avaliar esse segundo pedido atribuindo a nota `4,5`
**Então** o sistema salva a avaliação com `nota = 4,5` sem nenhum campo de comentário
**E** recalcula automaticamente para o fornecedor `mediaAvaliacao = 4,3` (média `4,25` arredondada para 1 casa decimal) e `totalAvaliacoes = 2`.

**CA-02: Rejeição de nota fora do intervalo de 0,0 a 5,0 ou com mais de uma casa decimal**
**Dado** um pedido de compra com status `"RECEBIDO_PARCIAL"`
**Quando** o comerciante tentar atribuir uma nota `-1,0`, `5,1` ou `4,25` (duas casas decimais)
**Então** o sistema rejeita a operação e exibe `"A nota deve estar entre 0,0 e 5,0 com no máximo uma casa decimal"`
**E** nenhuma avaliação é gravada.

**CA-03: Bloqueio de avaliação para pedido ainda não recebido ou cancelado**
**Dado** um pedido de compra com status `"CONFIRMADO"` ou `"CANCELADO"`
**Quando** o comerciante tentar registrar uma nota de avaliação para esse pedido
**Então** o sistema bloqueia a operação e exibe `"Apenas pedidos com recebimento parcial ou total confirmado podem ser avaliados"`.

**CA-04: Atualização de nota de avaliação após complementação de entrega sem duplicar contagem**
**Dado** um pedido que foi avaliado com nota `2,5` quando estava em `"RECEBIDO_PARCIAL"` (sendo a única avaliação do fornecedor, `totalAvaliacoes = 1` e `mediaAvaliacao = 2,5`)
**Quando** o comerciante atualizar a nota desse mesmo pedido para `4,8`
**Então** o sistema atualiza a nota do pedido para `4,8`
**E** mantém `totalAvaliacoes = 1` e atualiza a `mediaAvaliacao` do fornecedor para `4,8`.

---

## Restrições
- **Restrições de Negócio e Escopo:**
  - É estritamente proibida a inclusão de comentários em texto na avaliação do fornecedor.
  - Esta spec depende exclusivamente das `spec-revisada-001`, `spec-revisada-002`, `spec-revisada-005` e `spec-revisada-006`, e **não pode depender** da `spec-revisada-008`.
- **Limites de Stack:**
  - A solução deve compor uma aplicação web SPA em **Angular** (com renderização client-side das informações não confidenciais) comunicando-se com um back-end em **Spring Boot** operando como **API REST Stateless**.
  - A camada de persistência deve utilizar abstração **JPA / Hibernate**, executando inicialmente com banco de dados em memória (in-memory) e mantendo total compatibilidade estrutural para migração ao **PostgreSQL**.
- **Padrões de Repositório:**
  - O código-fonte do projeto deve residir em uma **macro pasta única**, organizada estritamente em duas pastas raiz: `/front` (contendo o projeto SPA Angular) e `/back` (contendo o projeto API REST Spring Boot).
