# Spec Revisada 004 – Envio de Lista de Pedidos via WhatsApp

- **Ordem de Complexidade:** 4 de 8
- **Dependências:** `spec-revisada-001` (Cadastro e Autenticação de Comerciante), `spec-revisada-002` (Gestão de Fornecedores) e `spec-revisada-003` (Gestão de Listas de Pedidos e Produtos)

---

## Objetivo
Permitir que o comerciante autenticado envie diretamente uma lista de pedidos preenchida para o WhatsApp do fornecedor vinculado, gerando uma mensagem completa e padronizada (com a identificação do estabelecimento comprador, do fornecedor e de todos os produtos anotados na lista), mantendo a lista livre como bloco de notas para novos envios a qualquer momento.

---

## Escopo
**Inclui:**
- Validar se a lista pertence ao comerciante autenticado, está ativa (`ativa = true`), possui ao menos um produto anotado e está associada a um fornecedor ativo com telefone válido.
- Gerar automaticamente o texto estruturado da mensagem contendo: identificação do estabelecimento do comerciante (`spec-revisada-001`), nome/contato do fornecedor (`spec-revisada-002`), título da lista e relação de produtos com quantidades, unidades e observações (`spec-revisada-003`).
- Codificar o texto de forma segura (preservando acentuação, símbolos e quebras de linha) e acionar o direcionamento direto para o WhatsApp do fornecedor.
- Registrar a data/hora do último envio (`dataUltimoEnvio`) e incrementar o contador de envios (`quantidadeEnvios`) da lista, sem bloquear ou alterar a capacidade de edição contínua da lista.

**Não inclui:**
- Recebimento automático de mensagens ou respostas do fornecedor dentro do sistema.
- Geração ou confirmação do registro de pedido de compra em modo rascunho ou confirmado (escopo da `spec-revisada-005`).
- Registro de recebimento físico (escopo da `spec-revisada-006`) ou avaliação do fornecedor (escopo da `spec-revisada-007`).

---

## Atores
- **Cliente (Comerciante):** Autenticado, pré-visualiza o resumo da lista de pedidos e aciona o envio direto para o WhatsApp do fornecedor quantas vezes desejar.
- **Sistema:** Verifica as condições de envio, compõe a mensagem identificando o comércio comprador e os itens anotados na lista, codifica o link de acionamento do WhatsApp com DDI `55` e registra a data/hora e a contagem de envios da lista.

---

## Dados – Envio da Lista via WhatsApp
```text
listaPedido:       referência à Lista de Pedidos (spec-revisada-003), obrigatório, pertencente ao comerciante autenticado
telefoneDestino:   texto, obrigatório, obtido do telefoneWhatsapp higienizado do Fornecedor (spec-revisada-002) prefixado com DDI "55"
mensagemFormatada: texto, obrigatório, gerado pelo sistema contendo nomeEstabelecimento, nome do fornecedor/contato, título e itens
urlWhatsapp:       texto, obrigatório, endereço de acionamento direto do WhatsApp com telefoneDestino e mensagemFormatada codificada
dataUltimoEnvio:   data/hora, opcional (preenchido/atualizado automaticamente pelo sistema a cada envio)
quantidadeEnvios:  numérico inteiro, obrigatório, maior ou igual a 0, padrão = 0
```

---

## Regras de Negócio
- **RN-01:** O envio via WhatsApp só é permitido para listas ativas (`ativa = true`) pertencentes ao comerciante autenticado (`spec-revisada-001`) que possuam pelo menos 1 (um) produto cadastrado (`spec-revisada-003`).
- **RN-02:** O fornecedor vinculado à lista deve estar com `ativo = true` e possuir `telefoneWhatsapp` válido (`spec-revisada-002`).
- **RN-03:** A `mensagemFormatada` gerada pelo sistema deve conter obrigatoriamente:
  1. Nome do estabelecimento (`nomeEstabelecimento`) e responsável (`nomeResponsavel`) do comerciante (`spec-revisada-001`);
  2. Saudação ao fornecedor (utilizando `nomeContato`, se cadastrado, ou `nome` do fornecedor da `spec-revisada-002`);
  3. Título da lista e relação numerada dos produtos no formato `"- [quantidade] [unidadeMedida] x [nomeProduto]"` (seguido de `observacaoItem`, quando houver);
  4. Observação geral da lista (quando houver).
