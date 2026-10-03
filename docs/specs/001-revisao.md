# Registro de Revisão Cruzada da Especificação (Feature 001)

**Artefato Auditado:** `docs/specs/001-lancamento-pedidos-compras.md`  
**Equipe Autora:** Equipe Micro-SaaS UniRV (Lançamento de Pedidos de Compras)  
**Equipe Revisora:** Equipe Beta (Sistema de Gestão de Frotas e Logística)  
**Data da Revisão:** 12/09/2026 (Oficina da Aula 07 / LAB 1 da Aula 08)  
**Disciplina:** Técnicas Avançadas de Construção de Software (ESW442)  
**Professor:** Gustavo Martins Lima  

---

## 1. Contexto da Dinâmica

Em conformidade com a dinâmica pedagógica estabelecida na **Aula 07 (Slide 18)** e aprofundada no **LAB 1 da Aula 08**, a especificação da Feature 001 foi submetida à leitura crítica e independente da Equipe Beta, sem interlocução verbal prévia. O objetivo foi detectar ambiguidades lexicais, omissões de escopo e inconsistências contratuais antes da codificação.

---

## 2. Parecer Técnico sobre as Três Perguntas Canônicas

### Pergunta 1: *Consegui entender o que deve ser construído sem perguntar nada a vocês?*
- **Avaliação da Equipe Revisora:** **SIM, COM TOTAL CLAREZA.**
- **Fundamentação:**
  - O documento demarca com extrema precisão a hierarquia das entidades: a `Empresa Fornecedora` é a raiz, a `Lista de Pedido de Compra` vincula-se a ela e os `Produtos / Itens` estão contidos unicamente dentro da lista.
  - A delimitação negativa (**Anti-escopo**) eliminou qualquer dúvida conceitual: compreendemos de imediato que não era para implementar catálogo central de produtos, controle de estoque ou emissão fiscal.
  - Os contratos de dados definem tipos, restrições e limites de caracteres de maneira não ambígua.

---

### Pergunta 2: *Achei alguma frase que admite duas leituras diferentes?*
- **Avaliação da Equipe Revisora:** **FORAM DETECTADAS 2 AMBIGUIDADES NA VERSÃO INICIAL, AMBAS SANADAS NA VERSÃO ATUAL (v2.0.0).**
- **Apontamentos Realizados e Correções Verificadas:**
  1. *Apontamento 1 (Cálculo do Desconto quando superior ao total):*
     - *Trecho original:* "O desconto é subtraído do total dos produtos."
     - *Risco identificado:* Admitia dupla interpretação: se o desconto fosse maior que a soma dos produtos, o total ficaria negativo ou geraria erro no fechamento da compra?
     - *Correção efetuada:* A equipe autora introduziu formalmente a função matemática $\text{Total Líquido} = \max(0, \text{Subtotal Geral} - \text{Desconto em R\$})$ e especificou a regra explícita de piso em zero na RN-06 e nos critérios de aceite.
  2. *Apontamento 2 (Renomeação da Lista vs. Itens já adicionados):*
     - *Trecho original:* "A lista possui nome customizável."
     - *Risco identificado:* Poderia sugerir que o nome só pode ser escolhido na criação da lista, ou que renomear após a inclusão de itens exigiria confirmação de recálculo.
     - *Correção efetuada:* A RN-03 foi explicitada, declarando que o título pode ser alterado a qualquer momento sem qualquer efeito colateral sobre os itens contidos.

---

### Pergunta 3: *Consigo dizer, lendo só os critérios de aceite, se a feature está pronta?*
- **Avaliação da Equipe Revisora:** **SIM, PLENAMENTE.**
- **Fundamentação:**
  - Todos os 6 critérios de aceite formulados (`CA-01` a `CA-06`) foram submetidos e aprovados no **Teste dos Três Dedos**:
    1. *São binários:* O resultado é estritamente verdadeiro ou falso, sem termos vagos ("fácil", "rápido").
    2. *São observáveis:* Validam valores monetários de retorno, textos na saída Markdown e mensagens de validação.
    3. *São livres de implementação:* Não citam nomes de classes, tabelas de banco relacional ou bibliotecas internas, focando estritamente no comportamento de negócio visível.
  - A inclusão da **Tabela de Exemplos** com cenários de caso feliz, caso de borda ($\text{desconto} = \text{subtotal}$ e $\text{desconto} > \text{subtotal}$) e casos de erro permite que qualquer pessoa ou suíte de testes automatizada comprove a conclusão da feature de maneira determinística.

---

## 3. Conclusão da Revisão

A especificação `001-lancamento-pedidos-compras.md` atende com excelência a todos os requisitos didáticos e técnicos estabelecidos para a disciplina ESW442, encontrando-se formalmente **APROVADA** para servir como fonte da verdade executável pelo agente de engenharia de software.
