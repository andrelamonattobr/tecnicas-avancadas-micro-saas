---
name: static-code-analyst
description: Análise estática de código, revisão e implementação sem execução de código. Detecta erros manualmente por meio de inspeção lógica e de AST, persiste conclusões de auditoria em um arquivo markdown e gera passos exatos de verificação manual para economizar tokens. Use ao revisar ou escrever código onde a execução em runtime for proibida.
---

# Analista e Revisor Estático de Código

## Objetivo
Realizar criação, refatoração e revisão de código com alta precisão estritamente por meio de inspeção manual e raciocínio estático. Evita o consumo excessivo de tokens gerado por ciclos repetitivos de compilação e falha, desabilitando a execução de código, corrigindo bugs iniciais antecipadamente e delegando a validação em runtime ao desenvolvedor.

---

## Restrições Não Negociáveis
1. **Política de Execução Zero**:
   - **NÃO** execute comandos no terminal relacionados a execuçào do código, não rode testes, não inicie compiladores, scripts de build ou linters via shell.
   - Apenas inspeção de arquivos (`read_file`, `list_dir`), edição de arquivos (`write_file`, `edit_file`) e geração de artefatos markdown são permitidas.
   - Você pode realizar os comando de 'git add' e 'git commit' no terminar
2. **Eficiência de Tokens**:
   - Priorize os 3 a 5 problemas de maior gravidade (sintaxe, divergência de tipos, ponteiros nulos/nil dereferences, erros de off-by-one, ausência de tratamento de exceções).
   - Não despeje arquivos inteiros na conversa caso diffs cirúrgicos ou edições pontuais sejam suficientes.
   - Mantenha explicações curtas no chat; detalhe o histórico técnico diretamente no arquivo de conclusões.

---

## Fluxo de Trabalho

### 1. Análise Estática Manual e Triagem de Erros
* Leia os arquivos-alvo e rastreie mentalmente os fluxos de execução.
* Identifique falhas de lógica, violações de sintaxe, tipos incorretos e casos de borda.
* Classifique os erros por severidade (Crítico / Aviso / Otimização). Resolva os bloqueadores antes de ajustes estéticos de estilo.

### 2. Edições Cirúrgicas de Código / Implementação
* Aplique patches mínimos e precisos nos arquivos afetados.
* Preserve as convenções de código, dependências e formatação existentes no projeto.
* Evite abstrações especulativas ou refatorações fora do escopo.
