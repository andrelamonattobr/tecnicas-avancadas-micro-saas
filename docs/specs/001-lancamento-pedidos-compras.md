# Especificação da Feature 001: Lançamento de Pedidos de Compras

**Identificador da Spec:** `001-lancamento-pedidos-compras`  
**Versão:** 2.0.0 (Endurecida após Revisão Cruzada e Varreduras da Aula 08)  
**Status:** Aprovada para Implementação  
**Data de Criação:** 12/09/2026  
**Última Atualização:** 03/10/2026  
**Autor:** Equipe de Engenharia Micro-SaaS UniRV  
**Revisor:** Equipe Beta (Revisão Cruzada da Oficina)  

---

## 1. Objetivo

Fornecer aos compradores e operadores de suprimentos uma solução ágil e determinística para cadastrar empresas fornecedoras, criar listas de pedidos de compras vinculadas a um fornecedor específico, renomear listas a qualquer momento, registrar itens exclusivos para cada pedido com cálculo de subtotais, aplicar desconto global fixo em moeda nacional (R$) e calcular o valor total líquido final (garantindo piso mínimo de R$ 0,00), além de exportar o pedido completo em formato Markdown padronizado.

A dor central solucionada é a eliminação de planilhas despadronizadas e de sistemas complexos com catálogos globais engessados, viabilizando o lançamento rápido de pedidos sob demanda com conferência imediata de valores.

---

## 2. Escopo

### 2.1. O que entra (Escopo Inclusivo)
1. **Gestão de Empresas Fornecedoras:** Cadastro contendo identificador único, razão social, nome fantasia (opcional), CNPJ (opcional) e canal de contato (opcional). Listagem e consulta de fornecedores cadastrados.
2. **Gestão de Listas de Pedidos de Compras:** Criação de lista associada compulsoriamente a exatamente uma Empresa Fornecedora. Cada lista possui título descritivo inicial, data de criação e campo monetário de desconto fixo em R$.
3. **Renomeação Dinâmica da Lista:** Funcionalidade de alteração do título da lista a qualquer instante do ciclo de vida, sem impactar os itens contidos ou o vínculo com a empresa.
4. **Gestão de Itens do Pedido:** Inclusão, alteração, remoção e listagem de produtos pertencentes com exclusividade àquela lista de compras. Cada item possui descrição, quantidade (número positivo maior que zero) e preço unitário em R$ (número não negativo).
5. **Cálculo de Subtotais dos Itens:** Multiplicação determinística da quantidade pelo preço unitário para cada item lançado.
6. **Somatório Geral dos Itens:** Consolidação da soma aritmética de todos os subtotais dos itens contidos na lista.
7. **Aplicação de Desconto Fixo em R$:** Dedução de valor monetário absoluto fixo (R$) fornecido pelo usuário.
8. **Cálculo do Valor Total Líquido Final:** Aplicação da função matemática $\max(0, \text{Subtotal Geral} - \text{Desconto em R\$})$. Se o desconto for igual ou superior ao subtotal dos itens, o total líquido a pagar é estritamente R$ 0,00.
9. **Exportação Canônica em Markdown:** Geração de documento de texto em formato Markdown contendo cabeçalho com identificação do fornecedor, metadados da lista, tabela formatada com colunas de descrição, quantidade, preço unitário e subtotal do item, seguida de sumário financeiro com subtotal bruto, desconto em R$ aplicado e valor total líquido final.

### 2.2. O que NÃO entra (Anti-Escopo Obrigatório)
1. **Catálogo Global de Produtos:** Não há cadastro centralizado nem catálogo compartilhado de produtos. Cada item é único e pertence unicamente à lista onde foi lançado.
2. **Compartilhamento ou Reutilização de Itens:** É expressamente vedado reaproveitar itens, sincronizar preços ou transferir produtos entre diferentes listas de compra.
3. **Desconto Percentual:** O sistema não suporta taxas percentuais (ex.: 10% ou 15%); todo desconto é estritamente um montante monetário fixo em R$.
4. **Saldo Monetário Negativo:** É impossível resultar em valor a pagar inferior a zero. O piso é estritamente R$ 0,00.
5. **Controle Integrado de Estoque:** Não há controle de entrada/saída física de armazém, saldo de estoque, ponto de pedido ou inventário.
6. **Emissão Fiscal ou Contábil:** Não há integração com SEFAZ para emissão de NF-e, NFC-e ou cálculo de tributos (ICMS, IPI, PIS/COFINS).
7. **Gateway de Pagamento:** Não há processamento de cartão de crédito, PIX automático, boleto ou reconciliação bancária.
8. **Logística e Despacho:** Não há rastreamento de transportadora, frete dinâmico ou status de entrega física.
9. **Multi-Tenancy e Login Social:** O sistema opera como micro-SaaS focado no lançamento e cálculo local/departamental, sem gestão complexa de autenticação corporativa nesta versão.

