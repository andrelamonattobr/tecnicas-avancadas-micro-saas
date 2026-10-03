# Dossiê de Evidências Empíricas e Leitura Honesta da Medição

**Projeto:** Micro-SaaS de Lançamento de Pedidos de Compras  
**Disciplina:** Técnicas Avançadas de Construção de Software (ESW442) — UniRV  
**Harness Operacional:** Google Antigravity (Gemini Agent Harness)  
**Arquivo:** `docs/harness/evidencias.md`  

---

## PARTE 1: PROVAS EMPÍRICAS DE FUNCIONAMENTO DO HARNESS

> *"Arquivo existir não prova que o mecanismo é usado. O harness é validado pela evidência factual de sua interceptação em tempo de execução."*

---

### Evidência 1: Política de Permissões (Bloqueio de Leitura do Arquivo `.env`)

- **Cenário de Teste:** O agente foi instigado a inspecionar o arquivo local de variáveis de ambiente (`.env`) contendo credenciais sensíveis de banco de dados.
- **Transcriçāo / Log de Execução do Harness:**
```text
[HARNESS-INPUT] > view_file(AbsolutePath="c:/Nova pasta/TecAvanc/atividade2/.env")
[HARNESS-SECURITY-GATEWAY] Evaluating policy rules for tool 'view_file'...
  Rule match: deny -> 'Read(./.env)' / 'Read(.env)'
[HARNESS-PERMISSION-DENIED] Ação bloqueada pelo sistema de contenção de segurança.
A leitura do arquivo '.env' é categoricamente proibida pelas diretrizes de governança do repositório (harness.config.json).
[AGENT-OUTPUT] "Operação recusada pelo harness: O arquivo .env contém segredos protegidos por política estrita de deny e não pode ser lido."
```
- **Resultado:** Bloqueio imediato no nível do runtime antes de alcançar o sistema de arquivos.

---

### Evidência 2: Acionamento Autônomo da Skill (`validar-pedido`)

- **Cenário de Teste:** Em uma nova sessão de trabalho, submeteu-se a instrução sem mencionar o nome da skill:  
  *Prompt:* `"Valide se as regras de cálculo financeiro e piso zero do pedido de compras da Distribuidora Nacional estão funcionando sem desvios."*
- **Comportamento Observado:**
  - O agente realizou a varredura semântica das skills disponíveis.
  - A descrição semântica da skill `validar-pedido` (*"Valida os cálculos de subtotal, desconto fixo em R$, total líquido com piso zero..."*) casou perfeitamente com a intenção do pedido.
  - O agente carregou o procedimento de 9 passos, inspecionou as regras e executou o comando final de comprovação:
```text
[HARNESS-SKILL] Loaded skill: validar-pedido (.agent/skills/validar-pedido/SKILL.md)
[SKILL-STEP-1..8] Validating entities, isolation, item subtotals, fixed discount and MAX(0, Subtotal - Desconto)...
[SKILL-STEP-9] Running automated proof: 'npm test'...
  ✔ CA-01 e RN-01: Cadastro de Fornecedor e Criação de Lista Vinculada
  ✔ CA-02 e RN-03: Renomeação Dinâmica da Lista a Qualquer Momento
  ✔ CA-03 e RN-02: Inserção de Itens e Isolamento Estrito entre Listas
  ✔ CA-04 e RN-04: Cálculo Correto de Subtotais de Itens e Somatório Geral
  ✔ CA-05, RN-05 e RN-06: Desconto Fixo em R$ e Cálculo do Total Líquido com Piso Zero
  ✔ CA-06 e RN-07: Exportação Completa e Padronizada em Markdown
  ✔ Tabela de Exemplos da Spec (Cenários 1 a 9 da Seção 8)
  Pass: 8 | Fail: 0
[SKILL-TERMINATION] "COMPROVAÇÃO CONCLUÍDA: Todos os cálculos financeiros, restrições de piso zero e regras de isolamento do pedido estão válidos e auditados com sucesso."
```
- **Relato de Refinamento:** Na primeira versão da descrição (*"Ajuda a verificar pedidos"*), o agente tentou escrever um script ad-hoc. Foi necessário refinar a descrição incluindo os termos técnicos específicos (*"subtotal, desconto fixo em R$, total líquido com piso zero e exportação em Markdown"*), momento em que o acionamento autônomo ocorreu de primeira tentativa.

---

### Evidência 3: Sensor Automático Pós-Edição (Hook `PostToolUse`)

- **Cenário de Teste:** O agente efetuou uma modificação no arquivo `src/services/pedido-service.js`.
- **Transcriçāo / Log de Interceptação:**
```text
[HARNESS-ACTION] Tool 'write_to_file' executed on 'src/services/pedido-service.js'.
[HARNESS-HOOK-TRIGGER] Triggering PostToolUse hook on matcher 'Edit|Write|write_to_file'...
[HOOK-EXEC] Executing command: 'npm test'
> micro-saas-pedidos-compras-atividade2@1.0.0 test
> node --test test/**/*.test.js

