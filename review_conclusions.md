# Relatório de Análise Estática e Conclusões de Implementação - Ciclo 1

**Data:** 10/10/2026  
**Iteração:** Ciclo 1 - Fundação de Fornecedores e Listas de Pedidos (Spec 001)  
**Metodologia:** Inspeção lógica estática e raciocínio de AST sem execução local de terminal (Zero Execution Policy)

---

## 1. Arquivos Criados / Modificados

### Planejamento e Governança
- `docs/PLAN.md`: Plano diretor em 5 ciclos progressivos com dependências, critérios de aceite e regras de negócio.
- `commit-msg.txt`: Mensagem descritiva pré-formatada para o desenvolvedor realizar o commit da iteração.

### Backend (`msaas/back/pedidos_conectado`)
- `domain/exception/ValidacaoException.java`: Exceção disparada para violações de regras de negócio e validações da spec.
- `domain/exception/EntidadeNaoEncontradaException.java`: Exceção disparada para recursos não localizados por identificador.
- `domain/Fornecedor.java`: Entidade de domínio JPA para a empresa fornecedora, com normalização de strings e validações de tamanho.
- `domain/ListaPedido.java`: Entidade de domínio JPA para a lista de pedido de compra, com método `renomear` e validações de título e desconto.
- `repository/FornecedorRepository.java`: Interface Spring Data JPA para persistência de fornecedores.
- `repository/ListaPedidoRepository.java`: Interface Spring Data JPA para persistência de listas com busca por empresa.
- `web/dto/CriarFornecedorDTO.java`: Payload de entrada para cadastro de fornecedor.
- `web/dto/FornecedorResponseDTO.java`: Resposta estruturada com dados do fornecedor.
- `web/dto/CriarListaPedidoDTO.java`: Payload de entrada para criação de lista vinculada a fornecedor.
- `web/dto/RenomearListaPedidoDTO.java`: Payload de entrada para renomeação do título da lista.
- `web/dto/ListaPedidoResponseDTO.java`: Resposta com identificador, vínculo, título, data e status financeiro inicial zerado.
- `web/dto/ErroResponseDTO.java`: Resposta padronizada para erros de validação e não encontrados.
- `web/GlobalExceptionHandler.java`: Controller advice mapeando exceções de domínio para HTTP 400 e HTTP 404.
- `service/FornecedorService.java`: Serviço de aplicação para gestão e consulta de fornecedores.
- `service/ListaPedidoService.java`: Serviço de aplicação com validação de vínculo mandatório (`RN-01`) e renomeação dinâmica (`RN-03`).
- `web/FornecedorController.java`: Endpoints REST (`/api/fornecedores`) com CORS habilitado.
- `web/ListaPedidoController.java`: Endpoints REST (`/api/listas`) com endpoints para criação, busca e renomeação de título (`PATCH`).

### Testes Automatizados Backend
- `src/test/java/com/maas/pedidos_conectado/service/FornecedorServiceTest.java`: Testes unitários para regras de fornecedor.
- `src/test/java/com/maas/pedidos_conectado/service/ListaPedidoServiceTest.java`: Testes unitários validando `CA-01`, `CA-02`, `RN-01` e `RN-03`.
- `src/test/java/com/maas/pedidos_conectado/web/ListaPedidoControllerTest.java`: Testes de camada web com `MockMvc` validando serialização JSON, status HTTP (201, 200, 400, 404) e tratamento de exceções.

---

## 2. Principais Erros Detectados e Prevenidos Estaticamente

1. **Risco de Inconsistência no Vínculo de Fornecedor (`RN-01` / `CA-01`):**
   - *Detecção:* A especificação exige que a lista seja compulsoriamente vinculada a uma empresa existente. Caso o serviço não verificasse previamente a existência no `FornecedorRepository`, poderiam ser criadas listas órfãs ou violar integridade relacional.
   - *Resolução:* Adicionada checagem explícita com `fornecedorRepository.existsById()` em `ListaPedidoService.criar()`, lançando `ValidacaoException` (HTTP 400) com mensagem clara.

2. **Espaços em Branco e Limites de Título e Razão Social:**
   - *Detecção:* A spec estabelece que a razão social tem de 3 a 120 caracteres com "texto aparado" (*trimmed*), e o título de 3 a 100 caracteres. Textos compostos apenas por espaços ("   ") passariam em checagens ingênuas de `length()`.
   - *Resolução:* Implementado tratamento em cascata no domínio e serviço com `trim()` e validação de tamanho antes da persistência.

3. **Status Financeiro Inicial Zerado (`CA-01`):**
   - *Detecção:* A criação de lista precisa retornar subtotal, desconto e total líquido inicial como `0.00` garantindo 2 casas decimais.
   - *Resolução:* `ListaPedidoResponseDTO` inicializa os campos financeiros com `BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)`.

4. **Preservação de Dados na Renomeação (`RN-03` / `CA-02`):**
   - *Detecção:* O método de renomeação precisava alterar unicamente o título, preservando imutáveis o identificador único da lista, a data de criação e o fornecedor vinculado.
   - *Resolução:* `ListaPedido.renomear()` opera cirurgicamente sobre o atributo `titulo`, revalidando as restrições de tamanho (3 a 100 caracteres).

---

## 3. Casos de Borda e Premissas Adotadas

- **Chave Primária Alfanumérica/UUID:** Foi utilizado `UUID.randomUUID().toString()` em formato String (comprimento 36), atendendo ao tipo `String (UUID / Alfanumérico)` especificado na tabela de entidades.
- **Isolamento de Testes com MockMvc Standalone:** Para os testes de controle, utilizou-se o setup standalone do MockMvc combinado com MockitoExtension. Isso garante que a suíte execute instantaneamente sem depender do boot do contexto Spring completo nem de drivers de banco externos.
- **Portabilidade de Dependências:** Não foram adicionadas dependências externas além do que já consta no `pom.xml` (`spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`, `h2`). O validation de domínio foi feito em código puro Java sem forçar a necessidade de `jakarta.validation` adicional no `pom.xml`.

---

## 4. Instruções de Verificação Local para o Desenvolvedor

Execute os seguintes comandos no terminal do projeto backend para compilar e rodar a suíte de testes do Ciclo 1:

```powershell
# Navegar até a pasta do backend
cd "c:\Nova pasta\TecAvanc\tecnicas-avancadas-micro-saas\msaas\back\pedidos_conectado"

# Rodar todos os testes automatizados
./mvnw test

# Ou rodar especificamente a suíte de validação dos critérios CA-01 e CA-02:
./mvnw test -Dtest=ListaPedidoServiceTest,FornecedorServiceTest,ListaPedidoControllerTest
```
