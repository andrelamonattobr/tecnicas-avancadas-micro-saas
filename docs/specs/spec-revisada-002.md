# Spec Revisada 002 – Gestão de Fornecedores

- **Ordem de Complexidade:** 2 de 8
- **Dependências:** `spec-revisada-001` (Cadastro e Autenticação de Comerciante)

---

## Objetivo
Permitir que o comerciante autenticado cadastre, consulte, pesquise, edite e gerencie a situação cadastral (ativo/inativo) de seus fornecedores em uma base própria e isolada, garantindo que os dados de contato comercial e WhatsApp estejam padronizados para o envio de listas e registro de pedidos.

---

## Escopo
**Inclui:**
- Cadastrar novo fornecedor vinculado exclusivamente à conta do comerciante autenticado (`spec-revisada-001`).
- Higienizar automaticamente os campos numéricos de telefone e documento (removendo máscaras como parênteses, pontos, barras e traços).
- Listar e pesquisar fornecedores do comerciante autenticado (por padrão apenas os ativos, com filtros por status ativo/inativo, nome ou categoria).
- Editar informações cadastrais de um fornecedor pertencente ao comerciante.
- Inativar e reativar logicamente um fornecedor.

**Não inclui:**
- Exclusão física (definitiva) de fornecedores.
- Compartilhamento de cadastro de fornecedores entre contas de comerciantes diferentes.
- Criação de listas de pedidos ou produtos (escopo da `spec-revisada-003`).
- Disparo de mensagens por WhatsApp (escopo da `spec-revisada-004`).
- Registro de pedidos de compra, recebimentos ou avaliações (escopo das `spec-revisada-005` a `spec-revisada-007`).

---

## Atores
- **Cliente (Comerciante):** Autenticado no sistema, realiza o cadastro, consulta, filtro, edição, inativação e reativação de seus fornecedores.
- **Sistema:** Garante o isolamento dos fornecedores por comerciante autenticado (`spec-revisada-001`), higieniza máscaras de telefone e documento, valida a unicidade de telefone e documento dentro da carteira daquele comerciante, define `ativo = true` na criação e impede a exclusão física.

---

## Dados – Fornecedor
```text
comerciante:      referência a Comerciante (spec-revisada-001), obrigatório, preenchido automaticamente com o comerciante autenticado
nome:             texto, obrigatório, não único, 3 a 100 caracteres (nome fantasia ou razão social do fornecedor)
nomeContato:      texto, opcional, não único, 2 a 80 caracteres (nome do vendedor ou representante comercial)
telefoneWhatsapp: texto, obrigatório, não único por comerciante, 10 ou 11 dígitos numéricos com DDD (armazenado sem máscara, ex.: 64999991234)
documento:        texto, opcional, único por comerciante quando informado, 11 dígitos (CPF) ou 14 dígitos (CNPJ) numéricos sem máscara
email:            texto, opcional, não único, formato válido de e-mail, até 120 caracteres
categoria:        texto, opcional, não único, 2 a 60 caracteres (ex.: Bebidas, Hortifrúti, Embalagens)
ativo:            booleano, obrigatório, não único, padrão = true no cadastro
dataCadastro:     data/hora, obrigatório, não único, gerado automaticamente pelo sistema na criação
```

---

