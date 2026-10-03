# AGENTS.md — Micro-SaaS Pedidos de Compras (Turma A - ESW442)

## Comandos do Projeto
- Instalar dependências: `npm install` (zero dependências externas necessárias)
- Executar testes automatizados: `npm test` (executa suíte via `node:test` e `node:assert`)
- Checagem estática / Linter: `npm run lint`

## Stack Tecnológica e Versões
- Runtime: Node.js >= 24.0.0 (ECMAScript Modules nativo)
- Test Runner: `node:test` embutido no Node.js
- Formato de Dados: JSON / UTF-8 / Markdown BRL (R$)

## Estrutura de Pastas
```text
atividade2/
├── docs/ (specs/ para especificações da verdade e harness/ para relatórios e evidências)
├── src/ (domain/ para entidades puras, services/ para regras de negócio e formatters/ para Markdown)
└── test/ (suíte automatizada de testes cobrindo critérios de aceite e casos de borda)
```

## Onde Ficam as Especificações
- As especificações executáveis residem em `docs/specs/` (ex: `docs/specs/001-lancamento-pedidos-compras.md`).
- A especificação é a única fonte da verdade (*single source of truth*). Nunca modifique comportamento sem atualizar a spec correspondente primeiro.

## Como Você Deve Trabalhar (Os Quatro Princípios de Karpathy)
1. **Pense antes de programar:** Declare suas suposições abertamente. Se uma regra de negócio ou requisito admitir duas interpretações divergentes, pergunte antes de arbitrar. Nunca assuma decisões de negócio não especificadas.
2. **Simplicidade primeiro:** Escreva o mínimo absoluto de código necessário para resolver o problema. Sem abstrações especulativas, sem padrões desnecessários de uso único e sem tratar exceções irreais. Se 40 linhas resolvem com clareza, não escreva 200.
3. **Mudanças cirúrgicas:** Toque exclusivamente nos arquivos pertinentes à tarefa delegada. Mantenha a convenção estilística do código existente. Não realize refatorações cosméticas em trechos funcionais que não fazem parte do escopo.
4. **Execução guiada por objetivo:** Declare a comprovação objetiva de sucesso antes de alterar qualquer código. Execute a suíte de testes (`npm test`) e garanta que todos os critérios de aceite foram comprovados antes de declarar a tarefa concluída.
