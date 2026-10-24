# Plano de Desenvolvimento Sequencial – Micro-SaaS (Specs 001 e 002)

Este documento define o planejamento sequencial, gradual e controlado para a implementação das especificações:
- **[Spec Revisada 001](file:///c:/Nova%20pasta/TecAvanc/tecnicas-avancadas-micro-saas/docs/specs/spec-revisada-001.md):** Cadastro e Autenticação de Comerciante (Cliente)
- **[Spec Revisada 002](file:///c:/Nova%20pasta/TecAvanc/tecnicas-avancadas-micro-saas/docs/specs/spec-revisada-002.md):** Gestão de Fornecedores

---

## 1. Diretrizes Gerais, Arquitetura e Governança

### 1.1. Política de Execução Zero e Análise Estática (`static-code-analyst`)
- **Proibição de Execução de Build/Testes:** Nenhum comando de compilação, execução de testes ou servidor (`mvn`, `mvnw`, `npm`, `ng`, `node`) deve ser executado no terminal.
- **Inspeção Estática Rigorosa:** Toda validação deve ser feita via inspeção manual de código (AST mental, verificação de imports, tipos, assinaturas de métodos, anotações Spring/JPA/Angular, injeção de dependências e aderência estrita às Regras de Negócio e Critérios de Aceite).
- **Segurança de Credenciais (`GEMINI.md`):** É estritamente vedada a leitura, exibição ou commit de arquivos de segredos locais (`.env`, `application.properties`, `application.yaml` ou chaves privadas).
- **Restrição de Dependências:** Utilizar exclusivamente as dependências já configuradas em `msaas/back/pom.xml` (Spring Boot 4.1.2, Spring Data JPA, Spring Security, `com.auth0:java-jwt`, H2) e `msaas/msaas-front/package.json` (Angular 22.2, Router, Forms, RxJS). Não adicionar novas bibliotecas.

### 1.2. Convenção de Rastreabilidade com Git
Cada etapa do plano corresponde a um ciclo atômico finalizado com `git add` e `git commit`:
- `🔴` **Antes da mensagem:** Commit onde os testes de Critérios de Aceite são criados para falhar propositalmente (Fase *Red* do TDD).
- `🟢` **Antes da mensagem:** Commit onde o código da spec foi implementado e validado estaticamente para fazer os testes passarem (Fase *Green* do TDD).
- `🏗️` **Antes da mensagem:** Commit de refatoração de testes ou refatoração/otimização/simplificação de código (Fase *Refactor* do TDD).
- **Sem símbolo antes da mensagem:** Commits referentes ao desenvolvimento do Frontend ou documentação.

### 1.3. Arquitetura DDD Simplificada

#### Backend (`msaas/back` – Java 25 + Spring Boot)
Pacote base: `com.msaas.back`
```text
com.msaas.back
├── shared
│   ├── domain
│   │   └── exception (RegraNegocioException, CredenciaisInvalidasException, RecursoNaoEncontradoException)
│   ├── infrastructure
│   │   └── security (SecurityConfig, JwtService, JwtAuthenticationFilter, ComercianteAutenticado)
│   └── presentation
│       └── GlobalExceptionHandler (Padronização de respostas HTTP e mensagens de erro)
├── comerciante (Spec 001)
│   ├── domain (Comerciante, ComercianteRepository)
│   ├── application (ComercianteAuthService, dto: RegistroComercianteRequest, LoginRequest, AuthResponse)
│   ├── infrastructure (SpringDataComercianteRepository, ComercianteRepositoryImpl)
│   └── presentation (AuthController)
└── fornecedor (Spec 002)
    ├── domain (Fornecedor, FornecedorRepository)
    ├── application (FornecedorService, dto: FornecedorCreateRequest, FornecedorUpdateRequest, FornecedorResponse, PaginaResponse)
    ├── infrastructure (SpringDataFornecedorRepository, FornecedorRepositoryImpl)
    └── presentation (FornecedorController)
```

#### Frontend (`msaas/msaas-front` – Angular 22 Standalone)
Diretório base: `src/app`
```text
src/app
├── core
│   └── auth (token-storage.service.ts, jwt.interceptor.ts, auth.guard.ts)
├── domains
│   ├── comerciante (Spec 001)
│   │   ├── domain (comerciante.model.ts, auth.model.ts)
│   │   ├── application (auth.service.ts)
│   │   └── presentation
│   │       ├── home-page (rota '/')
│   │       ├── login-page (rota '/login')
│   │       └── registro-page (rota '/registro')
│   ├── aplicacao (Spec 001 / Shell Principal)
│   │   └── presentation
│   │       └── aplicacao-layout (rota '/aplicacao' com menu hambúrguer à esquerda e área funcional centro/direita)
│   └── fornecedor (Spec 002)
│       ├── domain (fornecedor.model.ts, pagina.model.ts)
│       ├── application (fornecedor.service.ts)
│       └── presentation
│           ├── fornecedor-lista-page (rota '/aplicacao/fornecedores')
│           └── fornecedor-form-page (rotas '/aplicacao/fornecedores/novo' e '/aplicacao/fornecedores/:id/editar')
```

### 1.4. Identidade Visual do Frontend
- **Paleta Principal:** Azul (`#1E3A8A` primário, `#2563EB` interativo/botões, `#EFF6FF` superfícies leves) e Branco (`#FFFFFF` fundo principal e cards).
- **Tipografia e Leitura:** Textos principais em preto/grafite escuro (`#0F172A`) e textos secundários em cinza escuro (`#334155`) garantindo alto contraste e leitura confortável.
- **Estilo:** Minimalista, limpo, sem animações pesadas (apenas transições sutis de `0.15s ease` em hover/focus e abertura do menu hambúrguer).

---

## 2. Roteiro Sequencial de Execução – Spec Revisada 001

### Escopo e Regras da Spec 001
- **Entidade `Comerciante`:**
  - `id` (PK auto-gerado `Long`)
  - `nomeResponsavel` (obrigatório, 3 a 100 caracteres)
  - `nomeEstabelecimento` (obrigatório, 2 a 100 caracteres)
  - `email` (obrigatório, único case-insensitive, formato válido, até 120 caracteres)
  - `senha` (obrigatório, entrada de 8 a 64 caracteres com letras e números; persistida com **SALT + HASH** via `BCryptPasswordEncoder`)
  - `telefone` (opcional, higienizado para 10 ou 11 dígitos numéricos)
  - `documento` (opcional, único quando informado, higienizado para 11 ou 14 dígitos numéricos)
  - `ativo` (booleano, padrão `true` no cadastro)
  - `dataCadastro` (`LocalDateTime`, gerado automaticamente na criação)
- **Foco Funcional:** Registro de conta, Login com emissão de token JWT (`com.auth0:java-jwt`) e Logout.

---

### Passo 1.1 – [Backend Spec 001] Criar Testes Baseados nos Critérios de Aceite (TDD - Red)
**Objetivo:** Escrever os testes automatizados que validam todos os Critérios de Aceite da Spec 001 antes de existir a implementação.

**Arquivos a criar:**
- `msaas/back/src/test/java/com/msaas/back/comerciante/ComercianteAuthAcceptanceTest.java`

**Cenários obrigatórios nos testes:**
1. **CA-01 (Cadastro válido):** Cadastrar com `nomeResponsavel = "João Silva"`, `nomeEstabelecimento = "Mercadinho São José"`, `email = "joao@mercadinho.com.br"` e `senha = "Compra123"`. Verificar criação com `ativo = true`, `dataCadastro` preenchida, senha armazenada com hash (diferente de `"Compra123"` e validável via BCrypt) e mensagem `"Conta criada com sucesso"`.
2. **CA-02 (Rejeição de e-mail duplicado case-insensitive):** Dado `"joao@mercadinho.com.br"` já cadastrado, tentar cadastrar `"JOAO@mercadinho.com.br"` e verificar rejeição com a mensagem `"Este endereço de e-mail já está em uso"`.
3. **CA-03 (Login com sucesso por comerciante ativo):** Realizar login com `"joao@mercadinho.com.br"` e `"Compra123"`, verificando retorno de token JWT válido e acesso autorizado.
4. **CA-04 (Bloqueio de login com credenciais incorretas ou conta inativa):** Tentar login com senha `"SenhaErrada99"` (ou conta com `ativo = false`) e verificar erro com a mensagem `"E-mail ou senha inválidos"`.
5. **CA-05 (Rejeição de senha fraca no cadastro):** Tentar cadastrar com senha `"12345"` (menos de 8 caracteres ou sem combinação de letras e números) e verificar erro com a mensagem `"A senha deve ter no mínimo 8 caracteres, incluindo letras e números"`.

**Comandos Git ao finalizar o passo:**
```powershell
git add msaas/back/src/test/java/com/msaas/back/comerciante/ComercianteAuthAcceptanceTest.java
git commit -m "🔴 test(back): cria testes de criterios de aceite da spec 001 para registro e login"
```

---

### Passo 1.2 – [Backend Spec 001] Desenvolver o Código do Backend em DDD + Análise Estática (TDD - Green)
**Objetivo:** Implementar as camadas de domínio, aplicação, infraestrutura (JWT + BCrypt + JPA) e apresentação para atender integralmente à Spec 001.

**Arquivos a criar:**
1. **Exceções Compartilhadas (`shared/domain/exception`):**
   - `msaas/back/src/main/java/com/msaas/back/shared/domain/exception/RegraNegocioException.java`
   - `msaas/back/src/main/java/com/msaas/back/shared/domain/exception/CredenciaisInvalidasException.java`
2. **Domínio (`comerciante/domain`):**
   - `msaas/back/src/main/java/com/msaas/back/comerciante/domain/Comerciante.java`
     - Mapeamento JPA `@Entity`, `@Table(name = "comerciantes")`.
     - Regras de validação e higienização no domínio (`RN-01` a `RN-07`): normalização de e-mail em minúsculas, validação de força da senha bruta (`>= 8` caracteres, ao menos 1 letra e 1 dígito), higienização de `telefone` e `documento`, inicialização automática de `ativo = true` e `dataCadastro = LocalDateTime.now()`.
   - `msaas/back/src/main/java/com/msaas/back/comerciante/domain/ComercianteRepository.java`
3. **Infraestrutura de Segurança e Persistência (`shared/infrastructure/security` e `comerciante/infrastructure`):**
   - `msaas/back/src/main/java/com/msaas/back/shared/infrastructure/security/ComercianteAutenticado.java` (Principal contendo `id`, `email`, `nomeEstabelecimento`).
   - `msaas/back/src/main/java/com/msaas/back/shared/infrastructure/security/JwtService.java` (Geração e validação de token JWT usando `com.auth0.jwt.JWT` e `Algorithm.HMAC256` com chave padrão segura em memória caso não injetada).
   - `msaas/back/src/main/java/com/msaas/back/shared/infrastructure/security/JwtAuthenticationFilter.java` (Filtro `OncePerRequestFilter` que extrai o header `Authorization: Bearer <token>` e popula o `SecurityContextHolder`).
   - `msaas/back/src/main/java/com/msaas/back/shared/infrastructure/security/SecurityConfig.java` (Configuração `SecurityFilterChain` stateless, CORS liberado para `http://localhost:4200`, `BCryptPasswordEncoder` como `@Bean` garantindo SALT + HASH, rotas `/api/auth/**` públicas e demais `/api/**` autenticadas).
   - `msaas/back/src/main/java/com/msaas/back/comerciante/infrastructure/SpringDataComercianteRepository.java`
   - `msaas/back/src/main/java/com/msaas/back/comerciante/infrastructure/ComercianteRepositoryImpl.java`
4. **Aplicação (`comerciante/application`):**
   - `msaas/back/src/main/java/com/msaas/back/comerciante/application/dto/RegistroComercianteRequest.java`
   - `msaas/back/src/main/java/com/msaas/back/comerciante/application/dto/RegistroComercianteResponse.java`
   - `msaas/back/src/main/java/com/msaas/back/comerciante/application/dto/LoginRequest.java`
   - `msaas/back/src/main/java/com/msaas/back/comerciante/application/dto/AuthResponse.java`
   - `msaas/back/src/main/java/com/msaas/back/comerciante/application/ComercianteAuthService.java`
5. **Apresentação (`comerciante/presentation` e `shared/presentation`):**
   - `msaas/back/src/main/java/com/msaas/back/comerciante/presentation/AuthController.java` (`POST /api/auth/registro`, `POST /api/auth/login`, `POST /api/auth/logout`)
   - `msaas/back/src/main/java/com/msaas/back/shared/presentation/GlobalExceptionHandler.java`

**Checklist Obrigatório de Análise Estática Antes do Commit (`static-code-analyst`):**
- [ ] Todos os imports pertencem ao Jakarta EE (`jakarta.persistence.*`, `jakarta.servlet.*`) compatíveis com Spring Boot 4.
- [ ] O `BCryptPasswordEncoder` é aplicado somente após validar a força da senha em texto plano (`CA-05`).
- [ ] As mensagens de retorno batem caractere por caractere com os Critérios de Aceite (`"Conta criada com sucesso"`, `"Este endereço de e-mail já está em uso"`, `"E-mail ou senha inválidos"`, `"A senha deve ter no mínimo 8 caracteres, incluindo letras e números"`).
- [ ] A senha nunca é exposta em DTOs de resposta.

**Comandos Git ao finalizar o passo:**
```powershell
git add msaas/back/src/main/java/com/msaas/back/
git commit -m "🟢 feat(back): implementa dominio, seguranca JWT e endpoints da spec 001"
```

---

### Passo 1.3 – [Backend Spec 001] Refatorar os Testes e Auditar Estaticamente (TDD - Refactor Tests)
**Objetivo:** Revisar `ComercianteAuthAcceptanceTest.java` frente à implementação real criada no Passo 1.2, ajustando injeções, imports de DTOs/serviços, limpeza de dados entre testes (`@Transactional` ou limpeza explícita) e asserções de token JWT.

**Arquivos a revisar/editar:**
- `msaas/back/src/test/java/com/msaas/back/comerciante/ComercianteAuthAcceptanceTest.java`

**Checklist de Análise Estática dos Testes:**
- [ ] Verificar se as assinaturas de `ComercianteAuthService`, `AuthController` e DTOs usadas no teste coincidem 100% com as classes criadas no Passo 1.2.
- [ ] Verificar se os testes são independentes entre si (isolamento de estado do banco em memória H2).
- [ ] Confirmar que `BackApplicationTests.java` continua íntegro.

**Comandos Git ao finalizar o passo:**
```powershell
git add msaas/back/src/test/java/com/msaas/back/
git commit -m "🏗️ test(back): refatora testes da spec 001 alinhando contratos e isolamento"
```

---

### Passo 1.4 – [Backend Spec 001] Refatorar e Simplificar o Código do Backend + Análise Estática (TDD - Refactor Code)
**Objetivo:** Otimizar o código de produção da Spec 001 visando simplicidade máxima (regra "Simplicidade primeiro" do `GEMINI.md`), eliminando código redundante e garantindo coesão nas validações do domínio.

**Arquivos a revisar/editar:**
- Classes em `msaas/back/src/main/java/com/msaas/back/comerciante/**` e `shared/**`.

**Checklist de Análise Estática:**
- [ ] Extrair ou simplificar métodos privados de validação e higienização de strings em `Comerciante.java`.
- [ ] Garantir que não existam classes, métodos ou parâmetros não utilizados.
- [ ] Confirmar compatibilidade total com os testes refatorados no Passo 1.3.

**Comandos Git ao finalizar o passo:**
```powershell
git add msaas/back/src/main/java/com/msaas/back/
git commit -m "🏗️ refactor(back): simplifica validacoes de dominio e camada de autenticacao da spec 001"
```

---

### Passo 1.5 – [Frontend Spec 001] Criar Rotas, Tema Azul/Branco e Páginas da Spec 001
**Objetivo:** Construir a estrutura DDD do frontend para a Spec 001, incluindo a Tela Inicial (`/`), Tela de Login (`/login`), Tela de Registro (`/registro`) e Tela Principal da Aplicação (`/aplicacao`) com menu hambúrguer à esquerda.

**Requisitos Visuais e de Layout:**
- **Cores base:** Azul (`#1E3A8A` / `#2563EB`) e Branco (`#FFFFFF`), textos em preto/grafite (`#0F172A`) para leitura nítida, design simples e minimalista.
- **Tela Inicial (`/`):**
  - Header superior contendo o nome do sistema e botões/links de direcionamento para **Login** (`/login`) e **Registro** (`/registro`).
  - No centro vertical da tela, um bloco deslocado para a **direita** (`justify-content: flex-end`) apresentando a proposta do sistema e os botões de ação para ir para **Login** ou **Registro**.
- **Tela de Login (`/login`):**
  - Card centralizado em fundo branco/azul suave com campos de `email` e `senha`, botão de entrar e link para registro.
- **Tela de Registro (`/registro`):**
  - Card centralizado com campos de cadastro (`nomeResponsavel`, `nomeEstabelecimento`, `email`, `senha`, e campos opcionais `telefone` e `documento`), feedback de erro/sucesso e link para login.
- **Tela Principal da Aplicação (`/aplicacao`):**
  - Rota raiz autenticada `/aplicacao`.
  - **À esquerda:** Botão e painel de **Menu Hambúrguer** (nesta Spec 001 a lista de funcionalidades do menu estará vazia, exibindo apenas o cabeçalho do menu e opção de sair/logout).
  - **Centro e Direita:** Área principal de conteúdo (`<router-outlet>`) onde ficarão as funcionalidades das próximas specs.

**Arquivos a criar/modificar:**
- `msaas/msaas-front/src/styles.scss` (Variáveis CSS de tema azul/branco, tipografia e utilitários minimalistas)
- `msaas/msaas-front/src/app/app.html` e `msaas/msaas-front/src/app/app.scss`
- `msaas/msaas-front/src/app/app.routes.ts`
- `msaas/msaas-front/src/app/domains/comerciante/domain/comerciante.model.ts`
- `msaas/msaas-front/src/app/domains/comerciante/presentation/home-page/home-page.component.ts` (`.html`, `.scss`)
- `msaas/msaas-front/src/app/domains/comerciante/presentation/login-page/login-page.component.ts` (`.html`, `.scss`)
- `msaas/msaas-front/src/app/domains/comerciante/presentation/registro-page/registro-page.component.ts` (`.html`, `.scss`)
- `msaas/msaas-front/src/app/domains/aplicacao/presentation/aplicacao-layout/aplicacao-layout.component.ts` (`.html`, `.scss`)

**Checklist de Análise Estática do Frontend:**
- [ ] Todos os componentes standalone declaram corretamente `FormsModule` / `RouterLink` / `RouterOutlet` no array `imports`.
- [ ] As rotas `/`, `/login`, `/registro` e `/aplicacao` estão devidamente registradas em `app.routes.ts`.

**Comandos Git ao finalizar o passo:**
```powershell
git add msaas/msaas-front/src/
git commit -m "feat(front): cria rotas e telas inicial, login, registro e layout aplicacao da spec 001"
```

---

### Passo 1.6 – [Frontend Spec 001] Implementar Chamadas REST e Autenticação JWT no Frontend
**Objetivo:** Conectar as telas de Login, Registro e Layout Principal (`/aplicacao`) aos endpoints REST do backend (`POST /api/auth/registro`, `POST /api/auth/login`, `POST /api/auth/logout`), gerenciando o token JWT de forma compatível com SSR (`isPlatformBrowser`).

**Arquivos a criar/modificar:**
- `msaas/msaas-front/src/app/core/auth/token-storage.service.ts` (Leitura/gravação segura de token e dados do estabelecimento no `localStorage` verificando `isPlatformBrowser`).
- `msaas/msaas-front/src/app/core/auth/jwt.interceptor.ts` (`HttpInterceptorFn` que anexa `Authorization: Bearer <token>` nas chamadas para `/api/**`).
- `msaas/msaas-front/src/app/core/auth/auth.guard.ts` (`CanActivateFn` protegendo a rota `/aplicacao` e redirecionando para `/login` caso não autenticado).
- `msaas/msaas-front/src/app/domains/comerciante/application/auth.service.ts` (Chamadas HTTP para registro, login e logout).
- `msaas/msaas-front/src/app/app.config.ts` (Registro de `provideHttpClient(withFetch(), withInterceptors([jwtInterceptor]))`).
- Atualização de `login-page.component.ts`, `registro-page.component.ts` e `aplicacao-layout.component.ts` para consumir `AuthService`.

**Checklist de Análise Estática do Frontend:**
- [ ] Verificar compatibilidade dos payloads JSON enviados pelo `AuthService` com os DTOs do `AuthController` no backend.
- [ ] Garantir que o acesso a `localStorage` em `TokenStorageService` não quebre a hidratação/SSR do Angular (`@angular/ssr`).

**Comandos Git ao finalizar o passo:**
```powershell
git add msaas/msaas-front/src/
git commit -m "feat(front): implementa chamadas REST de autenticacao JWT, interceptor e guard da spec 001"
```

---

## 3. Roteiro Sequencial de Execução – Spec Revisada 002

### Escopo e Regras da Spec 002
- **Entidade `Fornecedor`:**
  - `id` (PK auto-gerado `Long`)
  - `comerciante` (referência obrigatória ao `Comerciante` autenticado)
  - `nome` (obrigatório, 3 a 100 caracteres)
  - `nomeContato` (opcional, 2 a 80 caracteres)
  - `telefoneWhatsapp` (obrigatório, higienizado sem máscara para 10 ou 11 dígitos numéricos, único por comerciante)
  - `documento` (opcional, higienizado sem máscara para 11 dígitos CPF ou 14 dígitos CNPJ, único por comerciante quando informado, **imutável na edição**)
  - `email` (opcional, formato válido, até 120 caracteres)
  - `categoria` (opcional, 2 a 60 caracteres)
  - `ativo` (booleano, padrão `true` no cadastro)
  - `dataCadastro` (`LocalDateTime`, gerado automaticamente na criação)
- **Operações CRUD + Paginação:**
  - Criação vinculada ao comerciante logado.
  - Listagem paginada (`GET /api/fornecedores?page=0&size=10&ativo=true&busca=...`) exclusiva do comerciante logado.
  - Consulta por ID e Edição (`PUT /api/fornecedores/{id}`) onde `id` e `documento` não podem ser alterados.
  - Inativação lógica (`DELETE /api/fornecedores/{id}` ou `PATCH /api/fornecedores/{id}/inativar`) alterando `ativo = false` (sem exclusão física).

---

### Passo 2.1 – [Backend Spec 002] Criar Testes Baseados nos Critérios de Aceite e Paginação (TDD - Red)
**Objetivo:** Criar os testes automatizados da Spec 002 cobrindo todos os Critérios de Aceite (`CA-01` a `CA-04`), consulta paginada e imutabilidade do campo `documento` na edição.

**Arquivos a criar:**
- `msaas/back/src/test/java/com/msaas/back/fornecedor/FornecedorAcceptanceTest.java`

**Cenários obrigatórios nos testes:**
1. **CA-01 (Cadastro com higienização automática de máscara):** Comerciante `"João"` autenticado cadastra `"Distribuidora Central"` com `telefoneWhatsapp = "(64) 99999-1234"`, `documento = "12.345.678/0001-99"` e `nomeContato = "Carlos Vendedor"`. Verificar persistência com `telefoneWhatsapp = "64999991234"`, `documento = "12345678000199"`, `ativo = true` e mensagem `"Fornecedor cadastrado com sucesso"`.
2. **CA-02 (Rejeição de telefone duplicado para o mesmo comerciante):** `"João"` tenta cadastrar outro fornecedor com `"(64) 99999-1234"`. Verificar rejeição com a mensagem `"Telefone de WhatsApp já cadastrado para outro fornecedor da sua conta"`.
3. **CA-03 (Isolamento Multi-Inquilino):** Comerciante `"Maria"` cadastra em sua conta um fornecedor com o mesmo telefone `"64999991234"` e documento `"12345678000199"`. Verificar que o cadastro é permitido e que a listagem paginada de `"João"` e `"Maria"` retorna apenas seus respectivos fornecedores.
4. **CA-04 (Inativação lógica e ocultação na listagem padrão paginada):** Ao inativar um fornecedor ativo, verificar `ativo = false`, ocultação na busca paginada padrão (`ativo = true`) e exibição ao filtrar por inativos (`ativo = false`).
5. **Edição com bloqueio de alteração de documento e Paginação:** Verificar que a atualização (`PUT`) altera dados permitidos (`nome`, `nomeContato`, `telefoneWhatsapp`, `email`, `categoria`), mas rejeita ou preserva inalterado o `documento` original, e que o `GET` retorna estrutura paginada (`content`, `page`, `size`, `totalElements`, `totalPages`).

**Comandos Git ao finalizar o passo:**
```powershell
git add msaas/back/src/test/java/com/msaas/back/fornecedor/FornecedorAcceptanceTest.java
git commit -m "🔴 test(back): cria testes de criterios de aceite, isolamento e paginacao da spec 002"
```

---

### Passo 2.2 – [Backend Spec 002] Desenvolver o Código do Backend em DDD + Análise Estática (TDD - Green)
**Objetivo:** Implementar as camadas DDD do contexto `fornecedor` vinculadas ao comerciante autenticado, incluindo CRUD, inativação lógica, imutabilidade do `documento` na edição e busca paginada.

**Arquivos a criar/modificar:**
1. **Exceção Compartilhada (se necessário):**
   - `msaas/back/src/main/java/com/msaas/back/shared/domain/exception/RecursoNaoEncontradoException.java`
2. **Domínio (`fornecedor/domain`):**
   - `msaas/back/src/main/java/com/msaas/back/fornecedor/domain/Fornecedor.java`
     - Entidade JPA `@Entity`, `@Table(name = "fornecedores")` com `@ManyToOne(fetch = FetchType.LAZY)` para `Comerciante`.
     - Métodos de domínio: criação com higienização de `telefoneWhatsapp` e `documento` (`RN-02`), validação de tamanho/formato (`RN-03`, `RN-04`), atualização cadastral bloqueando modificação do `documento`, e métodos `inativar()` / `reativar()` (`RN-06`).
   - `msaas/back/src/main/java/com/msaas/back/fornecedor/domain/FornecedorRepository.java`
3. **Infraestrutura (`fornecedor/infrastructure`):**
   - `msaas/back/src/main/java/com/msaas/back/fornecedor/infrastructure/SpringDataFornecedorRepository.java` (Consultas com `Pageable` filtrando por `comercianteId`, `ativo` e termo opcional de busca por nome/categoria, além de verificações `existsByComercianteIdAndTelefoneWhatsapp...` e `existsByComercianteIdAndDocumento...`).
   - `msaas/back/src/main/java/com/msaas/back/fornecedor/infrastructure/FornecedorRepositoryImpl.java`
4. **Aplicação (`fornecedor/application`):**
   - `msaas/back/src/main/java/com/msaas/back/fornecedor/application/dto/FornecedorCreateRequest.java`
   - `msaas/back/src/main/java/com/msaas/back/fornecedor/application/dto/FornecedorUpdateRequest.java`
   - `msaas/back/src/main/java/com/msaas/back/fornecedor/application/dto/FornecedorResponse.java`
   - `msaas/back/src/main/java/com/msaas/back/fornecedor/application/dto/PaginaFornecedorResponse.java`
   - `msaas/back/src/main/java/com/msaas/back/fornecedor/application/FornecedorService.java`
5. **Apresentação (`fornecedor/presentation`):**
   - `msaas/back/src/main/java/com/msaas/back/fornecedor/presentation/FornecedorController.java`
     - `POST /api/fornecedores` (Cria fornecedor vinculado ao comerciante do JWT)
     - `GET /api/fornecedores` (Listagem paginada com `page`, `size`, `ativo` padrão `true`, `busca`)
     - `GET /api/fornecedores/{id}` (Detalhe do fornecedor do comerciante logado)
     - `PUT /api/fornecedores/{id}` (Edição mantendo `id` e `documento` imutáveis)
     - `DELETE /api/fornecedores/{id}` e `PATCH /api/fornecedores/{id}/status` (Inativação/reativação lógica)

**Checklist Obrigatório de Análise Estática Antes do Commit (`static-code-analyst`):**
- [ ] Todas as queries em `SpringDataFornecedorRepository` filtram obrigatoriamente por `comerciante.id` (`RN-01`).
- [ ] A higienização remove `[^0-9]` antes de validar tamanho e unicidade de `telefoneWhatsapp` e `documento` (`RN-02`).
- [ ] Na edição (`PUT`), caso seja enviado um `documento` divergente do salvo, lança `RegraNegocioException` informando que o documento não pode ser alterado após o cadastro.
- [ ] Mensagens exatas dos Critérios de Aceite verificadas (`"Fornecedor cadastrado com sucesso"`, `"Telefone de WhatsApp já cadastrado para outro fornecedor da sua conta"`).

**Comandos Git ao finalizar o passo:**
```powershell
git add msaas/back/src/main/java/com/msaas/back/
git commit -m "🟢 feat(back): implementa CRUD paginado de fornecedores com isolamento multi-tenant da spec 002"
```

---

### Passo 2.3 – [Backend Spec 002] Refatorar os Testes e Auditar Estaticamente (TDD - Refactor Tests)
**Objetivo:** Ajustar e refatorar `FornecedorAcceptanceTest.java` para alinhar tipos, parâmetros de paginação, contexto de autenticação do comerciante logado e asserções de exceções.

**Arquivos a revisar/editar:**
- `msaas/back/src/test/java/com/msaas/back/fornecedor/FornecedorAcceptanceTest.java`

**Checklist de Análise Estática dos Testes:**
- [ ] Conferir se todos os imports e chamadas aos métodos de `FornecedorService` / `FornecedorController` correspondem exatamente às assinaturas criadas no Passo 2.2.
- [ ] Verificar cobertura completa de `CA-01`, `CA-02`, `CA-03`, `CA-04`, paginação e bloqueio de edição de documento.

**Comandos Git ao finalizar o passo:**
```powershell
git add msaas/back/src/test/java/com/msaas/back/fornecedor/
git commit -m "🏗️ test(back): refatora testes de fornecedores ajustando paginacao e contexto autenticado"
```

---

### Passo 2.4 – [Backend Spec 002] Refatorar e Simplificar o Código do Backend + Análise Estática (TDD - Refactor Code)
**Objetivo:** Refatorar o módulo `fornecedor` (e utilitários compartilhados de higienização numérica entre `Comerciante` e `Fornecedor`) mantendo o código enxuto, legível e livre de duplicações.

**Arquivos a revisar/editar:**
- Classes em `msaas/back/src/main/java/com/msaas/back/fornecedor/**` e `shared/**`.

**Checklist de Análise Estática:**
- [ ] Centralizar ou simplificar a função de higienização de dígitos numéricos sem criar abstrações complexas.
- [ ] Verificar se nenhuma alteração quebrou as assinaturas esperadas pelos testes da Spec 001 e Spec 002.

**Comandos Git ao finalizar o passo:**
```powershell
git add msaas/back/src/main/java/com/msaas/back/
git commit -m "🏗️ refactor(back): simplifica regras de higienizacao e servico de fornecedores da spec 002"
```

---

### Passo 2.5 – [Frontend Spec 002] Criar Rotas, Menu Funcional e Telas de Fornecedores (Listagem, Cadastro e Edição)
**Objetivo:** Criar no frontend Angular as páginas e rotas de Gestão de Fornecedores dentro do layout `/aplicacao` e adicionar o item **"Fornecedores"** no menu hambúrguer lateral.

**Requisitos Funcionais e Visuais das Telas:**
1. **Menu Hambúrguer em `/aplicacao` (`aplicacao-layout`):**
   - Adicionar link de navegação para **Fornecedores** (`/aplicacao/fornecedores`).
2. **Tela de Listagem de Fornecedores (`/aplicacao/fornecedores`):**
   - Cabeçalho com título, filtro de status (Ativos / Inativos) e botão de acesso rápido **"+ Novo Fornecedor"** (`/aplicacao/fornecedores/novo`).
   - Tabela/lista limpa em azul e branco exibindo ID, Nome, Contato, WhatsApp, Documento, Categoria e Status.
   - Em cada linha de fornecedor, botões de acesso rápido para:
     - **Editar** (navega para `/aplicacao/fornecedores/:id/editar`)
     - **Deletar / Inativar** (aciona a inativação lógica do fornecedor escolhido)
   - Controles de paginação (Página anterior / Próxima página, indicador de página atual e total de registros).
3. **Tela de Cadastro (`/aplicacao/fornecedores/novo`) e Edição (`/aplicacao/fornecedores/:id/editar`):**
   - Estrutura visual virtualmente idêntica (podendo compartilhar o mesmo componente orientado pelo modo da rota `novo` vs `:id/editar`):
   - **Modo Criação (`/novo`):**
     - Campo **ID do Fornecedor** exibido **vazio** e **não editável** (`readonly` / `disabled`).
     - Campos `nome`, `nomeContato`, `telefoneWhatsapp`, `documento`, `email` e `categoria` habilitados para preenchimento.
   - **Modo Edição (`/:id/editar`):**
     - Campo **ID do Fornecedor** exibido preenchido e **não editável** (`readonly` / `disabled`).
     - Campo **Documento (CPF/CNPJ)** exibido preenchido e **não editável** (`readonly` / `disabled`).
     - Demais campos (`nome`, `nomeContato`, `telefoneWhatsapp`, `email`, `categoria`) habilitados para edição.

**Arquivos a criar/modificar:**
- `msaas/msaas-front/src/app/domains/fornecedor/domain/fornecedor.model.ts`
- `msaas/msaas-front/src/app/domains/fornecedor/domain/pagina.model.ts`
- `msaas/msaas-front/src/app/domains/fornecedor/presentation/fornecedor-lista-page/fornecedor-lista-page.component.ts` (`.html`, `.scss`)
- `msaas/msaas-front/src/app/domains/fornecedor/presentation/fornecedor-form-page/fornecedor-form-page.component.ts` (`.html`, `.scss`)
- `msaas/msaas-front/src/app/domains/aplicacao/presentation/aplicacao-layout/aplicacao-layout.component.html` (inclusão do item Fornecedores no menu hambúrguer)
- `msaas/msaas-front/src/app/app.routes.ts` (rotas filhas de `/aplicacao`: `fornecedores`, `fornecedores/novo`, `fornecedores/:id/editar`)

**Checklist de Análise Estática do Frontend:**
- [ ] Verificar que no formulário de criação o input de `id` está com `[readonly]="true"` / `disabled` e valor vazio.
- [ ] Verificar que no formulário de edição os inputs de `id` e `documento` estão com `[readonly]="true"` / `disabled`.
- [ ] Conferir se as rotas filhas de `/aplicacao` renderizam corretamente dentro do `<router-outlet>` de `AplicacaoLayoutComponent`.

**Comandos Git ao finalizar o passo:**
```powershell
git add msaas/msaas-front/src/
git commit -m "feat(front): cria rotas e telas de listagem, cadastro e edicao de fornecedores da spec 002"
```

---

### Passo 2.6 – [Frontend Spec 002] Implementar Chamadas REST Paginadas e CRUD de Fornecedores
**Objetivo:** Implementar o `FornecedorService` no frontend e conectá-lo às telas de listagem paginada, criação, edição e inativação lógica de fornecedores.

**Arquivos a criar/modificar:**
- `msaas/msaas-front/src/app/domains/fornecedor/application/fornecedor.service.ts`
  - `listarPaginado(page: number, size: number, ativo: boolean, busca?: string)`
  - `buscarPorId(id: number)`
  - `criar(request: FornecedorCreateRequest)`
  - `atualizar(id: number, request: FornecedorUpdateRequest)`
  - `inativar(id: number)`
- Atualização de `fornecedor-lista-page.component.ts` e `fornecedor-form-page.component.ts` para integrar com `FornecedorService`, exibir mensagens de confirmação/erro do backend e recarregar a página atual após inativar um registro.

**Checklist Final de Análise Estática (`static-code-analyst`):**
- [ ] Todos os parâmetros de query (`page`, `size`, `ativo`, `busca`) no `FornecedorService` batem com os `@RequestParam` do `FornecedorController`.
- [ ] O `jwtInterceptor` envia automaticamente o token Bearer nas requisições de `/api/fornecedores/**`.
- [ ] Nenhuma credencial ou arquivo sensível (`application.properties`, `.env`) foi lido ou modificado.

**Comandos Git ao finalizar o passo:**
```powershell
git add msaas/msaas-front/src/
git commit -m "feat(front): implementa chamadas REST paginadas e CRUD de fornecedores da spec 002"
```

---

## 4. Prompt para Iniciar o Desenvolvimento do Plano

Utilize o prompt abaixo para instruir o agente a executar este plano do início ao fim:

```text
/goal Vamos executar o desenvolvimento completo planejado no arquivo @[docs/PLAN.md], implementando sequencialmente a @[docs/specs/spec-revisada-001.md] e a @[docs/specs/spec-revisada-002.md].

Siga rigorosamente todas as diretrizes definidas no @[docs/PLAN.md] e na skill `static-code-analyst`:
1. Política de Execução Zero: NÃO execute nenhum comando de build, compilação, servidor ou execução de testes no terminal (como mvn, npm ou ng). Realize toda a validação exclusivamente por Análise Estática minuciosa de código (inspeção de AST, tipos, imports, contratos de DTOs, regras de negócio e critérios de aceite) antes de cada commit.
2. Segurança de Credenciais: Nunca leia, exiba ou modifique arquivos sensíveis como `application.properties`, `application.yaml` ou `.env`.
3. Siga exatamente a ordem dos passos do @[docs/PLAN.md] (Passo 1.1 até Passo 1.6 para a Spec 001; Passo 2.1 até Passo 2.6 para a Spec 002).
4. Ao concluir cada passo, realize o `git add` e o `git commit` correspondente utilizando exatamente a convenção de símbolos definida no plano:
   - 🔴 antes da mensagem para o commit de criação dos testes que falham propositalmente (Red)
   - 🟢 antes da mensagem para o commit de implementação da spec que faz os testes passarem após análise estática (Green)
   - 🏗️ antes da mensagem para os commits de refatoração de testes e refatoração/simplificação de código (Refactor)
   - Sem símbolo antes da mensagem para os commits do frontend.
5. Ao final de todos os passos, gere um resumo da auditoria estática e os comandos manuais para que eu possa validar a aplicação em runtime.
```
