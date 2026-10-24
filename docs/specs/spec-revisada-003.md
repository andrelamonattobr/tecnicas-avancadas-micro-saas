# Spec Revisada 003 – Gestão de Listas de Pedidos e Produtos

- **Ordem de Complexidade:** 3 de 8
- **Dependências:** `spec-revisada-001` (Cadastro e Autenticação de Comerciante) e `spec-revisada-002` (Gestão de Fornecedores)

---

## Objetivo
Permitir que o comerciante autenticado crie quantas listas de pedidos desejar para cada um de seus fornecedores ativos e cadastre, edite ou remova quantos produtos quiser dentro de cada lista, funcionando como um "bloco de notas" permanente e reutilizável de produtos que ele pretende ou costuma comprar de cada fornecedor.

---

## Escopo
**Inclui:**
- Criar quantas listas de pedidos o comerciante desejar vinculadas a qualquer fornecedor ativo de sua conta (`spec-revisada-001`, `spec-revisada-002`).
- Listar e consultar as listas de pedidos do comerciante (filtrando por fornecedor ou buscando por título).
- Editar livremente o título e a observação de uma lista de pedidos ou remover/inativar listas que não utiliza mais.
- Cadastrar (anotar), editar e remover quantos produtos (itens) desejar dentro de cada lista de pedidos a qualquer momento, de forma contínua (como um bloco de notas do fornecedor).

**Não inclui:**
- Envio direto da lista por WhatsApp ao fornecedor (escopo da `spec-revisada-004`).
- Efetuação do pedido de compra em modo rascunho ou confirmação financeira (escopo da `spec-revisada-005`).
- Vínculo direto ou bloqueio da lista quando um pedido de compra é efetuado (a lista é independente do registro de pedido de compra).
- Registro de recebimento físico (escopo da `spec-revisada-006`) ou avaliação de fornecedor (escopo da `spec-revisada-007`).

---

## Atores
- **Cliente (Comerciante):** Autenticado, cria quantas listas de pedidos quiser para cada fornecedor e anota, edita ou remove livremente os produtos e quantidades dentro de cada lista.
- **Sistema:** Valida se o fornecedor selecionado pertence ao comerciante autenticado e está ativo, garante a unicidade dos produtos dentro da mesma lista e mantém a lista sempre editável como um bloco de notas permanente.

---

## Dados

### Lista de Pedidos (Bloco de Notas por Fornecedor)
```text
comerciante:     referência a Comerciante (spec-revisada-001), obrigatório, preenchido com o comerciante autenticado
fornecedor:      referência a Fornecedor (spec-revisada-002), obrigatório, não único (um fornecedor pode ter quantas listas o comerciante quiser)
titulo:          texto, obrigatório, não único, 3 a 80 caracteres (ex.: "Bebidas de Giro Rápido", "Itens de Mercearia")
observacao:      texto, opcional, não único, até 250 caracteres
ativa:           booleano, obrigatório, padrão = true
dataCriacao:     data/hora, obrigatório, gerado automaticamente pelo sistema na criação
dataAtualizacao: data/hora, obrigatório, atualizado automaticamente pelo sistema a cada alteração na lista ou em seus produtos
```

### Produto da Lista (Item Anotado na Lista)
```text
listaPedido:     referência à Lista de Pedidos, obrigatório
nomeProduto:     texto, obrigatório, único na mesma lista quando combinado com unidadeMedida, 2 a 100 caracteres
quantidade:      numérico decimal positivo, obrigatório, maior que 0 (até 2 casas decimais)
unidadeMedida:   texto (UN | CX | FD | KG | LT | PCT), obrigatório, 2 a 10 caracteres
observacaoItem:  texto, opcional, não único, até 120 caracteres
```

---

