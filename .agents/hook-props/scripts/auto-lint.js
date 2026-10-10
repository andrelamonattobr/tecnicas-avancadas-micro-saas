#!/usr/bin/env node
const fs = require('fs');
const { execSync } = require('child_process');

// ============================================================================
// CONTROLE DE BYPASS / MOCK
// Altere para true para simular a execução sem rodar o linter de verdade.
// Alternativamente, crie um arquivo vazio '.skip-lint' na raiz do projeto.
// ============================================================================
const MOCK_MODE = true;

function exitSuccess() {
  // O contrato de PostToolUse do Antigravity exige '{}' no stdout
  console.log('{}');
  process.exit(0);
}

try {
  // 1. Checagem de Bypass: se ativo, finge execução com sucesso instantâneo
  if (MOCK_MODE || fs.existsSync('.skip-lint') || process.env.SKIP_LINT === 'true') {
    exitSuccess();
  }

  // 2. Leitura da entrada (stdin) fornecida pelo Antigravity
  const rawInput = fs.readFileSync(0, 'utf-8');
  if (!rawInput.trim()) {
    exitSuccess();
  }

  const payload = JSON.parse(rawInput);
  const args = payload.toolCall?.args || {};
  const targetFile = args.TargetFile;

  // 3. Validação de extensão (.js, .ts, .jsx, .tsx)
  if (targetFile && /\.(js|ts|jsx|tsx)$/i.test(targetFile)) {
    if (fs.existsSync(targetFile)) {
      // Executa o linter/formatter apenas no arquivo afetado para economizar recursos.
      // stdio: 'ignore' impede que logs externos quebrem o JSON de saída do hook.
      execSync(`npx prettier --write "${targetFile}"`, { stdio: 'ignore' });
    }
  }
} catch (error) {
  // Silencia erros para não quebrar o fluxo do agente
} finally {
  exitSuccess();
}