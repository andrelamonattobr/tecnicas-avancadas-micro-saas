# Spec Revisada 005 – Efetuação e Confirmação de Pedidos de Compra

- **Ordem de Complexidade:** 5 de 8
- **Dependências:** `spec-revisada-001` (Cadastro e Autenticação de Comerciante), `spec-revisada-002` (Gestão de Fornecedores), `spec-revisada-003` (Gestão de Listas de Pedidos e Produtos) e `spec-revisada-004` (Envio de Lista de Pedidos via WhatsApp)

---

## Objetivo
Permitir que o comerciante autenticado efetue novos pedidos de compra a partir de suas listas de pedidos já criadas, gerando inicialmente um registro independente de pedido de compra em modo **Rascunho** (`RASCUNHO`) — que permite editar livremente os produtos e quantidades antes do fechamento — e, em seguida, confirmar o pedido de compra (`CONFIRMADO`) salvando os valores negociados com o fornecedor, ou marcá-lo como cancelado (`CANCELADO`).

---

## Escopo
**Inclui:**
- Iniciar a efetuação de um novo pedido de compra a partir de uma lista de pedidos existente (`spec-revisada-003`), gerando um registro de pedido de compra em status `RASCUNHO` com a cópia dos produtos da lista, **sem manter vínculo com o registro da lista de pedidos**.
- Permitir a edição livre dos produtos dentro do pedido de compra enquanto ele estiver em modo `RASCUNHO` (alterar quantidades, remover itens que não serão comprados na ocasião, adicionar novos itens ao rascunho e informar o `valorUnitario` de cada item).
- Confirmar o pedido de compra (transicionando de `RASCUNHO` para `CONFIRMADO`), calculando e salvando automaticamente os subtotais por item e o `valorTotal` do pedido.
- Permitir descartar um pedido ainda em `RASCUNHO` ou marcar como `CANCELADO` um pedido que já havia sido `CONFIRMADO` (enviado ao fornecedor).
- Listar e consultar os pedidos de compra do comerciante (filtrando por fornecedor e por status).

**Não inclui:**
- Qualquer vínculo estrutural (chave estrangeira/referência persistida) entre o registro de Pedido de Compra e a Lista de Pedidos que serviu de base, nem bloqueio da Lista de Pedidos original.
- Registro de recebimento físico total ou parcial das mercadorias (escopo da `spec-revisada-006`).
- Avaliação de nota do fornecedor pelo pedido (escopo da `spec-revisada-007`).
- Consolidação estatística no painel analítico (escopo da `spec-revisada-008`).

---

## Atores
- **Cliente (Comerciante):** Autenticado, inicia a efetuação de um pedido de compra a partir de uma lista de pedidos, edita os produtos e informa os valores no modo `RASCUNHO`, confirma o envio/fechamento do pedido (`CONFIRMADO`) e pode marcar um pedido confirmado como `CANCELADO`.
- **Sistema:** Copia o fornecedor e os produtos da lista selecionada para um novo registro independente de Pedido de Compra em `RASCUNHO` (sem vincular o pedido à lista), permite a edição dos itens enquanto em `RASCUNHO`, valida quantidades e valores unitários positivos no ato da confirmação, calcula subtotais e `valorTotal`, transiciona o status para `CONFIRMADO` e gerencia o cancelamento (`CANCELADO`).

---

## Dados