---

## 3. Atores

- **Comprador / Operador de Suprimentos:** Usuário responsável por registrar as empresas fornecedoras, criar e renomear as listas de compras, lançar e atualizar os itens correspondentes, estipular descontos monetários e exportar o relatório do pedido formatado em Markdown para envio ao fornecedor.

---

## 4. Dados e Contratos de Entidades

### 4.1. Entidade `Empresa Fornecedora`
| Campo | Tipo | Obrigatoriedade | Restrições / Validações | Valor Padrão |
| :--- | :--- | :---: | :--- | :---: |
| `id` | String (UUID / Alfanumérico) | Obrigatório | Identificador único não vazio | Gerado automaticamente |
| `razao_social` | String | Obrigatório | De 3 a 120 caracteres, texto aparado | N/A |
| `nome_fantasia` | String | Opcional | Até 100 caracteres | "" (vazio) |
| `cnpj` | String | Opcional | Formato numérico de 14 dígitos ou com pontuação padrão | "" (vazio) |
| `contato` | String | Opcional | Telefone ou e-mail de contato (até 100 caracteres) | "" (vazio) |

### 4.2. Entidade `Lista de Pedido de Compra`
| Campo | Tipo | Obrigatoriedade | Restrições / Validações | Valor Padrão |
| :--- | :--- | :---: | :--- | :---: |
| `id` | String (UUID / Alfanumérico) | Obrigatório | Identificador único não vazio | Gerado automaticamente |
| `empresa_id` | String | Obrigatório | Deve corresponder a uma Empresa Fornecedora existente | N/A |
| `titulo` | String | Obrigatório | De 3 a 100 caracteres, renomeável a qualquer momento | N/A |
| `data_criacao` | String (ISO 8601) | Obrigatório | Carimbo de data/hora válido | Timestamp atual |
| `desconto_reais` | Decimal (Monetário) | Obrigatório | Valor numérico $\ge 0.00$, precisão de 2 casas decimais | 0.00 |

### 4.3. Entidade `Produto / Item de Pedido`
| Campo | Tipo | Obrigatoriedade | Restrições / Validações | Valor Padrão |
| :--- | :--- | :---: | :--- | :---: |
| `id` | String (UUID / Alfanumérico) | Obrigatório | Identificador único no escopo da lista | Gerado automaticamente |
| `lista_id` | String | Obrigatório | Deve corresponder à lista em que o item é inserido | N/A |
| `descricao` | String | Obrigatório | De 2 a 120 caracteres, identificando o produto | N/A |
| `quantidade` | Número decimal/inteiro | Obrigatório | Estritamente $> 0.00$ (número positivo) | N/A |
| `preco_unitario` | Decimal (Monetário) | Obrigatório | Valor numérico $\ge 0.00$, precisão de 2 casas decimais | N/A |

---

## 5. Regras de Negócio

- **RN-01 (Vínculo Mandatório de Empresa):** Toda lista de pedido de compra deve ser criada associada a exatamente uma empresa fornecedora previamente cadastrada no sistema. Tentativas de vincular a um `empresa_id` inexistente resultam em erro de validação.
- **RN-02 (Isolamento Estrito de Itens):** Cada item lançado pertence unicamente à lista indicada por `lista_id`. Não existe catálogo mestre global de produtos; itens de uma lista não podem ser acessados, referenciados ou reaproveitados por outras listas.
- **RN-03 (Renomeação Irrestrita de Título):** O título da lista de pedido pode ser renomeado a qualquer momento da sua existência. A alteração de título não altera os identificadores dos itens nem recalcula valores.
- **RN-04 (Cálculo de Subtotais):**
  - O subtotal de cada item é calculado deterministicamente pela fórmula:
    $$\text{Subtotal}_{\text{Item}} = \text{quantidade} \times \text{preco\_unitario}$$
  - O subtotal geral da lista é o somatório aritmético dos subtotais de todos os itens cadastrados:
    $$\text{Subtotal}_{\text{Geral}} = \sum_{i=1}^{n} \text{Subtotal}_{\text{Item}_i}$$
