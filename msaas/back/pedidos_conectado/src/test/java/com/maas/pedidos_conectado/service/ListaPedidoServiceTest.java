package com.maas.pedidos_conectado.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.maas.pedidos_conectado.domain.ItemPedido;
import com.maas.pedidos_conectado.domain.ListaPedido;
import com.maas.pedidos_conectado.domain.exception.EntidadeNaoEncontradaException;
import com.maas.pedidos_conectado.domain.exception.ValidacaoException;
import com.maas.pedidos_conectado.repository.FornecedorRepository;
import com.maas.pedidos_conectado.repository.ItemPedidoRepository;
import com.maas.pedidos_conectado.repository.ListaPedidoRepository;
import com.maas.pedidos_conectado.web.dto.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListaPedidoServiceTest {

    @Mock
    private ListaPedidoRepository listaPedidoRepository;

    @Mock
    private FornecedorRepository fornecedorRepository;

    @Mock
    private ItemPedidoRepository itemPedidoRepository;

    @InjectMocks
    private ListaPedidoService listaPedidoService;

    // ==========================================
    // CICLO 1: CRITÉRIOS CA-01 E CA-02
    // ==========================================

    @Test
    @DisplayName("CA-01: Deve criar nova lista de pedido vinculada a fornecedor com status financeiro inicial zerado")
    void deveCriarListaComSucessoEStatusFinanceiroZeradoInicial_CA01() {
        String empresaId = "empresa-001";
        when(fornecedorRepository.existsById(empresaId)).thenReturn(true);

        CriarListaPedidoDTO dto = new CriarListaPedidoDTO(empresaId, "Reposição Emergencial Outubro");

        ListaPedido listaSalva = new ListaPedido(
                "lista-uuid-1",
                empresaId,
                "Reposição Emergencial Outubro",
                Instant.now(),
                BigDecimal.ZERO
        );

        when(listaPedidoRepository.save(any(ListaPedido.class))).thenReturn(listaSalva);

        ListaPedidoResponseDTO resposta = listaPedidoService.criar(dto);

        assertNotNull(resposta);
        assertEquals("lista-uuid-1", resposta.getId());
        assertEquals(empresaId, resposta.getEmpresaId());
        assertEquals("Reposição Emergencial Outubro", resposta.getTitulo());

        BigDecimal zeroEsperado = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        assertEquals(zeroEsperado, resposta.getSubtotalItens());
        assertEquals(zeroEsperado, resposta.getDescontoReais());
        assertEquals(zeroEsperado, resposta.getTotalLiquido());
        assertTrue(resposta.getItens().isEmpty());

        verify(fornecedorRepository, times(1)).existsById(empresaId);
        verify(listaPedidoRepository, times(1)).save(any(ListaPedido.class));
    }

    @Test
    @DisplayName("RN-01: Deve rejeitar criação de lista vinculada a empresa fornecedora inexistente")
    void deveRejeitarCriacaoDeListaVinculadaAEmpresaInexistente_RN01() {
        String empresaInexistenteId = "empresa-fantasma";
        when(fornecedorRepository.existsById(empresaInexistenteId)).thenReturn(false);

        CriarListaPedidoDTO dto = new CriarListaPedidoDTO(empresaInexistenteId, "Lista Válida");

        ValidacaoException ex = assertThrows(ValidacaoException.class, () -> listaPedidoService.criar(dto));
        assertTrue(ex.getMessage().contains("não existe"));

        verify(listaPedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar criação de lista quando empresaId for nulo ou em branco")
    void deveRejeitarCriacaoQuandoEmpresaIdForVazio() {
        CriarListaPedidoDTO dtoVazio = new CriarListaPedidoDTO("   ", "Título Válido");

        ValidacaoException ex = assertThrows(ValidacaoException.class, () -> listaPedidoService.criar(dtoVazio));
        assertTrue(ex.getMessage().contains("Identificador da empresa fornecedora é obrigatório"));

        CriarListaPedidoDTO dtoNulo = new CriarListaPedidoDTO(null, "Título Válido");
        assertThrows(ValidacaoException.class, () -> listaPedidoService.criar(dtoNulo));
    }

    @Test
    @DisplayName("Deve rejeitar criação de lista quando título tiver menos de 3 ou mais de 100 caracteres")
    void deveRejeitarCriacaoComTituloInvalido() {
        String empresaId = "empresa-001";
        when(fornecedorRepository.existsById(empresaId)).thenReturn(true);

        CriarListaPedidoDTO dtoCurto = new CriarListaPedidoDTO(empresaId, "AB");
        ValidacaoException exCurto = assertThrows(ValidacaoException.class, () -> listaPedidoService.criar(dtoCurto));
        assertTrue(exCurto.getMessage().contains("entre 3 e 100 caracteres"));

        String tituloLongo = "A".repeat(101);
        CriarListaPedidoDTO dtoLongo = new CriarListaPedidoDTO(empresaId, tituloLongo);
        ValidacaoException exLongo = assertThrows(ValidacaoException.class, () -> listaPedidoService.criar(dtoLongo));
        assertTrue(exLongo.getMessage().contains("entre 3 e 100 caracteres"));
    }

    @Test
    @DisplayName("CA-02: Deve renomear a lista dinamicamente preservando o vínculo com a empresa e ID")
    void deveRenomearListaDinamicaPreservandoEmpresaEId_CA02() {
        String listaId = "lista-xyz";
        String empresaIdOriginal = "empresa-001";
        Instant dataOriginal = Instant.now();
        ListaPedido listaExistente = new ListaPedido(listaId, empresaIdOriginal, "Pedido Rascunho", dataOriginal, BigDecimal.ZERO);

        when(listaPedidoRepository.findById(listaId)).thenReturn(Optional.of(listaExistente));
        when(listaPedidoRepository.save(any(ListaPedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemPedidoRepository.findByListaId(listaId)).thenReturn(List.of());

        RenomearListaPedidoDTO renomearDTO = new RenomearListaPedidoDTO("Pedido Final Aprovado - Diretoria");
        ListaPedidoResponseDTO resposta = listaPedidoService.renomear(listaId, renomearDTO);

        assertNotNull(resposta);
        assertEquals("Pedido Final Aprovado - Diretoria", resposta.getTitulo());
        assertEquals(listaId, resposta.getId());
        assertEquals(empresaIdOriginal, resposta.getEmpresaId());
        assertEquals(dataOriginal, resposta.getDataCriacao());

        verify(listaPedidoRepository, times(1)).save(listaExistente);
    }

    @Test
    @DisplayName("RN-03: Deve rejeitar renomeação quando novo título for inválido")
    void deveRejeitarRenomeacaoComTituloInvalido_RN03() {
        String listaId = "lista-xyz";
        ListaPedido listaExistente = new ListaPedido(listaId, "empresa-001", "Pedido Rascunho", Instant.now(), BigDecimal.ZERO);
        when(listaPedidoRepository.findById(listaId)).thenReturn(Optional.of(listaExistente));

        RenomearListaPedidoDTO dtoCurto = new RenomearListaPedidoDTO("Oi");
        ValidacaoException exCurto = assertThrows(ValidacaoException.class, () -> listaPedidoService.renomear(listaId, dtoCurto));
        assertTrue(exCurto.getMessage().contains("entre 3 e 100 caracteres"));

        RenomearListaPedidoDTO dtoLongo = new RenomearListaPedidoDTO("A".repeat(101));
        assertThrows(ValidacaoException.class, () -> listaPedidoService.renomear(listaId, dtoLongo));
    }

    // ==========================================
    // CICLO 2: CRITÉRIOS CA-03, CA-04 E CA-05
    // ==========================================

    @Test
    @DisplayName("CA-03: Inserção de Item e Isolamento por Lista (RN-02)")
    void deveInserirItemEIsolarPorLista_CA03() {
        // Dado que existem duas listas de pedidos distintas "Lista A" e "Lista B"
        String listaA = "lista-A";
        String listaB = "lista-B";

        when(listaPedidoRepository.existsById(listaA)).thenReturn(true);
        when(listaPedidoRepository.existsById(listaB)).thenReturn(true);

        CriarItemPedidoDTO itemDto = new CriarItemPedidoDTO(
                "Filtro de Óleo Lubrificante",
                new BigDecimal("10"),
                new BigDecimal("35.00")
        );

        ItemPedido itemSalvoListaA = new ItemPedido(
                "item-1",
                listaA,
                "Filtro de Óleo Lubrificante",
                new BigDecimal("10"),
                new BigDecimal("35.00")
        );

        when(itemPedidoRepository.save(any(ItemPedido.class))).thenReturn(itemSalvoListaA);
        when(itemPedidoRepository.findByListaId(listaA)).thenReturn(List.of(itemSalvoListaA));
        when(itemPedidoRepository.findByListaId(listaB)).thenReturn(List.of());

        // Quando eu adicionar o item na "Lista A"
        ItemPedidoResponseDTO itemCriado = listaPedidoService.adicionarItem(listaA, itemDto);

        // Então o item é persistido exclusivamente na "Lista A"
        assertNotNull(itemCriado);
        assertEquals(listaA, itemCriado.getListaId());
        assertEquals("Filtro de Óleo Lubrificante", itemCriado.getDescricao());

        List<ItemPedidoResponseDTO> itensListaA = listaPedidoService.listarItens(listaA);
        assertEquals(1, itensListaA.size());
        assertEquals("Filtro de Óleo Lubrificante", itensListaA.get(0).getDescricao());

        // E a consulta de itens da "Lista B" não exibe o "Filtro de Óleo Lubrificante"
        List<ItemPedidoResponseDTO> itensListaB = listaPedidoService.listarItens(listaB);
        assertTrue(itensListaB.isEmpty());
    }

    @Test
    @DisplayName("CA-04: Cálculo Correto do Subtotal dos Itens e da Lista (RN-04)")
    void deveCalcularSubtotalDosItensEDaListaCorretamente_CA04() {
        // Dado uma lista de pedido de compra com itens: Parafuso Sextavado (100 x 0,50) e Chave de Boca 14mm (2 x 25,00)
        ItemPedido item1 = new ItemPedido("i1", "lista-1", "Parafuso Sextavado", new BigDecimal("100"), new BigDecimal("0.50"));
        ItemPedido item2 = new ItemPedido("i2", "lista-1", "Chave de Boca 14mm", new BigDecimal("2"), new BigDecimal("25.00"));

        // Então o subtotal do item Parafuso Sextavado é exatamente R$ 50,00
        assertEquals(new BigDecimal("50.00").setScale(2, RoundingMode.HALF_UP), item1.getSubtotal());

        // E o subtotal do item Chave de Boca 14mm é exatamente R$ 50,00
        assertEquals(new BigDecimal("50.00").setScale(2, RoundingMode.HALF_UP), item2.getSubtotal());

        // E o subtotal geral da lista reflete exatamente R$ 100,00
        BigDecimal subtotalGeral = listaPedidoService.calcularSubtotalGeral(List.of(item1, item2));
        assertEquals(new BigDecimal("100.00").setScale(2, RoundingMode.HALF_UP), subtotalGeral);
    }

    @Test
    @DisplayName("CA-05: Aplicação de Desconto Fixo com Função max(0, Subtotal - Desconto) (RN-05 e RN-06)")
    void deveAplicarDescontoFixoComPisoZero_CA05() {
        String listaId = "lista-ca05";
        ListaPedido lista = new ListaPedido(listaId, "emp-1", "Pedido CA05", Instant.now(), BigDecimal.ZERO);

        ItemPedido item = new ItemPedido("i1", listaId, "Item Base", new BigDecimal("1"), new BigDecimal("200.00"));
        when(listaPedidoRepository.findById(listaId)).thenReturn(Optional.of(lista));
        when(listaPedidoRepository.save(any(ListaPedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemPedidoRepository.findByListaId(listaId)).thenReturn(List.of(item));

        // Quando eu aplicar um desconto fixo de R$ 50,00
        AplicarDescontoDTO dto50 = new AplicarDescontoDTO(new BigDecimal("50.00"));
        ListaPedidoResponseDTO resp50 = listaPedidoService.aplicarDesconto(listaId, dto50);

        // Então o valor total líquido final é calculado como R$ 150,00
        assertEquals(new BigDecimal("200.00").setScale(2, RoundingMode.HALF_UP), resp50.getSubtotalItens());
        assertEquals(new BigDecimal("50.00").setScale(2, RoundingMode.HALF_UP), resp50.getDescontoReais());
        assertEquals(new BigDecimal("150.00").setScale(2, RoundingMode.HALF_UP), resp50.getTotalLiquido());

        // Quando eu alterar o desconto fixo para R$ 250,00 (superior ao subtotal)
        AplicarDescontoDTO dto250 = new AplicarDescontoDTO(new BigDecimal("250.00"));
        ListaPedidoResponseDTO resp250 = listaPedidoService.aplicarDesconto(listaId, dto250);

        // Então o valor total líquido final é calculado exatamente como R$ 0,00 e não negativo
        assertEquals(new BigDecimal("200.00").setScale(2, RoundingMode.HALF_UP), resp250.getSubtotalItens());
        assertEquals(new BigDecimal("250.00").setScale(2, RoundingMode.HALF_UP), resp250.getDescontoReais());
        assertEquals(new BigDecimal("0.00").setScale(2, RoundingMode.HALF_UP), resp250.getTotalLiquido());
        assertTrue(resp250.getTotalLiquido().compareTo(BigDecimal.ZERO) >= 0);
    }

    // ==========================================
    // SEÇÃO 8: TABELA DE CASOS EMPÍRICOS (1 a 9)
    // ==========================================

    @Test
    @DisplayName("Cenário 1: Happy Path - Desconto inferior ao subtotal (500,00 - 50,00 = 450,00)")
    void cenario1_descontoInferiorAoSubtotal() {
        BigDecimal subtotal = new BigDecimal("500.00");
        BigDecimal desconto = new BigDecimal("50.00");
        BigDecimal totalEsperado = new BigDecimal("450.00").setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = listaPedidoService.calcularTotalLiquido(subtotal, desconto);
        assertEquals(totalEsperado, total);
    }

    @Test
    @DisplayName("Cenário 2: Happy Path - Compra com desconto zero (320,00 - 0,00 = 320,00)")
    void cenario2_descontoZero() {
        BigDecimal subtotal = new BigDecimal("320.00");
        BigDecimal desconto = BigDecimal.ZERO;
        BigDecimal totalEsperado = new BigDecimal("320.00").setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = listaPedidoService.calcularTotalLiquido(subtotal, desconto);
        assertEquals(totalEsperado, total);
    }

    @Test
    @DisplayName("Cenário 3: Edge Case - Desconto exatamente igual ao subtotal (100,00 - 100,00 = 0,00)")
    void cenario3_descontoIgualAoSubtotal() {
        BigDecimal subtotal = new BigDecimal("100.00");
        BigDecimal desconto = new BigDecimal("100.00");
        BigDecimal totalEsperado = new BigDecimal("0.00").setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = listaPedidoService.calcularTotalLiquido(subtotal, desconto);
        assertEquals(totalEsperado, total);
    }

    @Test
    @DisplayName("Cenário 4: Edge Case - Desconto superior ao subtotal (100,00 - 180,00 = 0,00, veda negativo)")
    void cenario4_descontoSuperiorAoSubtotalPisoZero() {
        BigDecimal subtotal = new BigDecimal("100.00");
        BigDecimal desconto = new BigDecimal("180.00");
        BigDecimal totalEsperado = new BigDecimal("0.00").setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = listaPedidoService.calcularTotalLiquido(subtotal, desconto);
        assertEquals(totalEsperado, total);
    }

    @Test
    @DisplayName("Cenário 5: Edge Case - Lista recém-criada sem itens (0,00 - 0,00 = 0,00)")
    void cenario5_listaRecemCriadaSemItens() {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal desconto = BigDecimal.ZERO;
        BigDecimal totalEsperado = new BigDecimal("0.00").setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = listaPedidoService.calcularTotalLiquido(subtotal, desconto);
        assertEquals(totalEsperado, total);
    }

    @Test
    @DisplayName("Cenário 6: Edge Case - Lista sem itens com desconto preenchido (0,00 - 30,00 = 0,00)")
    void cenario6_listaSemItensComDesconto() {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal desconto = new BigDecimal("30.00");
        BigDecimal totalEsperado = new BigDecimal("0.00").setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = listaPedidoService.calcularTotalLiquido(subtotal, desconto);
        assertEquals(totalEsperado, total);
    }

    @Test
    @DisplayName("Cenário 7: Error Case - Desconto com valor monetário negativo (-15,00) deve ser rejeitado")
    void cenario7_descontoNegativoRejeitado() {
        String listaId = "lista-c7";
        ListaPedido lista = new ListaPedido(listaId, "emp-1", "Pedido", Instant.now(), BigDecimal.ZERO);
        when(listaPedidoRepository.findById(listaId)).thenReturn(Optional.of(lista));

        AplicarDescontoDTO dtoNegativo = new AplicarDescontoDTO(new BigDecimal("-15.00"));
        ValidacaoException ex = assertThrows(ValidacaoException.class, () ->
                listaPedidoService.aplicarDesconto(listaId, dtoNegativo)
        );
        assertEquals("Desconto em R$ não pode ser negativo.", ex.getMessage());
    }

    @Test
    @DisplayName("Cenário 8: Error Case - Quantidade menor ou igual a zero rejeitada")
    void cenario8_quantidadeMenorOuIgualAZero() {
        String listaId = "lista-c8";
        when(listaPedidoRepository.existsById(listaId)).thenReturn(true);

        CriarItemPedidoDTO dtoZero = new CriarItemPedidoDTO("Item Zero", BigDecimal.ZERO, new BigDecimal("10.00"));
        ValidacaoException exZero = assertThrows(ValidacaoException.class, () ->
                listaPedidoService.adicionarItem(listaId, dtoZero)
        );
        assertEquals("Quantidade do item deve ser estritamente maior que zero.", exZero.getMessage());
    }

    @Test
    @DisplayName("Cenário 9: Error Case - Preço unitário negativo rejeitado")
    void cenario9_precoUnitarioNegativo() {
        String listaId = "lista-c9";
        when(listaPedidoRepository.existsById(listaId)).thenReturn(true);

        CriarItemPedidoDTO dtoPrecoNegativo = new CriarItemPedidoDTO("Item Negativo", new BigDecimal("2"), new BigDecimal("-10.00"));
        ValidacaoException ex = assertThrows(ValidacaoException.class, () ->
                listaPedidoService.adicionarItem(listaId, dtoPrecoNegativo)
        );
        assertEquals("Preço unitário não pode ser negativo.", ex.getMessage());
    }

    @Test
    @DisplayName("RN-02: Deve remover item exclusivo com sucesso")
    void deveRemoverItemComSucesso() {
        String listaId = "lista-rem";
        String itemId = "item-rem";
        ItemPedido item = new ItemPedido(itemId, listaId, "Item", BigDecimal.ONE, BigDecimal.TEN);

        when(listaPedidoRepository.existsById(listaId)).thenReturn(true);
        when(itemPedidoRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertDoesNotThrow(() -> listaPedidoService.removerItem(listaId, itemId));
        verify(itemPedidoRepository, times(1)).delete(item);
    }

    @Test
    @DisplayName("RN-02: Deve rejeitar remoção quando item não pertencer à lista informada")
    void deveRejeitarRemocaoQuandoItemNaoPertencerALista() {
        String listaId = "lista-1";
        String itemId = "item-2";
        ItemPedido itemOutraLista = new ItemPedido(itemId, "outra-lista", "Item", BigDecimal.ONE, BigDecimal.TEN);

        when(listaPedidoRepository.existsById(listaId)).thenReturn(true);
        when(itemPedidoRepository.findById(itemId)).thenReturn(Optional.of(itemOutraLista));

        ValidacaoException ex = assertThrows(ValidacaoException.class, () ->
                listaPedidoService.removerItem(listaId, itemId)
        );
        assertTrue(ex.getMessage().contains("não pertence à lista"));
        verify(itemPedidoRepository, never()).delete(any());
    }
}