## Regras de Negócio
- **RN-01:** O comerciante pode criar quantas listas de pedidos quiser para um mesmo fornecedor, desde que o fornecedor pertença ao `comerciante` autenticado (`spec-revisada-001`) e esteja com `ativo = true` (`spec-revisada-002`).
- **RN-02:** Em cada lista de pedidos, o comerciante pode cadastrar quantos produtos desejar, sem limite máximo de itens por lista.
- **RN-03:** A Lista de Pedidos atua como um bloco de notas permanente de produtos por fornecedor: seus produtos podem ser adicionados, alterados ou removidos livremente a qualquer momento enquanto a lista estiver com `ativa = true`, não sendo bloqueada nem consumida por operações posteriores.
- **RN-04:** A `quantidade` de qualquer produto anotado na lista deve ser estritamente maior que zero (`> 0`); para a unidade `UN` (unidade inteira), o valor deve ser um número inteiro positivo.
- **RN-05:** Não é permitido cadastrar dois produtos com o mesmo `nomeProduto` (ignorando maiúsculas/minúsculas e espaços extras nas extremidades) e a mesma `unidadeMedida` dentro da mesma lista; caso o comerciante deseje alterar a quantidade anotada, deve editar o produto já existente na lista.
- **RN-06:** A Lista de Pedidos não possui vínculo com registros de pedidos de compra; ela existe exclusivamente para organizar as anotações de produtos que o comerciante quer ou planeja comprar daquele fornecedor.

---

## Critérios de Aceite

**CA-01: Criação de múltiplas listas para o mesmo fornecedor e anotação livre de produtos**
**Dado** que o comerciante autenticado possui o fornecedor ativo `"Distribuidora Central"`
**Quando** criar a lista `"Reposição de Segunda"` e a lista `"Itens de Fim de Semana"` para esse mesmo fornecedor e cadastrar múltiplos produtos dentro de cada uma
**Então** o sistema salva ambas as listas vinculadas ao fornecedor `"Distribuidora Central"` com `ativa = true`
**E** registra todos os produtos dentro de suas respectivas listas.

**CA-02: Edição contínua da lista como bloco de notas**
**Dado** uma lista de pedidos ativa contendo 3 produtos anotados
**Quando** o comerciante adicionar um 4º produto, alterar a quantidade de um produto existente e remover outro produto da lista
**Então** o sistema atualiza imediatamente os produtos da lista e a `dataAtualizacao`
**E** a lista permanece livre para novas anotações a qualquer momento.

**CA-03: Bloqueio de criação de lista para fornecedor inativo ou de outro comerciante**
**Dado** que o fornecedor `"Atacado Sul"` está com `ativo = false`
**Quando** o comerciante tentar criar uma nova lista de pedidos para `"Atacado Sul"`
**Então** o sistema rejeita a operação e exibe `"Não é possível criar lista de pedidos para um fornecedor inativo"`
**E** nenhuma lista é criada.

**CA-04: Impedimento de produto duplicado na mesma lista**
**Dado** uma lista de pedidos que já possui o item `"Açúcar Cristal 5kg"` com unidade `"FD"`
**Quando** o comerciante tentar adicionar novamente `"açúcar cristal 5kg"` com a mesma unidade `"FD"` na mesma lista
**Então** o sistema rejeita a inclusão e exibe `"Este produto já está registrado nesta lista com a mesma unidade de medida"`.

---

## Restrições
- **Restrições de Negócio e Escopo:**
  - Não deve haver campos de preço ou valor monetário na Lista de Pedidos, e a lista jamais deve ficar vinculada ou bloqueada por um registro de pedido de compra.
  - Esta spec depende exclusivamente das `spec-revisada-001` e `spec-revisada-002`, e **não pode depender** de nenhuma spec posterior (`spec-revisada-004` a `spec-revisada-008`).
- **Limites de Stack:**
  - A solução deve compor uma aplicação web SPA em **Angular** (com renderização client-side das informações não confidenciais) comunicando-se com um back-end em **Spring Boot** operando como **API REST Stateless**.
  - A camada de persistência deve utilizar abstração **JPA / Hibernate**, executando inicialmente com banco de dados em memória (in-memory) e mantendo total compatibilidade estrutural para migração ao **PostgreSQL**.
- **Padrões de Repositório:**
  - O código-fonte do projeto deve residir em uma **macro pasta única**, organizada estritamente em duas pastas raiz: `/front` (contendo o projeto SPA Angular) e `/back` (contendo o projeto API REST Spring Boot).