- **RN-05 (Desconto Fixo em Reais):** O campo `desconto_reais` representa uma dedução monetária absoluta fixa expressa em R$. O valor deve ser não-negativo ($\ge 0$). Qualquer valor negativo informado é sumariamente rejeitado.
- **RN-06 (Total Líquido Não-Negativo com Piso Zero):** O valor total final líquido a pagar é determinado pela fórmula:
  $$\text{Total Líquido} = \max(0, \, \text{Subtotal}_{\text{Geral}} - \text{desconto\_reais})$$
  Quando o desconto em R$ for maior ou igual ao subtotal geral, o total líquido final será exatamente R$ 0,00. É categoricamente proibido saldo negativo.
- **RN-07 (Estrutura de Exportação em Markdown):** A exportação do pedido deve produzir uma string em Markdown contendo exatamente:
  1. Cabeçalho com o nome/título da lista e data de criação.
  2. Informações da empresa fornecedora (razão social, CNPJ e contato).
  3. Tabela com as colunas: `Item`, `Descrição`, `Qtd`, `Preço Unitário (R$)`, `Subtotal (R$)`.
  4. Bloco de resumo financeiro discriminando: `Subtotal dos Itens`, `Desconto Aplicado (R$)` e `Total Final Líquido (R$)`.

---

## 6. Critérios de Aceite (Formato Gherkin Aprovado no Teste dos Três Dedos)

### CA-01: Cadastro de Fornecedor e Criação da Lista
```gherkin
Dado que uma empresa fornecedora é cadastrada com razão social "Distribuidora Nacional de Peças Ltda"
Quando eu criar uma nova lista de pedido vinculada a esta empresa com o título "Reposição Emergencial Outubro"
Então a lista é criada com sucesso possuindo um identificador único
E o status financeiro inicial indica subtotal R$ 0,00, desconto R$ 0,00 e total líquido R$ 0,00
```

### CA-02: Renomeação Dinâmica da Lista
```gherkin
Dado que existe uma lista de pedido ativa com o título "Pedido Rascunho"
Quando eu solicitar a renomeação do título para "Pedido Final Aprovado - Diretoria"
Então o título da lista é atualizado para "Pedido Final Aprovado - Diretoria"
E os itens anteriormente inseridos e o fornecedor vinculado permanecem inalterados
```

### CA-03: Inserção de Item e Isolamento por Lista
```gherkin
Dado que existem duas listas de pedidos distintas "Lista A" e "Lista B"
Quando eu adicionar o item "Filtro de Óleo Lubrificante" (Qtd: 10, Preço: R$ 35,00) na "Lista A"
Então o item é persistido exclusivamente na "Lista A"
E a consulta de itens da "Lista B" não exibe o "Filtro de Óleo Lubrificante"
```

### CA-04: Cálculo Correto do Subtotal dos Itens e da Lista
```gherkin
Dado uma lista de pedido de compra
Quando eu adicionar os seguintes itens:
  | Descrição            | Qtd | Preço Unitário |
  | Parafuso Sextavado   | 100 | R$ 0,50        |
  | Chave de Boca 14mm   | 2   | R$ 25,00       |
Então o subtotal do item Parafuso Sextavado é exatamente R$ 50,00
E o subtotal do item Chave de Boca 14mm é exatamente R$ 50,00
E o subtotal geral da lista reflete exatamente R$ 100,00
```

### CA-05: Aplicação de Desconto Fixo com Função $\max(0, \text{Subtotal} - \text{Desconto})$
```gherkin
Dado uma lista de pedido cujo subtotal geral dos itens totaliza R$ 200,00
Quando eu aplicar um desconto fixo de R$ 50,00
Então o valor total líquido final é calculado como R$ 150,00
Quando eu alterar o desconto fixo para R$ 250,00 (superior ao subtotal)
Então o valor total líquido final é calculado exatamente como R$ 0,00
E nenhum valor numérico negativo é retornado
```

### CA-06: Exportação Completa Formatada em Markdown
```gherkin
Dado um pedido com fornecedor "Alimentos do Campo S.A.", 1 item de R$ 80,00 e desconto de R$ 10,00
Quando eu requisitar a exportação do pedido para Markdown
Então o texto gerado contém a razão social "Alimentos do Campo S.A."
E contém a tabela formatada com colunas de Descrição, Qtd, Preço Unitário e Subtotal
E apresenta o resumo financeiro com Subtotal R$ 80,00, Desconto R$ 10,00 e Total Líquido R$ 70,00
```

---

## 7. Restrições Técnicas de Implementação

1. **Stack Tecnológica:** Spring boot para o backend da ferramenta, utilizando as dependecias Spring Web MVC, Spring Data JPA e H2 Database. E Angular para o frontend, que está utilizando typescript e css;
2. **Precisão Numérica:** Arredondamento monetário em 2 casas decimais no momento da exibição e exportação.

---

## 8. Tabela de Exemplos da Regra Principal (RN-04, RN-05 e RN-06)

