# Segundo Relatório de Diagnóstico do Harness (Medição Pós-Configuração)

**Ferramenta de Aferição:** Better Harness (v1.2.0 - QoderAI / Open Source MIT)  
**Projeto:** Micro-SaaS de Lançamento de Pedidos de Compras  
**Etapa:** Pós-Configuração do Harness e Implementação de Salvaguardas  
**Data da Medição:** 22/09/2026 (Terça-feira) / Homologação 03/10/2026  
**Avaliador:** Equipe de Engenharia de Software UniRV  

---

## 1. Matriz Comparativa de Evolução das 5 Dimensões

| Dimensão | Medição Inicial | Medição Final | Saldo / Status | Evidência Comprobatória |
| :--- | :---: | :---: | :---: | :--- |
| **1. Entendimento da Tarefa** | 50% | **95%** | **+45%** (Excelente) | Presença do `AGENTS.md` (1 página) e `GEMINI.md`, fornecendo comandos exatos de instalação, teste e os 4 princípios operacionais de Karpathy. |
| **2. Execução Controlada** | 20% | **90%** | **+70%** (Excelente) | Implementação da skill `validar-pedido` (`.agent/skills/` e `.claude/skills/`) com procedimento de 9 passos e critério imperativo de parada. |
| **3. Validação da Mudança** | 35% | **95%** | **+60%** (Excelente) | Sensor automático (`PostToolUse`) ativo em `harness.config.json` e `settings.json`, disparando a suíte de testes a cada alteração de arquivo. |
| **4. Entrega Confiável** | 15% | **90%** | **+75%** (Excelente) | Governança declarativa de permissões (`allow`, `ask`, `deny`), com blindagem total de `.env` e `.gitignore` ativo contra vazamentos. |
| **5. Captura de Aprendizado** | 0% | **75%** | **+75%** (Bom) | Procedimento de conferência de pedidos com piso zero persistido como skill reutilizável no repositório. |

---

## 2. Análise Detalhada dos Mecanismos Implementados

### 2.1. Feedforward Estruturado (Instruções e Skills)
- O arquivo `AGENTS.md` reduziu a taxa de alucinação de comandos para zero, fornecendo os comandos reais de `npm test` e `npm run lint`.
- A integração com `GEMINI.md` formalizou o modelo de trabalho com subagentes e guardrails de especificações.
- A skill `validar-pedido` proveu um caminho repetível e auditável para a conferência matemática dos pedidos de compra sem improvisos.

### 2.2. Feedback Automático (Hooks e Sensores)
- O hook de `PostToolUse` transformou o critério de aceite em sensor em tempo real: o agente não consegue concluir uma rodada de edição com regressão de código, pois a falha é imediatamente injetada de volta na sua janela de contexto.

### 2.3. Blindagem de Segurança e Contenção
- A política de `deny` em `Read(./.env)` e `Read(.env)` garantiu que credenciais e variáveis sensíveis sejam sumariamente bloqueadas no nível do harness, impedindo que o modelo consuma dados privados mesmo sob engenharia de prompt adversária.

---

## 3. Conclusão da Comparação

A infraestrutura de governança (*Harness Engineering*) evoluiu de um estado vulnerável e puramente manual para um ciclo fechado de controle (*Feedforward + Feedback*). O projeto micro-SaaS encontra-se plenamente capacitado para operar em níveis elevados de autonomia sem risco de regressão silenciosa ou violação de segurança.
