# GEMINI.md — Configurações Persistentes do Harness Google Antigravity

@AGENTS.md

## Diretrizes Operacionais do Google Antigravity Harness
- **Importação de Contexto:** Este arquivo especializa a operação do agente autônomo no harness Google Antigravity sobre a base comum declarada em `AGENTS.md`.
- **Modo de Operação por Metas (`/goal`):** Para tarefas complexas ou multi-arquivo, opere de forma exaustiva e estruturada, verificando todos os critérios de aceite antes de emitir a conclusão.
- **Delegação e Subagentes:** Para pesquisas amplas, auditorias de segurança ou checagens extensas de código, invoque subagentes (`invoke_subagent`) para preservar o orçamento da janela de contexto principal.
- **Governança de Especificações:** Arquivos em `docs/specs/` são a fonte canônica da verdade. Ao receber comandos para alterar regras de negócio, atualize a especificação primeiro antes de alterar o código derivado em `src/`.
- **Sensores e Validação Contínua:** Antes de finalizar qualquer intervenção, execute sempre `npm test` e `npm run lint` para comprovar que nenhuma regressão foi introduzida.
- **Segurança de Credenciais:** É estritamente vedada a leitura, exibição ou commit de arquivos de segredos locais (como `.env` ou chaves privadas).