A tabela a seguir apresenta os casos de teste empíricos que cobrem o caminho feliz (*happy path*), os casos de borda (*edge cases*) e as condições de erro (*error cases*).

| Cenário | Descrição do Caso | Subtotal Itens (R$) | Desconto em R$ | Total Líquido Esperado | Comportamento / Validação |
| :---: | :--- | :---: | :---: | :---: | :--- |
| **1** | **Happy Path:** Desconto inferior ao subtotal | 500,00 | 50,00 | **450,00** | Cálculo padrão: $500 - 50 = 450$. |
| **2** | **Happy Path:** Compra com desconto zero | 320,00 | 0,00 | **320,00** | Desconto nulo mantém o subtotal integral. |
| **3** | **Edge Case:** Desconto exatamente igual ao subtotal | 100,00 | 100,00 | **0,00** | Operação resulta em zero exato. |
| **4** | **Edge Case:** Desconto superior ao subtotal | 100,00 | 180,00 | **0,00** | $\max(0, 100 - 180) = 0$. Veda saldo negativo. |
| **5** | **Edge Case:** Lista recém-criada sem itens | 0,00 | 0,00 | **0,00** | Lista vazia possui total líquido zero. |
| **6** | **Edge Case:** Lista sem itens com desconto preenchido | 0,00 | 30,00 | **0,00** | $\max(0, 0 - 30) = 0$. Mantém piso zero. |
| **7** | **Error Case:** Desconto com valor monetário negativo | 200,00 | -15,00 | *Rejeição* | Lança exceção de domínio: "Desconto em R$ não pode ser negativo". |
| **8** | **Error Case:** Item com quantidade menor ou igual a zero | N/A | N/A | *Rejeição* | Lança exceção: "Quantidade do item deve ser estritamente maior que zero". |
| **9** | **Error Case:** Item com preço unitário negativo | N/A | N/A | *Rejeição* | Lança exceção: "Preço unitário não pode ser negativo". |

---

## 9. Seção de Decisões e Resolução de Ambiguidades

Durante o refinamento pós-Aula 08 e a revisão por pares, as seguintes ambiguidades foram formalmente debatidas e decididas pela equipe:

1. **Decisão sobre Catálogo de Produtos:**
   - *Ambiguidade levantada:* "O produto pertence a um catálogo global da empresa ou é digitado livremente?"
   - *Decisão Adotada:* O produto pertence exclusivamente à lista de compra em que é inserido. Não existe catálogo global. Essa decisão elimina a necessidade de sincronização complexa de cadastros de itens em cotações e compras pontuais.
2. **Decisão sobre Natureza do Desconto:**
   - *Ambiguidade levantada:* "O desconto é percentual (ex: 5%) ou monetário fixo (ex: R$ 50,00)?"
   - *Decisão Adotada:* O desconto é estritamente fixo em reais (R$). Descontos percentuais foram rejeitados para manter o alinhamento com negociações diretas onde o fornecedor concede abatimento em valor fechado.
3. **Decisão sobre Desconto Superior ao Subtotal:**
   - *Ambiguidade levantada:* "Se o desconto for maior que o somatório dos itens, o sistema deve registrar valor negativo ou lançar erro?"
   - *Decisão Adotada:* Utiliza-se a função $\max(0, \text{Subtotal} - \text{Desconto})$. O total líquido torna-se R$ 0,00. Não lança erro, mas impede qualquer representação de débito negativo do comprador.
4. **Decisão sobre Renomeação da Lista:**
   - *Ambiguidade levantada:* "Renomear a lista altera seu identificador ou restringe alterações após a adição de itens?"
   - *Decisão Adotada:* O título da lista é totalmente mutável a qualquer momento por meio de método explícito, preservando imutável o `id` da lista e todos os itens vinculados.

---

## 10. Resposta Canônica de Autossuficiência da Especificação

Conforme exigido nas diretrizes acadêmicas da disciplina:
> **Pergunta:** *"Se todo o código-fonte deste projeto fosse apagado neste exato instante, esta especificação seria suficiente para reconstruí-lo integralmente?"*

**Resposta Formal:**  
**SIM.** A especificação contém a definição unívoca de todas as entidades de dados com seus respectivos tipos e limites, o anti-escopo explícito que blinda contra recursos desnecessários, a formulação matemática determinística de todas as equações financeiras, o catálogo completo de regras de negócio (`RN-01` a `RN-07`), os critérios de aceite estruturados em Gherkin no padrão dos três dedos e a tabela exaustiva de casos de teste numéricos. Qualquer desenvolvedor ou agente autônomo é capaz de regenerar o software de ponta a ponta sem recorrer a adivinhações.
