# Plano de Implementação da Feature 001: Lançamento de Pedidos de Compras

**Referência Canônica da Spec:** `docs/specs/001-lancamento-pedidos-compras.md`  
**Arquitetura:** Backend Spring Boot (`msaas/back/pedidos_conectado`) + Frontend Angular (`msaas/front/pedidos_conectado`)  
**Estratégia de Desenvolvimento:** Construção gradual em ciclos determinísticos com validação estática rigorosa e testes automatizados.

---

## 1. Visão Geral e Critérios de Ordenação

A ordem de execução foi estabelecida a partir da cadeia de dependências de domínio e valor de negócio:
1. **Fundação e Vínculo de Entidades-Mãe:** Uma lista de compras depende compulsoriamente da existência de um fornecedor (`RN-01`). Portanto, o cadastro de fornecedor e a criação/renomeação de lista são os pré-requisitos absolutos.
2. **Itens e Motor de Cálculo Financeiro:** Os cálculos de subtotais (`RN-04`), desconto monetário (`RN-05`) e piso zero (`RN-06`) dependem dos itens associados à lista (`RN-02`).
3. **Exportação Canônica em Markdown:** A exportação (`RN-07`) consome todos os dados consolidados da lista, fornecedor e itens.
4. **Interface do Usuário (Frontend Angular):** Consome os contratos de API REST já estabilizados e testados no backend.
5. **Homologação e Revisão Final:** Validação integrada ponta a ponta dos critérios `CA-01` a `CA-06`.

---

## 2. Divisão dos Ciclos de Desenvolvimento

```mermaid
flowchart TD
    C1["Ciclo 1: Fundação Backend (Fornecedor & Lista) [CA-01, CA-02]"] --> C2["Ciclo 2: Itens e Cálculos Financeiros [CA-03, CA-04, CA-05]"]
    C2 --> C3["Ciclo 3: Exportação em Markdown [CA-06, RN-07]"]
    C3 --> C4["Ciclo 4: Frontend Angular (Interface & Serviços)"]
    C4 --> C5["Ciclo 5: Homologação e Revisão End-to-End"]
```

---

### Ciclo 1: Fundação Backend - Fornecedor e Gestão de Listas de Pedidos
- **Status:** CONCLUÍDO NESTA ITERAÇÃO
- **Critérios de Aceite Atendidos:**
  - `CA-01`: Cadastro de Fornecedor e Criação da Lista vinculada com status financeiro zerado inicial.
  - `CA-02`: Renomeação Dinâmica da Lista a qualquer instante sem impacto nos vínculos.
- **Regras de Negócio Implementadas:**
  - `RN-01`: Vínculo Mandatório de Empresa Fornecedora existente.
  - `RN-03`: Renomeação Irrestrita de Título (3 a 100 caracteres).
- **Entregas Técnicas:**
  - Entidade de Domínio `Fornecedor` (`id`, `razaoSocial`, `nomeFantasia`, `cnpj`, `contato`) com validações de tamanho e normalização de texto.
  - Entidade de Domínio `ListaPedido` (`id`, `empresaId`, `titulo`, `dataCriacao`, `descontoReais`).
  - Repositórios Spring Data JPA: `FornecedorRepository`, `ListaPedidoRepository`.
  - Serviços de Aplicação: `FornecedorService`, `ListaPedidoService`.
  - DTOs de Entrada e Saída com campos monetários e status financeiro inicial determinístico.
  - Controladores REST: `FornecedorController` (`/api/fornecedores`) e `ListaPedidoController` (`/api/listas`).
  - Tratamento Global de Exceções (`GlobalExceptionHandler`) com respostas padronizadas HTTP 400/404.
  - Suporte a CORS para integração com o frontend Angular.
  - Testes Automatizados cobrindo `CA-01`, `CA-02`, e cenários de erro (fornecedor inexistente, títulos inválidos, razão social inválida).

---

### Ciclo 2: Gestão de Itens, Isolamento e Motor de Cálculo Financeiro
- **Status:** CONCLUÍDO NESTA ITERAÇÃO
- **Critérios de Aceite Atendidos:**
  - `CA-03`: Inserção de Item e Isolamento Estrito por Lista.
  - `CA-04`: Cálculo Correto do Subtotal dos Itens e da Lista.
  - `CA-05`: Aplicação de Desconto Fixo com função $\max(0, \text{Subtotal} - \text{Desconto})$.
- **Regras de Negócio Implementadas:**
  - `RN-02`: Isolamento Estrito de Itens (sem catálogo mestre global).
  - `RN-04`: Cálculo de Subtotais ($\text{Qtd} \times \text{Preço Unitário}$) e somatório geral.
  - `RN-05`: Desconto Fixo em Reais ($\ge 0$, valor negativo rejeitado).
  - `RN-06`: Total Líquido Não-Negativo com Piso Zero.
  - Tabela da Seção 8: Cenários 1 a 9 da especificação.
