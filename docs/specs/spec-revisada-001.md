# Spec Revisada 001 – Cadastro e Autenticação de Comerciante (Cliente)

- **Ordem de Complexidade:** 1 de 8 (Menor complexidade)
- **Dependências:** Nenhuma

---

## Objetivo
Permitir que o pequeno comerciante crie uma conta própria no Micro-SaaS, gerencie seus dados básicos de identificação e realize login (autenticação) e logout com segurança, garantindo que cada comerciante tenha um ambiente exclusivo e isolado para organizar seus fornecedores, listas e pedidos de compra.

---

## Escopo
**Inclui:**
- Criar conta de comerciante (auto-cadastro) informando nome do responsável, nome do estabelecimento, e-mail, telefone, documento opcional e senha.
- Autenticar o comerciante no sistema (login) mediante e-mail e senha válidos, iniciando sessão autenticada.
- Encerrar a sessão autenticada (logout).
- Consultar e atualizar os dados básicos do perfil do comerciante autenticado (nome, nome do estabelecimento e telefone).
- Inativar a própria conta do comerciante logicamente.

**Não inclui:**
- Recuperação de senha via envio de SMS ou links externos por servidor de e-mail de terceiros.
- Autenticação social (login com Google, Facebook ou Apple).
- Gestão de múltiplos usuários/funcionários com diferentes níveis de permissão dentro do mesmo estabelecimento (multi-perfil por loja).
- Cadastro de fornecedores, listas de pedidos ou compras (escopo das `spec-revisada-002` em diante).

---

## Atores
- **Cliente (Comerciante):** Realiza o próprio cadastro na plataforma, efetua login e logout, visualiza e edita os dados do seu estabelecimento.
- **Sistema:** Valida a obrigatoriedade, o formato e a unicidade do e-mail e documento informados, armazena a senha de forma criptografada (nunca em texto puro), valida as credenciais no login, emite e valida a credencial de sessão stateless e garante o isolamento dos dados do comerciante autenticado.

---

## Dados – Comerciante (Cliente)
```text
nomeResponsavel:    texto, obrigatório, não único, 3 a 100 caracteres (nome do comerciante)
nomeEstabelecimento:texto, obrigatório, não único, 2 a 100 caracteres (nome fantasia da empresa/comércio)
email:              texto, obrigatório, único no sistema, formato válido de e-mail, até 120 caracteres (usado como login)
senha:              texto, obrigatório, não único, mínimo de 8 e máximo de 64 caracteres (armazenada sob hash irreversível)
telefone:           texto, opcional, não único, 10 ou 11 dígitos numéricos com DDD (armazenado apenas com números)
documento:          texto, opcional, único no sistema quando informado, CPF (11 dígitos) ou CNPJ (14 dígitos) apenas números
ativo:              booleano, obrigatório, não único, padrão = true no cadastro
dataCadastro:       data/hora, obrigatório, não único, gerado automaticamente pelo sistema na criação
```

---

## Regras de Negócio
- **RN-01:** O campo `email` é o identificador de login do comerciante e deve ser único em todo o sistema (desconsiderando diferenças entre maiúsculas e minúsculas); tentativas de cadastro com e-mail já existente devem ser rejeitadas.
- **RN-02:** O campo `documento` (CPF ou CNPJ) é opcional, mas quando informado deve ser higienizado (apenas dígitos numéricos), conter exatamente 11 ou 14 dígitos e ser único em todo o sistema.
- **RN-03:** A `senha` informada no cadastro deve possuir no mínimo 8 caracteres, contendo pelo menos uma letra e um número, e nunca deve ser armazenada ou trafegada em respostas de consulta em texto aberto.
- **RN-04:** Todo novo comerciante cadastrado recebe automaticamente `ativo = true` e `dataCadastro` com a data e hora atuais do sistema.
- **RN-05:** O login só deve ser autorizado quando o `email` existir, a `senha` corresponder ao registro armazenado e o comerciante estiver com `ativo = true`.
- **RN-06:** Por segurança, em caso de e-mail inexistente ou senha incorreta no login, o sistema deve retornar uma mensagem genérica única (`"E-mail ou senha inválidos"`), sem revelar qual dos dois campos está incorreto.
- **RN-07:** A inativação da conta do comerciante define `ativo = false` sem exclusão física dos dados, impedindo novos logins até eventual reativação.

---

## Critérios de Aceite

**CA-01: Cadastro de conta de comerciante com dados válidos**
**Dado** que não existe conta cadastrada com o e-mail `"joao@mercadinho.com.br"`
**Quando** o comerciante preencher o cadastro com nomeResponsavel `"João Silva"`, nomeEstabelecimento `"Mercadinho São José"`, email `"joao@mercadinho.com.br"` e senha `"Compra123"`
**Então** o sistema cria a conta com `ativo = true` e `dataCadastro` preenchida
**E** armazena a senha de forma protegida
**E** exibe a mensagem `"Conta criada com sucesso"`.

**CA-02: Rejeição de cadastro com e-mail duplicado**
**Dado** que já existe um comerciante cadastrado com o e-mail `"joao@mercadinho.com.br"`
**Quando** outro usuário tentar criar uma conta informando `"JOAO@mercadinho.com.br"`
**Então** o sistema rejeita o cadastro e exibe a mensagem `"Este endereço de e-mail já está em uso"`
**E** nenhum novo registro é criado.

**CA-03: Login realizado com sucesso por comerciante ativo**
**Dado** um comerciante cadastrado com `ativo = true`, email `"joao@mercadinho.com.br"` e senha `"Compra123"`
**Quando** o comerciante realizar o login informando essas credenciais
**Então** o sistema autentica o comerciante, inicia sua sessão autenticada e libera o acesso às funcionalidades restritas do sistema.

**CA-04: Bloqueio de login com credenciais incorretas ou conta inativa**
**Dado** um comerciante cadastrado com email `"joao@mercadinho.com.br"`
**Quando** tentar efetuar login informando uma senha incorreta `"SenhaErrada99"`
**Então** o sistema nega o acesso e exibe a mensagem `"E-mail ou senha inválidos"`.

**CA-05: Rejeição de senha fraca no momento do cadastro**
**Dado** que um novo comerciante está preenchendo o formulário de criação de conta
**Quando** informar uma senha com menos de 8 caracteres (ex.: `"12345"`) ou sem combinação de letras e números
**Então** o sistema impede o cadastro e exibe a mensagem `"A senha deve ter no mínimo 8 caracteres, incluindo letras e números"`.

---

## Restrições
- **Restrições de Negócio e Escopo:**
  - Não existe exclusão física de conta de comerciante; a senha jamais pode ser retornada em consultas do perfil.
  - Esta spec é a base do sistema e **não pode depender** de nenhuma spec posterior (`spec-revisada-002` a `spec-revisada-008`).
- **Limites de Stack:**
  - A solução deve compor uma aplicação web SPA em **Angular** (com renderização client-side das informações não confidenciais) comunicando-se com um back-end em **Spring Boot** operando como **API REST Stateless**.
  - A camada de persistência deve utilizar abstração **JPA / Hibernate**, executando inicialmente com banco de dados em memória (in-memory) e mantendo total compatibilidade estrutural para migração ao **PostgreSQL**.
- **Padrões de Repositório:**
  - O código-fonte do projeto deve residir em uma **macro pasta única**, organizada estritamente em duas pastas raiz: `/front` (contendo o projeto SPA Angular) e `/back` (contendo o projeto API REST Spring Boot).
