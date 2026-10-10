# Micro-SaaS: Pedidos Conectado

Plataforma ágil e determinística para cadastro de empresas fornecedoras, criação e renomeação dinâmica de listas de pedidos de compra, lançamento de itens com subtotais, aplicação de desconto financeiro fixo com piso zero e exportação canônica padronizada em Markdown.

Projeto desenvolvido com base na **Feature 001** (`docs/specs/001-lancamento-pedidos-compras.md`) e plano gradual em (`docs/PLAN.md`).

---

## 🛠️ Pré-requisitos e Ferramentas Necessárias

Para rodar este projeto localmente, certifique-se de ter instalado em sua máquina:

1. **Java JDK 25:**
   - Versão exigida pelo Spring Boot do projeto (`<java.version>25</java.version>`).
   - Verifique com: `java -version`
2. **Node.js (versão 24.x ou superior):**
   - Ambiente de execução para o Angular 22.
   - Verifique com: `node -v`
3. **npm (versão 11.x ou compativel):**
   - Gerenciador de pacotes do ecossistema Node (`npm@11.13.0`).
   - Verifique com: `npm -v`
4. **Git:**
   - Para clonar e gerenciar o versionamento do projeto.
   - Verifique com: `git --version`
5. *(Opcional)* **Maven:**
   - O projeto backend já inclui o wrapper Maven (`mvnw` / `mvnw.cmd`), não sendo estritamente obrigatório ter o Maven instalado globalmente no sistema operacional.

---

## 📁 Estrutura do Repositório

```text
tecnicas-avancadas-micro-saas/
├── docs/
│   ├── PLAN.md                               # Plano gradual de implementação em 5 ciclos
│   └── specs/001-lancamento-pedidos-compras.md # Fonte canônica da verdade (Spec 001)
├── msaas/
│   ├── back/pedidos_conectado/               # Backend Java 25 + Spring Boot + JPA + H2
│   └── front/pedidos_conectado/              # Frontend Angular 22 + TypeScript + CSS
├── commit-msg.txt                            # Mensagem descritiva do commit atual
└── review_conclusions.md                     # Auditoria estática e conformidade dos ciclos
```

---

## 🚀 Como Executar o Projeto (A partir da raiz do repositório)

Ao clonar o repositório, abra dois terminais (um para o backend e outro para o frontend). Todas as instruções abaixo consideram que o seu diretório atual é a raiz da pasta clonada (`tecnicas-avancadas-micro-saas`).

### 1. Rodando o Backend (Spring Boot)

O backend roda por padrão na porta **8080** (`http://localhost:8080`).

#### No Windows (PowerShell):
```powershell
# 1. Navegar até a pasta do backend e iniciar a aplicação
cd msaas\back\pedidos_conectado
.\mvnw spring-boot:run
```

#### No Linux / macOS (Bash):
```bash
# 1. Conceder permissão de execução ao wrapper (caso necessário na primeira vez)
chmod +x msaas/back/pedidos_conectado/mvnw

# 2. Navegar até a pasta do backend e iniciar a aplicação
cd msaas/back/pedidos_conectado
./mvnw spring-boot:run
```

#### Para rodar os testes automatizados do Backend:
```bash
cd msaas/back/pedidos_conectado
./mvnw test
```

---

### 2. Rodando o Frontend (Angular)

O frontend roda por padrão na porta **4200** (`http://localhost:4200`).

#### No Windows / Linux / macOS:
```bash
# 1. Navegar até a pasta do frontend
cd msaas/front/pedidos_conectado

# 2. Instalar as dependências do projeto (obrigatório na primeira execução após clonar)
npm install

# 3. Iniciar o servidor de desenvolvimento do Angular
npm start
```

Após iniciar, acesse `http://localhost:4200` no seu navegador.

#### Para rodar os testes automatizados do Frontend:
```bash
cd msaas/front/pedidos_conectado
npm test
```

---

## 📡 Endpoints Principais da API REST (Backend)

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `POST` | `/api/fornecedores` | Cadastra uma nova empresa fornecedora |
| `GET` | `/api/fornecedores` | Lista todas as empresas fornecedoras cadastradas |
| `GET` | `/api/fornecedores/{id}` | Consulta detalhes de um fornecedor por ID |
| `POST` | `/api/listas` | Cria lista de pedido de compra vinculada a fornecedor (`RN-01`, `CA-01`) |
| `GET` | `/api/listas` | Lista pedidos de compra (com filtro opcional `?empresaId=...`) |
| `GET` | `/api/listas/{id}` | Consulta detalhes da lista com totais e status financeiro |
| `PATCH`| `/api/listas/{id}/titulo` | Renomeia o título da lista dinamicamente (`RN-03`, `CA-02`) |
| `POST` | `/api/listas/{id}/itens` | Adiciona item exclusivo à lista de pedido (`RN-02`, `CA-03`) |
| `GET` | `/api/listas/{id}/itens` | Lista os itens da lista com subtotais calculados (`RN-04`, `CA-04`) |
| `DELETE`| `/api/listas/{id}/itens/{itemId}` | Remove um item exclusivo da lista |
| `PUT` | `/api/listas/{id}/desconto` | Aplica desconto monetário fixo com piso zero (`RN-05`, `RN-06`, `CA-05`) |
| `GET` | `/api/listas/{id}/exportar-markdown`| Exporta o relatório completo formatado em Markdown (`RN-07`, `CA-06`) |