- **Entregas Técnicas:**
  - Entidade `ItemPedido` vinculada à `ListaPedido` com validações de domínio (descrição, quantidade $> 0$, preço unitário $\ge 0$).
  - Repositório `ItemPedidoRepository` com busca e deleção por `listaId`.
  - Métodos em `ListaPedidoService` para adição, listagem e remoção de itens com isolamento estrito, além de aplicação de desconto monetário fixo e cálculo dinâmico de total líquido com piso zero.
  - Endpoints REST em `ListaPedidoController` para gestão de itens e desconto.
  - Suíte completa de testes unitários e de camada web cobrindo `CA-03`, `CA-04`, `CA-05` e todos os 9 cenários da Seção 8.

---

### Ciclo 3: Exportação Canônica em Markdown
- **Status:** CONCLUÍDO NESTA ITERAÇÃO
- **Critérios de Aceite Atendidos:**
  - `CA-06`: Exportação Completa Formatada em Markdown.
- **Regras de Negócio Implementadas:**
  - `RN-07`: Estrutura canônica de cabeçalho (título e data de criação), informações da empresa fornecedora (razão social, CNPJ e contato), tabela com colunas (`Item`, `Descrição`, `Qtd`, `Preço Unitário (R$)`, `Subtotal (R$)`) e resumo financeiro discriminado (`Subtotal dos Itens`, `Desconto Aplicado (R$)`, `Total Final Líquido (R$)`).
- **Entregas Técnicas:**
  - Serviço `ExportacaoMarkdownService` com formatação monetária em padrão brasileiro (`R$ X,XX`) e determinação de linhas da tabela.
  - Método `exportarMarkdown()` em `ListaPedidoService`.
  - Endpoint REST `GET /api/listas/{id}/exportar-markdown` com Content-Type `text/markdown; charset=UTF-8`.
  - Testes automatizados unitários e de controlador validando o critério `CA-06`, `RN-07`, listas vazias e listas com múltiplos itens.

---

### Ciclo 4: Frontend Angular - Telas e Integração
- **Status:** CONCLUÍDO NESTA ITERAÇÃO
- **Critérios de Aceite Atendidos:**
  - `CA-01` a `CA-06`: Interface gráfica interativa para o operador de compras cobrindo todos os critérios da spec.
- **Entregas Técnicas:**
  - Modelos TypeScript: `Fornecedor`, `CriarFornecedorDTO`, `ListaPedido`, `CriarListaPedidoDTO`, `RenomearListaPedidoDTO`, `ItemPedido`, `CriarItemPedidoDTO`, `AplicarDescontoDTO`.
  - Serviços Angular: `FornecedorService` e `ListaPedidoService` integrados à API Spring Boot via `HttpClient`.
  - Componente Standalone: `PedidosComponent` (`src/app/components/pedidos/pedidos.component.*`) com telas de Fornecedor, Listas de Pedidos, Lançamento de Itens, Resumo Financeiro reativo com piso zero e Exportação Markdown.
  - Configuração de roteamento em `app.routes.ts` e injeção de `provideHttpClient()` em `app.config.ts`.
  - Suíte de testes unitários Angular para serviços e componentes (`fornecedor.service.spec.ts`, `lista-pedido.service.spec.ts`, `pedidos.component.spec.ts`, `app.spec.ts`).

---

### Ciclo 5: Homologação Integrada e Revisão Final
- **Status:** CONCLUÍDO NESTA ITERAÇÃO
- **Entregas Técnicas:**
  - Teste de homologação end-to-end completo em `FluxoCompletoE2ETest.java` validando em cadeia transacional real:
    - Criação de fornecedor e criação de lista vinculada com status financeiro inicial zerado (`CA-01`).
    - Renomeação dinâmica de título preservando IDs e fornecedores (`CA-02`).
    - Isolamento absoluto de itens entre listas distintas (`CA-03`).
    - Cálculo determinístico de subtotais de itens e subtotal geral (`CA-04`).
    - Aplicação de desconto monetário fixo e respeito estrito ao piso zero ($\max(0, \text{Subtotal} - \text{Desconto})$) (`CA-05`).
    - Exportação canônica estruturada em Markdown com dados de cabeçalho, fornecedor, tabela e sumário financeiro (`CA-06`).
  - Auditoria de Anti-Escopo (Seção 2.2):
    1. Sem catálogo global de produtos: validado.
    2. Sem reaproveitamento de itens entre listas: validado.
    3. Sem desconto percentual: validado (apenas valor fixo em R$).
    4. Sem saldo negativo: validado (piso de R$ 0,00 garantido).
    5. Sem complexidades fora do escopo (estoque, fiscal, pagamentos, logins externos): validado.
  - Especificação 001 100% implementada e coberta por suítes de testes automatizados no backend e no frontend.