- **RN-04:** Todos os caracteres especiais, quebras de linha e acentos da `mensagemFormatada` devem ser codificados ao compor a `urlWhatsapp` para impedir truncamento da mensagem no WhatsApp.
- **RN-05:** A cada acionamento de envio da lista para o WhatsApp, o sistema deve gravar a data e hora atuais em `dataUltimoEnvio` e incrementar `quantidadeEnvios` em +1, mantendo a lista e seus produtos totalmente abertos para futuras anotações e novos envios.

---

## Critérios de Aceite

**CA-01: Envio de lista preenchida com identificação completa do comerciante e fornecedor**
**Dado** que o comerciante autenticado do `"Mercadinho São José"` possui uma lista ativa `"Reposição Semanal"` para o fornecedor ativo `"Distribuidora Central"` (telefone `"64999991234"`) com 2 produtos anotados
**Quando** o comerciante acionar o envio da lista por WhatsApp
**Então** o sistema gera a mensagem contendo a identificação `"Mercadinho São José"`, o título `"Reposição Semanal"` e os 2 produtos com suas quantidades e unidades
**E** direciona o envio para o número `"5564999991234"` com o texto codificado
**E** preenche `dataUltimoEnvio` e atualiza `quantidadeEnvios = 1`.

**CA-02: Bloqueio de envio para lista sem produtos**
**Dado** uma lista de pedidos ativa sem nenhum produto anotado
**Quando** o comerciante tentar enviá-la por WhatsApp
**Então** o sistema bloqueia a operação e exibe `"Adicione pelo menos um produto à lista antes de enviá-la por WhatsApp"`.

**CA-03: Reenvio de lista após novas anotações no bloco de notas**
**Dado** uma lista de pedidos que já foi enviada anteriormente (`quantidadeEnvios = 1`)
**Quando** o comerciante anotar um novo produto ou alterar uma quantidade na lista e acionar novamente o envio por WhatsApp
**Então** o sistema gera a mensagem atualizada com os produtos vigentes da lista
**E** atualiza `dataUltimoEnvio` e incrementa `quantidadeEnvios` para `2`.

**CA-04: Bloqueio de envio quando o fornecedor foi inativado**
**Dado** uma lista de pedidos com produtos cujo fornecedor vinculado foi inativado (`ativo = false`)
**Quando** o comerciante tentar enviar a lista por WhatsApp
**Então** o sistema rejeita a operação e exibe `"Não é possível enviar a lista pois o fornecedor vinculado está inativo"`.

---

## Restrições
- **Restrições de Negócio e Escopo:**
  - O sistema não deve realizar disparos automáticos em segundo plano sem ação explícita do comerciante nem depender de API paga da Meta; o envio ocorre via acionamento direto para o WhatsApp com a mensagem estruturada.
  - Esta spec depende exclusivamente das `spec-revisada-001`, `spec-revisada-002` e `spec-revisada-003`, e **não pode depender** de nenhuma spec posterior (`spec-revisada-005` a `spec-revisada-008`).
- **Limites de Stack:**
  - A solução deve compor uma aplicação web SPA em **Angular** (com renderização client-side das informações não confidenciais) comunicando-se com um back-end em **Spring Boot** operando como **API REST Stateless**.
  - A camada de persistência deve utilizar abstração **JPA / Hibernate**, executando inicialmente com banco de dados em memória (in-memory) e mantendo total compatibilidade estrutural para migração ao **PostgreSQL**.
- **Padrões de Repositório:**
  - O código-fonte do projeto deve residir em uma **macro pasta única**, organizada estritamente em duas pastas raiz: `/front` (contendo o projeto SPA Angular) e `/back` (contendo o projeto API REST Spring Boot).
