# Primeiro Relatório de Diagnóstico do Harness (Medição Inicial)

**Ferramenta de Aferição:** Better Harness (v1.2.0 - QoderAI / Open Source MIT)  
**Projeto:** Micro-SaaS de Lançamento de Pedidos de Compras  
**Etapa:** Pré-Configuração do Harness (Pós-Atividade 1 / Baseline)  
**Data da Medição:** 19/09/2026 (Sábado)  
**Avaliador:** Equipe de Engenharia de Software UniRV  

---

## 1. Sumário Executivo das 5 Dimensões do Agent Work Loop

| Dimensão | Estado Observado | Diagnóstico Sintético |
| :--- | :---: | :--- |
| **1. Entendimento da Tarefa** | **Regular (50%)** | A especificação `docs/specs/001-lancamento-pedidos-compras.md` está bem estruturada, porém a raiz do repositório carece de um arquivo unificado de instruções persistentes (`AGENTS.md`) com comandos reais e regras de postura. |
| **2. Execução Controlada** | **Crítico (20%)** | Inexistência de procedimentos operacionais sob demanda (*Skills*). O agente atua por improvisação contextual sem caminhos padronizados e reprodutíveis. |
| **3. Validação da Mudança** | **Frágil (35%)** | Embora existam testes automatizados em `test/pedido.test.js`, não há sensores passivos (*hooks*) interceptando alterações em tempo real. A execução do teste depende estritamente da lembrança do agente. |
| **4. Entrega Confiável** | **Vulnerável (15%)** | Nenhuma política declarativa de permissões configurada (`allow/ask/deny`). O agente possui amplitude irrestrita de leitura, expondo potenciais segredos locais (`.env`). |
| **5. Captura de Aprendizado** | **Não Observado (0%)** | Não há registro de procedimentos reutilizáveis ou repositório de lições aprendidas entre tarefas. |

---

## 2. Detalhamento das Evidências Coletadas

### 2.1. Dimensão: Entendimento da Tarefa
- **Evidências Positivas:** Presença da especificação funcional com 7 seções, regras de negócio numeradas (RN-01 a RN-07) e critérios de aceite em Gherkin (`CA-01` a `CA-06`).
- **Lacunas Críticas:** Falta um arquivo conciso de 1 página (`AGENTS.md`) orientando como instalar, rodar, testar e se comportar no repositório.

### 2.2. Dimensão: Execução Controlada
- **Evidências:** Não foram encontradas pastas de skills especializadas (`.agent/skills/` ou `.claude/skills/`). Tarefas repetitivas (como auditar cálculos de pedidos com piso zero) são refeitas de maneira ad-hoc.

### 2.3. Dimensão: Validação da Mudança
- **Evidências:** A suíte de testes unitários existe e funciona quando chamada explicitamente (`npm test`), mas inexiste automação pós-ferramenta (*PostToolUse*).

### 2.4. Dimensão: Entrega Confiável
- **Evidências:** Inexistência de arquivos de governança (`harness.config.json` ou `settings.json`). Comandos destrutivos ou leitura de credenciais não são interceptados no runtime.

---

## 3. Achado Crítico Escolhido para Reparo Imediato

> **Achado Selecionado:** *Vulnerabilidade de Governança e Ausência de Sensores Passivos (Dimensões 3 e 4).*  
> **Impacto:** O agente opera no nível máximo de autonomia sem guardrails (sem deny para credenciais sensíveis) e sem sensores automáticos (o feedback depende da boa vontade do modelo).

### Reparo Estrutural Definido:
1. Criação do arquivo `AGENTS.md` (máximo de 1 página) com stack, comandos reais e os 4 princípios de Karpathy.
2. Implementação das políticas declarativas de segurança: $\ge 2$ regras de `allow` (testes e lint), $\ge 1$ regra de `ask` e $\ge 2$ regras de `deny` (com blindagem estrita de `.env` e comandos destrutivos).
3. Configuração de um hook `PostToolUse` nas ferramentas de edição (`Edit|Write`) para executar `npm test` automaticamente a cada alteração.
4. Criação da primeira skill da equipe (`validar-pedido`) com passos verificáveis e critério formal de parada.
5. Inclusão de `.env` no arquivo `.gitignore`.