## Regras de Negócio
- **RN-01:** Todo fornecedor pertence estritamente ao `comerciante` autenticado que o cadastrou (`spec-revisada-001`); um comerciante nunca pode visualizar, editar ou inativar fornecedores pertencentes a outro comerciante.
- **RN-02:** Antes de validar e salvar, o sistema deve higienizar `telefoneWhatsapp` e `documento`, removendo quaisquer caracteres não numéricos (espaços, parênteses, traços, pontos e barras).
- **RN-03:** O campo `telefoneWhatsapp` é obrigatório e deve ser único **dentro da carteira do mesmo comerciante**; dois comerciantes distintos podem cadastrar o mesmo telefone de fornecedor em suas respectivas contas, mas um mesmo comerciante não pode ter dois fornecedores com o mesmo telefone.
- **RN-04:** O campo `documento` é opcional, mas quando informado deve possuir exatamente 11 dígitos (CPF) ou 14 dígitos (CNPJ) e ser único **dentro da carteira do mesmo comerciante**.
- **RN-05:** No momento do cadastro, o sistema define automaticamente `ativo = true` e `dataCadastro` com a data/hora atual.
- **RN-06:** A inativação altera apenas o campo `ativo` para `false`, não existindo exclusão física; fornecedores inativos deixam de aparecer na listagem padrão, mas continuam consultáveis mediante filtro explícito e podem ser reativados (`ativo = true`).

---

## Critérios de Aceite

**CA-01: Cadastro de fornecedor com higienização automática de máscara**
**Dado** que o comerciante `"João"` está autenticado e não possui fornecedor com o telefone `"64999991234"`
**Quando** ele cadastrar o fornecedor `"Distribuidora Central"` informando telefoneWhatsapp `"(64) 99999-1234"`, documento `"12.345.678/0001-99"` e nomeContato `"Carlos Vendedor"`
**Então** o sistema salva o fornecedor vinculado ao comerciante `"João"` com telefoneWhatsapp `"64999991234"`, documento `"12345678000199"` e `ativo = true`
**E** exibe a confirmação `"Fornecedor cadastrado com sucesso"`.

**CA-02: Rejeição de telefone duplicado para o mesmo comerciante**
**Dado** que o comerciante `"João"` já possui um fornecedor cadastrado com telefoneWhatsapp `"64999991234"`
**Quando** o próprio `"João"` tentar cadastrar outro fornecedor com telefone `"(64) 99999-1234"`
**Então** o sistema rejeita a operação e exibe `"Telefone de WhatsApp já cadastrado para outro fornecedor da sua conta"`
**E** nenhum novo registro é criado.

**CA-03: Permissão de mesmo fornecedor em contas de comerciantes diferentes (Isolamento Multi-Inquilino)**
**Dado** que o comerciante `"João"` possui um fornecedor com telefoneWhatsapp `"64999991234"` e documento `"12345678000199"`
**Quando** outra comerciante autenticada `"Maria"` cadastrar em sua própria conta um fornecedor com o mesmo telefone `"64999991234"` e documento `"12345678000199"`
**Então** o sistema permite o cadastro normalmente na conta de `"Maria"`
**E** cada comerciante visualiza exclusivamente o seu próprio registro.

**CA-04: Inativação e ocultação na listagem padrão**
**Dado** um fornecedor ativo pertencente ao comerciante autenticado
**Quando** o comerciante inativar esse fornecedor
**Então** o sistema define `ativo = false` para o registro
**E** o fornecedor deixa de ser exibido na listagem padrão de ativos, permanecendo acessível ao filtrar por `"Inativos"`.

---

## Restrições
- **Restrições de Negócio e Escopo:**
  - É proibida a exclusão física de fornecedores e o acesso cruzado a dados de outros comerciantes.
  - Esta spec depende exclusivamente da `spec-revisada-001` e **não pode depender** de nenhuma spec posterior (`spec-revisada-003` a `spec-revisada-008`).
- **Limites de Stack:**
  - A solução deve compor uma aplicação web SPA em **Angular** (com renderização client-side das informações não confidenciais) comunicando-se com um back-end em **Spring Boot** operando como **API REST Stateless**.
  - A camada de persistência deve utilizar abstração **JPA / Hibernate**, executando inicialmente com banco de dados em memória (in-memory) e mantendo total compatibilidade estrutural para migração ao **PostgreSQL**.
- **Padrões de Repositório:**
  - O código-fonte do projeto deve residir em uma **macro pasta única**, organizada estritamente em duas pastas raiz: `/front` (contendo o projeto SPA Angular) e `/back` (contendo o projeto API REST Spring Boot).
