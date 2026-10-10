# Relatório de Análise Estática e Conclusões de Implementação - Ciclo 3

**Data:** 10/10/2026  
**Iteração:** Ciclo 3 - Exportação Canônica Formatada em Markdown (Spec 001)  
**Metodologia:** Inspeção lógica estática e raciocínio de AST sem execução local de terminal (Zero Execution Policy)

---

## 1. Arquivos Criados / Modificados

### Planejamento e Governança
- `docs/PLAN.md`: Atualizado o status do Ciclo 3 para CONCLUÍDO e Ciclo 4 para PENDENTE (Próxima iteração).
- `commit-msg.txt`: Atualizada a mensagem prévia de commit do Ciclo 3.

### Backend (`msaas/back/pedidos_conectado`)
- `service/ExportacaoMarkdownService.java`: Serviço determinístico de geração de relatórios em Markdown conforme `RN-07` e `CA-06`, formatando valores em moeda brasileira (`R$ X,XX`), dados do fornecedor, tabela de itens e sumário financeiro com piso zero.
- `service/ListaPedidoService.java`: Injeção de `ExportacaoMarkdownService` e adição do método `exportarMarkdown()`, consolidando lista, fornecedor vinculado, itens persistidos e totais calculados.
- `web/ListaPedidoController.java`: Exposição do endpoint `GET /api/listas/{id}/exportar-markdown` retornando `text/markdown; charset=UTF-8`.

### Testes Automatizados Backend
- `src/test/java/com/maas/pedidos_conectado/service/ExportacaoMarkdownServiceTest.java`:
  - `CA-06`: Validação completa do cenário Gherkin (fornecedor "Alimentos do Campo S.A.", item de R$ 80,00, desconto de R$ 10,00 e total líquido R$ 70,00).
  - `RN-07`: Validação estrutural de cabeçalho, tabela com todas as colunas obrigatórias e resumo financeiro discriminado.
  - Cenário de lista vazia (sem itens).
  - Cenário de múltiplos itens com numeração de linhas sequencial.
  - Cenário de desconto superior ao subtotal com piso zero R$ 0,00.
- `src/test/java/com/maas/pedidos_conectado/service/ListaPedidoServiceTest.java`: Teste de integração do serviço validando busca das entidades e delegação para o gerador de Markdown.
- `src/test/java/com/maas/pedidos_conectado/web/ListaPedidoControllerTest.java`: Teste de camada web validando status HTTP 200 e Content-Type compatível com `text/markdown`.

---

## 2. Principais Erros Detectados e Prevenidos Estaticamente

1. **Incompatibilidade na Formatação Regional de Moeda (`RN-07` / `CA-06`):**
   - *Detecção:* A máquina do usuário ou o container pode ter como locale padrão `en-US`, o que faria `NumberFormat` ou `String.format` usar ponto como separador decimal (`$80.00` ou `R$ 80.00`), violando a asserção exata do critério `CA-06` (`R$ 80,00`).
   - *Resolução:* Implementado `formatarMoeda()` explicitamente com `DecimalFormatSymbols(Locale.forLanguageTag("pt-BR"))`, forçando vírgula como separador decimal e ponto como agrupador de milhares de forma 100% determinística.

2. **Truncamento de Itens Vazios:**
   - *Detecção:* Se a lista não possuir itens cadastrados no momento da exportação, uma iteração vazia deixaria a tabela Markdown quebrada sem linhas de corpo.
   - *Resolução:* Adicionada linha padrão de fallback `| - | *(Nenhum item adicionado)* | - | - | - |` quando a lista de itens for nula ou vazia.

3. **Garantia de Content-Type nos Clientes HTTP:**
   - *Detecção:* Endpoint retornando `text/markdown` poderia falhar em clientes que esperam `text/plain` ou em requisições de teste sem header Accept explícito.
   - *Resolução:* Configurado `produces = {MediaType.TEXT_PLAIN_VALUE, "text/markdown"}` com codificação explícita `UTF-8` no controlador.

---

## 3. Casos de Borda e Premissas Adotadas

- **Fuso Horário Canônico UTC:** A data de criação do pedido é formatada em padrão `dd/MM/yyyy HH:mm:ss (UTC)`, garantindo reprodutibilidade idêntica em qualquer ambiente onde os testes forem executados.
- **Campos Opcionais do Fornecedor:** Caso o fornecedor não possua CNPJ ou Contato cadastrados, o relatório exporta de forma amigável `"Não informado"` em vez de deixar campos vazios ou `null`.
- **Precisão Numérica de 2 Casas Decimais:** Todos os cálculos e valores monetários exibidos no Markdown respeitam o piso mínimo estrito de R$ 0,00 e o arredondamento `HALF_UP`.

---

## 4. Instruções de Verificação Local para o Desenvolvedor

Execute os seguintes comandos no terminal do backend para compilar e rodar toda a suíte de testes (Ciclos 1, 2 e 3):

```powershell
# Navegar até a pasta do backend
cd "c:\Nova pasta\TecAvanc\tecnicas-avancadas-micro-saas\msaas\back\pedidos_conectado"

# Rodar todos os testes automatizados (CA-01 até CA-06)
./mvnw test

# Ou rodar especificamente os testes de exportação em Markdown:
./mvnw test -Dtest=ExportacaoMarkdownServiceTest,ListaPedidoServiceTest,ListaPedidoControllerTest
```
