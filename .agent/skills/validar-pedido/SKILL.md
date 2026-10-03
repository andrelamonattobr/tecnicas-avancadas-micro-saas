---
name: validar-pedido
description: Valida os cálculos de subtotal, desconto fixo em R$, total líquido com piso zero e a exportação em Markdown de uma lista de pedido de compras. Use quando o usuário solicitar a validação de regras de negócio de pedidos, conferência financeira ou verificação de conformidade de uma lista de compras.
---

# Procedimento Operacional: Validação e Auditoria de Lista de Pedido de Compra

Execute este procedimento rigorosamente passo a passo sempre que uma lista de compras ou pedido for submetido a auditoria de conformidade:

1. **Identificação da Entidade Raiz:** Localize o identificador da lista de pedido (`listaId`) e confirme a existência da `Empresa Fornecedora` correspondente, verificando o preenchimento de sua razão social.
2. **Inspeção de Itens e Isolamento:** Verifique os itens vinculados exclusivamente à lista. Certifique-se de que não há compartilhamento de instâncias com outras listas de compras.
3. **Checagem de Parâmetros dos Produtos:** Para cada item, valide:
   - `quantidade`: deve ser estritamente superior a zero ($> 0.00$).
   - `precoUnitario`: deve ser um valor monetário não-negativo ($\ge 0.00$).
4. **Conferência Determinística de Subtotais:** Calcule o subtotal de cada item multiplicando quantidade por preço unitário:
   $$\text{Subtotal}_{\text{Item}} = \text{quantidade} \times \text{precoUnitario}$$
5. **Consolidação do Subtotal Geral:** Realize o somatório aritmético de todos os subtotais dos itens da lista.
6. **Auditoria de Desconto Global:** Inspecione o campo `descontoReais`. Certifique-se de que o valor é monetário fixo em R$ ($\ge 0.00$). Rejeite qualquer desconto percentual ou negativo.
7. **Aplicação da Função de Piso Zero:** Calcule o valor total final líquido a pagar aplicando a função matemática:
   $$\text{Total Líquido} = \max(0, \, \text{Subtotal Geral} - \text{descontoReais})$$
   Comprove que, se o desconto for maior ou igual ao subtotal, o total líquido resultante é exatamente R$ 0,00.
8. **Conferência da Exportação em Markdown:** Verifique se o documento Markdown contém:
   - Título e identificadores do pedido.
   - Dados completos da empresa fornecedora.
   - Tabela de itens com colunas discriminadas.
   - Resumo financeiro com subtotal bruto, desconto em R$ e total líquido final.
9. **Passo Final de Parada e Comprovação Empírica:** Execute o comando de testes automatizados do projeto:
   ```bash
   npm test
   ```
   - Caso qualquer teste resulte em falha, interrompa imediatamente a execução e reporte a não-conformidade.
   - Caso todos os testes passem com 100% de sucesso, emita o relatório de auditoria e finalize com a mensagem de comprovação:
     > *"COMPROVAÇÃO CONCLUÍDA: Todos os cálculos financeiros, restrições de piso zero e regras de isolamento do pedido estão válidos e auditados com sucesso."*
   - Pare a execução. Não execute refatorações ou geração de código adicional após este passo.