### Pedido de Compra (Efetuação de Pedido de Compra)
```text
comerciante:       referência a Comerciante (spec-revisada-001), obrigatório, preenchido com o comerciante autenticado
codigo:            texto, obrigatório, único por comerciante, gerado automaticamente (ex.: "PC-0001")
fornecedor:        referência a Fornecedor (spec-revisada-002), obrigatório, copiado da lista usada como ponto de partida
statusPedido:      texto (RASCUNHO | CONFIRMADO | CANCELADO), obrigatório, padrão inicial = RASCUNHO (extensível pela spec-revisada-006)
valorTotal:        numérico decimal monetário (2 casas decimais), obrigatório na confirmação (padrão = 0,00 enquanto em RASCUNHO), calculado pelo sistema
dataCriacao:       data/hora, obrigatório, gerado automaticamente pelo sistema ao iniciar o rascunho do pedido
dataConfirmacao:   data/hora, opcional (gerado obrigatoriamente pelo sistema no momento em que o pedido passa de RASCUNHO para CONFIRMADO)
previsaoEntrega:   data, opcional, maior ou igual à data de confirmação
observacaoCompra:  texto, opcional, até 250 caracteres
```

### Item do Pedido de Compra
```text
pedidoCompra:         referência ao Pedido de Compra, obrigatório
nomeProduto:          texto, obrigatório, único no mesmo pedido quando combinado com unidadeMedida, 2 a 100 caracteres (copiado da lista e editável em RASCUNHO)
unidadeMedida:        texto (UN | CX | FD | KG | LT | PCT), obrigatório (copiado da lista e editável em RASCUNHO)
quantidade:           numérico decimal positivo, obrigatório, maior que 0 (copiado da lista e editável enquanto o pedido estiver em RASCUNHO)
valorUnitario:        numérico decimal monetário (2 casas decimais), opcional enquanto em RASCUNHO, obrigatório e maior que 0,00 para confirmar o pedido
subtotal:             numérico decimal monetário (2 casas decimais), obrigatório, calculado pelo sistema (quantidade * valorUnitario)
```

---

## Regras de Negócio
- **RN-01:** Quando o comerciante decidir efetuar um novo pedido de compra a partir de uma lista de pedidos existente (`spec-revisada-003`), o sistema deve criar um novo registro de Pedido de Compra com `statusPedido = RASCUNHO`, copiando o `fornecedor` (que deve estar ativo conforme `spec-revisada-002`) e todos os produtos anotados naquela lista para os Itens do Pedido de Compra.
- **RN-02:** O registro de Pedido de Compra **não deve ser vinculado** ao registro da Lista de Pedidos: após a cópia inicial dos produtos para o rascunho do pedido, a Lista de Pedidos e o Pedido de Compra seguem vidas totalmente independentes, e a mesma Lista de Pedidos pode continuar sendo editada como bloco de notas e usada para gerar quantos pedidos de compra o comerciante quiser.
- **RN-03:** Enquanto o Pedido de Compra estiver com `statusPedido = RASCUNHO`, o comerciante pode editar livremente os produtos do pedido (adicionar novos produtos, remover produtos copiados, alterar quantidades ou unidades de medida e preencher os preços unitários `valorUnitario`).
- **RN-04:** Para confirmar o pedido de compra (transição de `RASCUNHO` para `CONFIRMADO`), o pedido deve conter **pelo menos 1 (um) produto**, e todos os itens do pedido devem possuir `quantidade > 0` e `valorUnitario > 0,00`.
- **RN-05:** No ato da confirmação do pedido, o sistema calcula e salva automaticamente o `subtotal` de cada item (`quantidade * valorUnitario`, arredondado em 2 casas decimais) e o `valorTotal` do pedido (soma exata dos subtotais de todos os itens), preenche `dataConfirmacao` e altera `statusPedido` para `CONFIRMADO`.
- **RN-06:** Após o pedido de compra atingir o status `CONFIRMADO`, seus produtos, quantidades e valores financeiros tornam-se imutáveis.
- **RN-07:** Um pedido de compra com `statusPedido = CONFIRMADO` (já enviado/fechado com o fornecedor, mas sem recebimento registrado) pode ser marcado pelo comerciante como `CANCELADO` caso a compra seja cancelada.

---

## Critérios de Aceite

