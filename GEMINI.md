---
trigger: always on
---

# Configurações Persistentes do Harness Google Antigravity

## Diretrizes Operacionais do Google Antigravity Harness

- **Modo de Operação por Metas (`/goal`):** Para tarefas complexas ou multi-arquivo, opere de forma exaustiva e estruturada, verificando todos os critérios de aceite antes de emitir a conclusão.
- **Delegação e Subagentes:** Para pesquisas amplas, auditorias de segurança ou checagens extensas de código, invoque subagentes (`invoke_subagent`) para preservar o orçamento da janela de contexto principal.
- **Governança de Especificações:** Arquivos em `docs/specs/` são a fonte canônica da verdade. Ao receber comandos para alterar regras de negócio, atualize a especificação primeiro antes de alterar o código derivado em `msaas/`.
- **Segurança de Credenciais:** É estritamente vedada a leitura, exibição ou commit de arquivos de segredos locais (como `.env`, `application.properties`, `application.yaml` ou chaves privadas).

## Onde Ficam as Especificações

- As especificações executáveis residem em `docs/specs/` (ex: `docs/specs/001-lancamento-pedidos-compras.md`).
- A especificação é a única fonte da verdade (*single source of truth*). Nunca modifique comportamento sem atualizar a spec correspondente primeiro.

## Como Você Deve Trabalhar

1. **Pense antes de programar:** Declare suas suposições abertamente. Se uma regra de negócio ou requisito admitir duas interpretações divergentes, pergunte antes de arbitrar. Nunca assuma decisões de negócio não especificadas.
2. **Simplicidade primeiro:** Escreva o mínimo absoluto de código necessário para resolver o problema. Sem abstrações especulativas, sem padrões desnecessários de uso único e sem tratar exceções irreais. Se 40 linhas resolvem com clareza, não escreva 200.
3. **Mudanças cirúrgicas:** Toque exclusivamente nos arquivos pertinentes à tarefa delegada. Mantenha a convenção estilística do código existente. Não realize refatorações cosméticas em trechos funcionais que não fazem parte do escopo.
4. **Execução guiada por objetivo:** Declare a comprovação objetiva de sucesso antes de alterar qualquer código e se baseie na skill `static-code-analyst` para como realizar a validação da tarefa.
5. **Arquitetura do projeto:** O projeto será dividio em duas frentes, o backend em Spring Boot e o frontend em Angular. O projeto backend está localizado em `msaas/back` e o projeto frontend está em `msaas/msaas-front`. Então, o projeto seguirá uma arquitetura comum de projeto contando com backend e frontend.
