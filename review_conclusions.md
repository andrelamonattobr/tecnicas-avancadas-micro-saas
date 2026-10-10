# Relatório de Análise Estática e Conclusões de Implementação - Ciclo 2

**Data:** 10/10/2026  
**Iteração:** Ciclo 2 - Gestão de Itens e Motor de Cálculo Financeiro (Spec 001)  
**Metodologia:** Inspeção lógica estática e raciocínio de AST sem execução local de terminal (Zero Execution Policy)

---

## 1. Arquivos Criados / Modificados

### Planejamento e Governança
- `docs/PLAN.md`: Atualizado o status do Ciclo 2 para CONCLUÍDO e Ciclo 3 para PENDENTE (Próxima iteração).
- `commit-msg.txt`: Atualizada a mensagem descritiva prévia de commit para o Ciclo 2.

### Backend (`msaas/back/pedidos_conectado`)
- `domain/ItemPedido.java`: Entidade de domínio JPA para produtos/itens do pedido, contendo cálculo determinístico de subtotal (`getSubtotal()`) e validações estritas de domínio (`quantidade > 0`, `precoUnitario >= 0`, `descricao` de 2 a 120 caracteres).
- `repository/ItemPedidoRepository.java`: Interface Spring Data JPA com métodos de busca e deleção filtrados por `listaId`.
- `web/dto/CriarItemPedidoDTO.java`: Payload para criação de itens.
- `web/dto/ItemPedidoResponseDTO.java`: DTO de resposta do item com subtotal calculado.
- `web/dto/AplicarDescontoDTO.java`: DTO para atualização do desconto monetário em R$.
- `web/dto/ListaPedidoResponseDTO.java`: Atualizado para conter a lista detalhada de itens (`List<ItemPedidoResponseDTO>`), mantendo compatibilidade retroativa.
- `service/ListaPedidoService.java`: Implementação das regras `RN-02`, `RN-04`, `RN-05` e `RN-06`:
  - `adicionarItem()`: Valida existência da lista e persiste item exclusivo.
  - `listarItens()`: Garante isolamento estrito por `listaId`.
  - `removerItem()`: Valida integridade referencial e vinculação exclusiva do item antes da exclusão.
  - `aplicarDesconto()`: Rejeita valores negativos (`RN-05`) e persiste o desconto.
  - `calcularSubtotalGeral()` e `calcularTotalLiquido()`: Implementam determinismo numérico com piso zero estrito ($\max(0, \text{Subtotal} - \text{Desconto})$).
- `web/ListaPedidoController.java`: Endpoints REST adicionados:
  - `POST /api/listas/{id}/itens` (201 Created)
  - `GET /api/listas/{id}/itens` (200 OK)
  - `DELETE /api/listas/{id}/itens/{itemId}` (204 No Content)
  - `PUT /api/listas/{id}/desconto` (200 OK)

### Testes Automatizados Backend
- `src/test/java/com/maas/pedidos_conectado/domain/ItemPedidoTest.java`: Testes unitários de domínio cobrindo cálculo de subtotal, validação de quantidade zero/negativa (Cenário 8) e preço negativo (Cenário 9).
- `src/test/java/com/maas/pedidos_conectado/service/ListaPedidoServiceTest.java`: Cobertura exaustiva dos critérios `CA-03`, `CA-04`, `CA-05` e todos os 9 cenários da tabela da Seção 8:
  - Cenário 1: 500,00 - 50,00 = 450,00 (Happy Path)
  - Cenário 2: 320,00 - 0,00 = 320,00 (Desconto Zero)
  - Cenário 3: 100,00 - 100,00 = 0,00 (Desconto Igual ao Subtotal)
  - Cenário 4: 100,00 - 180,00 = 0,00 (Desconto Superior com Piso Zero)
  - Cenário 5: 0,00 - 0,00 = 0,00 (Lista sem Itens)
  - Cenário 6: 0,00 - 30,00 = 0,00 (Lista sem Itens com Desconto)
  - Cenário 7: Desconto negativo rejeitado com mensagem de domínio
  - Cenário 8: Quantidade <= 0 rejeitada com mensagem de domínio
  - Cenário 9: Preço < 0 rejeitado com mensagem de domínio
  - Validação de isolamento entre listas distintas e remoção de itens (`RN-02`).