**CA-01: Geração de pedido de compra em modo RASCUNHO a partir de uma lista de pedidos (sem vínculo com a lista)**
**Dado** que o comerciante possui uma lista de pedidos `"Bloco de Notas Bebidas"` para o fornecedor ativo `"Distribuidora Central"` contendo 3 produtos anotados
**Quando** o comerciante acionar a opção de efetuar um novo pedido de compra a partir dessa lista
**Então** o sistema gera um novo registro de Pedido de Compra com `statusPedido = "RASCUNHO"` vinculado apenas ao comerciante e ao fornecedor `"Distribuidora Central"` (sem vínculo com o registro da lista)
**E** copia os 3 produtos da lista para dentro do novo pedido de compra em rascunho
**E** a lista `"Bloco de Notas Bebidas"` permanece intacta e livre para gerar outros pedidos de compra no futuro.

**CA-02: Edição dos produtos no pedido de compra em RASCUNHO e posterior confirmação com valores**
**Dado** um Pedido de Compra em status `"RASCUNHO"` contendo `"Refrigerante 2L"` (`10 FD`), `"Água 500ml"` (`20 FD`) e `"Suco 1L"` (`5 CX`)
**Quando** o comerciante editar o rascunho do pedido removendo `"Suco 1L"`, alterando a quantidade de `"Água 500ml"` para `15 FD`, informando os valores unitários de `R$ 35,00` (Refrigerante) e `R$ 12,00` (Água) e confirmar o pedido de compra
**Então** o sistema calcula os subtotais de `R$ 350,00` e `R$ 180,00`
**E** atualiza o pedido para `statusPedido = "CONFIRMADO"`, salva `valorTotal = R$ 530,00`, registra `dataConfirmacao` e bloqueia novas edições nos itens do pedido.

**CA-03: Impedimento de confirmação de rascunho sem valores unitários ou sem produtos**
**Dado** um Pedido de Compra em status `"RASCUNHO"`
**Quando** o comerciante tentar confirmar o pedido deixando algum item sem `valorUnitario` (ou com valor `0,00`) ou tendo removido todos os produtos do rascunho
**Então** o sistema impede a confirmação e exibe `"Para confirmar o pedido de compra, mantenha ao menos um produto e informe um valor unitário maior que zero para todos os itens"`
**E** o pedido permanece em status `"RASCUNHO"`.

**CA-04: Marcação de pedido de compra confirmado como CANCELADO**
**Dado** um Pedido de Compra que já foi confirmado e encontra-se com `statusPedido = "CONFIRMADO"`
**Quando** o comerciante marcar esse pedido de compra como cancelado
**Então** o sistema atualiza o `statusPedido` para `"CANCELADO"`
**E** preserva o registro no histórico sem permitir edições ou recebimentos sobre ele.

---

## Restrições
- **Restrições de Negócio e Escopo:**
  - O Pedido de Compra **não deve possuir vínculo** (chave estrangeira/referência) com a Lista de Pedidos; a lista funciona apenas como um bloco de notas de onde os produtos são copiados para iniciar o rascunho da efetuação do pedido.
  - Não é permitido editar produtos ou preços de um pedido após ele sair do status `RASCUNHO` e passar para `CONFIRMADO`.
  - Esta spec depende apenas das `spec-revisada-001` a `spec-revisada-004`, e **não pode depender** de nenhuma spec posterior (`spec-revisada-006` a `spec-revisada-008`).
- **Limites de Stack:**
  - A solução deve compor uma aplicação web SPA em **Angular** (com renderização client-side das informações não confidenciais) comunicando-se com um back-end em **Spring Boot** operando como **API REST Stateless**.
  - A camada de persistência deve utilizar abstração **JPA / Hibernate**, executando inicialmente com banco de dados em memória (in-memory) e mantendo total compatibilidade estrutural para migração ao **PostgreSQL**.
- **Padrões de Repositório:**
  - O código-fonte do projeto deve residir em uma **macro pasta única**, organizada estritamente em duas pastas raiz: `/front` (contendo o projeto SPA Angular) e `/back` (contendo o projeto API REST Spring Boot).
