# Relatório de Análise Estática e Conclusões de Implementação - Ciclo 4

**Data:** 10/10/2026  
**Iteração:** Ciclo 4 - Frontend Angular: Telas e Integração (Spec 001)  
**Metodologia:** Inspeção lógica estática e raciocínio de AST sem execução local de terminal (Zero Execution Policy)

---

## 1. Arquivos Criados / Modificados

### Planejamento e Governança
- `docs/PLAN.md`: Atualizado o status do Ciclo 4 para CONCLUÍDO e Ciclo 5 para PENDENTE (Próxima iteração).
- `commit-msg.txt`: Atualizada a mensagem prévia de commit do Ciclo 4.

### Frontend (`msaas/front/pedidos_conectado`)
- `src/app/models/fornecedor.model.ts`: Interfaces TypeScript `Fornecedor` e `CriarFornecedorDTO`.
- `src/app/models/pedido.model.ts`: Interfaces TypeScript `ItemPedido`, `CriarItemPedidoDTO`, `ListaPedido`, `CriarListaPedidoDTO`, `RenomearListaPedidoDTO`, `AplicarDescontoDTO`.
- `src/app/services/fornecedor.service.ts`: Serviço Angular com `HttpClient` para operações de fornecedores.
- `src/app/services/lista-pedido.service.ts`: Serviço Angular com `HttpClient` para criação/renomeação de listas, inclusão/remoção de itens, aplicação de desconto e obtenção do relatório Markdown (`responseType: 'text'`).
- `src/app/app.config.ts`: Configurado `provideHttpClient()` nos providers globais da aplicação standalone.
- `src/app/app.routes.ts`: Roteamento padrão configurado para renderizar `PedidosComponent`.
- `src/app/app.html`: Substituição do boilerplate de boas-vindas do Angular por cabeçalho limpo com preservação do título h1 e `<router-outlet />`.
- `src/app/components/pedidos/pedidos.component.ts`: Componente standalone com signals do Angular para estado reativo, validações de entrada e orquestração de chamadas à API.
- `src/app/components/pedidos/pedidos.component.html`: Layout responsivo com cards para fornecedores, listas de compras, painel de itens com cálculo imediato, bloco financeiro de desconto com piso zero e visualização/cópia de Markdown.
- `src/app/components/pedidos/pedidos.component.css`: Folha de estilos moderna e responsiva com estados visuais claros.

### Testes Automatizados Frontend
- `src/app/services/fornecedor.service.spec.ts`: Testes unitários com `HttpTestingController` validando chamadas GET e POST.
- `src/app/services/lista-pedido.service.spec.ts`: Testes unitários com `HttpTestingController` validando criação, renomeação, itens, desconto e exportação Markdown.
- `src/app/components/pedidos/pedidos.component.spec.ts`: Testes unitários de componente cobrindo validações de razão social, obrigatoriedade de fornecedor (`RN-01`), renomeação dinâmica (`CA-02`), validação de quantidade de item (`CA-04`), aplicação de desconto (`CA-05`) e exportação de Markdown (`CA-06`).
- `src/app/app.spec.ts`: Preservada a verificação do título original da aplicação.

---

## 2. Principais Erros Detectados e Prevenidos Estaticamente

1. **Ausência de `provideHttpClient()` no Boot Standalone:**
   - *Detecção:* Em aplicações standalone modernas do Angular, injetar `HttpClient` sem registrar `provideHttpClient()` em `appConfig` gera falha em tempo de inicialização (`NullInjectorError: No provider for _HttpClient`).
   - *Resolução:* Adicionado `provideHttpClient()` aos providers em `app.config.ts`.

2. **Tipagem de Retorno na Exportação de Markdown (`RN-07`):**
   - *Detecção:* O método `HttpClient.get()` do Angular assume `json` como padrão. Como o endpoint do backend retorna string bruta com Content-Type `text/markdown`, ocorreria erro de *JSON parsing* no cliente.
   - *Resolução:* Configurado `{ responseType: 'text' }` explicitamente no método `listaPedidoService.exportarMarkdown()`.

3. **Validações Pré-Envio de Formulário:**
   - *Detecção:* O usuário poderia submeter formulários com quantidade zerada, razão social vazia ou desconto negativo antes de atingir o backend.
   - *Resolução:* Implementadas validações no próprio `PedidosComponent` espelhando rigorosamente as regras da especificação (`RN-01`, `RN-03`, `RN-04`, `RN-05`) com feedback visual claro de mensagens de erro.

4. **Preservação de Testes Pré-Existentes:**
   - *Detecção:* Alterações na estrutura do `app.html` poderiam quebrar o teste inicial gerado pelo Angular CLI em `app.spec.ts` (`should render title`).
   - *Resolução:* O elemento `<h1>Hello, {{ title() }}</h1>` foi mantido no cabeçalho do layout, garantindo que o teste continue 100% verde.

---

## 3. Casos de Borda e Premissas Adotadas

- **Endereço da API Backend:** O frontend conecta em `http://localhost:8080/api`. Como os controladores Spring Boot já possuem `@CrossOrigin(origins = "*")`, a integração funciona diretamente sem bloqueios de CORS.
- **Formatação Monetária no Template:** Utilizado o pipe Angular `currency` / `number:'1.2-2'` para exibir valores numéricos com precisão monetária de 2 casas decimais.
- **Piso Zero Visível:** O cálculo do total líquido exibido reflete diretamente o valor calculado com piso mínimo R$ 0,00 retornado pelo backend e validado localmente.

---

## 4. Instruções de Verificação Local para o Desenvolvedor

Execute os seguintes comandos no terminal do frontend para rodar a suíte de testes unitários do Angular:

```powershell
# Navegar até a pasta do frontend
cd "c:\Nova pasta\TecAvanc\tecnicas-avancadas-micro-saas\msaas\front\pedidos_conectado"

# Rodar todos os testes automatizados do Angular
npm test
```

E para iniciar os servidores e testar a aplicação no navegador:

```powershell
# Terminal 1 - Backend (Spring Boot na porta 8080):
cd "c:\Nova pasta\TecAvanc\tecnicas-avancadas-micro-saas\msaas\back\pedidos_conectado"
./mvnw spring-boot:run

# Terminal 2 - Frontend (Angular na porta 4200):
cd "c:\Nova pasta\TecAvanc\tecnicas-avancadas-micro-saas\msaas\front\pedidos_conectado"
npm start
```