- `src/test/java/com/maas/pedidos_conectado/web/ListaPedidoControllerTest.java`: Testes de controller com `MockMvc` cobrindo adição de item, listagem, remoção e aplicação de desconto (sucesso e falha 400).

---

## 2. Principais Erros Detectados e Prevenidos Estaticamente

1. **Risco de Cruzamento de Itens Entre Listas (`RN-02` / `CA-03`):**
   - *Detecção:* Se a operação de remoção ou consulta de itens recebesse apenas o `itemId` sem checar o `listaId`, seria possível deletar ou expor itens pertencentes a outra lista de compra.
   - *Resolução:* Implementada validação estrita em `removerItem()` checando se `item.getListaId().equals(listaId)`. Caso divirja, lança `ValidacaoException`.

2. **Perda de Precisão em Cálculos Financeiros (`RN-04`, `RN-06`):**
   - *Detecção:* Uso de tipos primitivos `double` ou `float` para moedas acarreta imprecisões de ponto flutuante binário (ex.: `0.1 + 0.2 = 0.30000000000000004`).
   - *Resolução:* Todas as operações utilizam `BigDecimal` com arredondamento padronizado `RoundingMode.HALF_UP` e escala de 2 casas decimais.

3. **Garantia de Piso Zero contra Valores Negativos (`RN-06` / `CA-05`):**
   - *Detecção:* Subtração direta de subtotal menos desconto quando o desconto supera o subtotal geraria saldo credor negativo no total a pagar.
   - *Resolução:* Implementada a fórmula $\max(0, \text{Subtotal} - \text{Desconto})$ via comparação determinística com `BigDecimal.ZERO`, retornando exatamente `0.00` e impedindo qualquer valor inferior a zero.

4. **Tratamento de Quantidade Decimal e Preço Unitário Gratuito:**
   - *Detecção:* A especificação permite que quantidade seja decimal/inteira (ex.: 2.5 kg ou 100 unidades) e exige que seja estritamente positiva ($> 0.00$), enquanto preço unitário é não negativo ($\ge 0.00$, permitindo item com preço 0.00 como bonificação).
   - *Resolução:* Configurado `quantidade` como `BigDecimal` validando `compareTo(ZERO) <= 0`, e `precoUnitario` validando `compareTo(ZERO) < 0`.

---

## 3. Casos de Borda e Premissas Adotadas

- **Isolamento de Itens no Banco:** A chave estrangeira lógica é representada pelo campo `listaId` indexado no repositório `findByListaId`, preservando a regra de que não há catálogo global compartilhado.
- **Retrocompatibilidade nos DTOs:** `ListaPedidoResponseDTO` manteve construtores legados da iteração 1, permitindo que os testes existentes do Ciclo 1 continuem passando sem alterações.
- **Nenhum Lançamento de Dependência Extra:** Todas as operações numéricas e relacionais foram resolvidas com classes padrão do Java e Spring Data JPA já existentes no projeto.

---

## 4. Instruções de Verificação Local para o Desenvolvedor

Execute os seguintes comandos no terminal do projeto backend para compilar e rodar a suíte de testes atualizada:

```powershell
# Navegar até a pasta do backend
cd "c:\Nova pasta\TecAvanc\tecnicas-avancadas-micro-saas\msaas\back\pedidos_conectado"

# Rodar todos os testes automatizados (Ciclo 1 + Ciclo 2)
./mvnw test

# Ou rodar especificamente a suíte de validação dos critérios CA-03, CA-04, CA-05 e Seção 8:
./mvnw test -Dtest=ListaPedidoServiceTest,ItemPedidoTest,ListaPedidoControllerTest
```