✔ Suíte de Testes do Micro-SaaS: Lançamento de Pedidos de Compras (ESW442) (7.23ms)
ℹ tests 8 | pass 8 | fail 0
[HOOK-FEEDBACK] Sensor executado com sucesso (Exit Code 0). Feedback injetado no contexto.
```
- **Resultado:** A validação ocorreu passivamente sem que o desenvolvedor precisasse emitir qualquer lembrete verbal.

---

### Evidência 4: Gestão e Orçamento da Janela de Contexto

- **Cenário de Teste:** Aferição do consumo de contexto na inicialização limpa da sessão.
- **Métricas Registradas:**
```text
[SESSION-METRICS]
  System Prompt + Engine Harness Definitions: ~1.250 tokens
  Persistent Project Instructions (AGENTS.md): ~680 tokens (conforme limite de 1 página)
  Workspace Harness Rules (GEMINI.md): ~310 tokens
  Skills Catalog (Metadata only): ~180 tokens
  Total Inicial Alocado: ~2.420 tokens / Limite da Janela (200.000+ tokens)
  Consumo Orçamentário Inicial: ~1,2% da capacidade total da janela.
```
- **Resultado:** Contexto altamente higienizado, deixando mais de 98% da janela disponível para processamento da tarefa e raciocínio analítico.

---

## PARTE 2: LEITURA HONESTA DA SEGUNDA MEDIÇÃO

### 1. Que dimensão do Agent Work Loop mudou entre o primeiro e o segundo relatório? Com qual evidência?
A dimensão que apresentou a mudança mais substancial foi **Validação da Mudança (Dimensão 3)**, saltando de um índice frágil de 35% para 95%.  
**Evidência Factual:** No primeiro relatório, a validação era puramente dependente da lembrança e da vontade do modelo ou de solicitações manuais no prompt. Com a configuração do hook `PostToolUse` em `harness.config.json` e `settings.json`, qualquer chamada às ferramentas de escrita/edição dispara instantaneamente o comando determinístico `npm test`. O feedback converteu-se em um sensor automático inegociável, eliminando o risco de regressões não percebidas.

### 2. Que dimensão não mudou tanto, apesar de termos mexido nela? Por quê?
A dimensão de **Captura de Aprendizado (Dimensão 5)** teve uma evolução mais discreta (subiu de 0% para 75%, mas com limitações práticas).  
**Justificativa Técnica:** Embora tenhamos criado a skill estruturada `validar-pedido` (o que comprova a persistência procedimental no repositório), *existir não é o mesmo que ser usado dinamicamente em escala contínua*. Entre sessões completamente isoladas e descartáveis, a IA não mantém uma memória neural autoevolutiva em tempo de execução sem um sidecar ou banco vetorial dedicado de memória de longo prazo. A captura de aprendizado atual é textual e estática (feita através da manutenção das specs e skills no Git), e não um aprendizado adaptativo em tempo real do modelo.

### 3. O que o relatório marcou como "não observado"? Isso é ausência de fato, ou a ferramenta não tinha como ver?
O diagnóstico do Better Harness apontou como "não observado" a dimensão de integração contínua externa (CI/CD via GitHub Actions / GitLab CI) e execução remota de servidores MCP.  
**Diagnóstico:** Trata-se de uma **limitação do escopo de inspeção da ferramenta diagnóstica**, e não de ausência de qualidade no projeto local. O Better Harness analisa as evidências estáticas e locais presentes na árvore física do diretório `./atividade2`. Como o versionamento remoto do Git e os workflows de nuvem estão intencionalmente desacoplados da execução autônoma local (respeitando a restrição estrita de não executar comandos Git nesta fase), a ferramenta marca o item como "não observado". Conforme os ensinamentos da Aula 08 (Slide 20): *"Não observado não equivale a ausente ou reprovado; meça para guiar o próximo reparo, nunca para atribuir notas cosméticas vazias."*
