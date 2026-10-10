package com.maas.pedidos_conectado;

import static org.junit.jupiter.api.Assertions.*;

import com.maas.pedidos_conectado.domain.exception.ValidacaoException;
import com.maas.pedidos_conectado.service.FornecedorService;
import com.maas.pedidos_conectado.service.ListaPedidoService;
import com.maas.pedidos_conectado.web.dto.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class FluxoCompletoE2ETest {

    @Autowired
    private FornecedorService fornecedorService;

    @Autowired
    private ListaPedidoService listaPedidoService;

    @Test
    @DisplayName("Homologação E2E: Ciclo completo da Feature 001 cobrindo CA-01 a CA-06 e Anti-Escopo")
    void fluxoCompletoE2E_HomologacaoSpec001() {
        // ==========================================
        // 1. CA-01: Cadastro de Fornecedor e Criação da Lista
        // ==========================================
        CriarFornecedorDTO fornecedorDTO = new CriarFornecedorDTO(
                "Distribuidora Nacional de Peças Ltda",
                "Distribuidora Nacional",
                "12.345.678/0001-90",
                "contato@pecas.com.br"
        );
        FornecedorResponseDTO fornecedorCriado = fornecedorService.criar(fornecedorDTO);
        assertNotNull(fornecedorCriado.getId());
        assertEquals("Distribuidora Nacional de Peças Ltda", fornecedorCriado.getRazaoSocial());

        // Criar Lista A vinculada à empresa
        CriarListaPedidoDTO listaADTO = new CriarListaPedidoDTO(
                fornecedorCriado.getId(),
                "Reposição Emergencial Outubro"
        );
        ListaPedidoResponseDTO listaACriada = listaPedidoService.criar(listaADTO);
        assertNotNull(listaACriada.getId());
        assertEquals("Reposição Emergencial Outubro", listaACriada.getTitulo());
        assertEquals(fornecedorCriado.getId(), listaACriada.getEmpresaId());

        // Status financeiro inicial zerado
        BigDecimal zero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        assertEquals(zero, listaACriada.getSubtotalItens());
        assertEquals(zero, listaACriada.getDescontoReais());
        assertEquals(zero, listaACriada.getTotalLiquido());

        // ==========================================
        // 2. CA-02: Renomeação Dinâmica da Lista
        // ==========================================
        RenomearListaPedidoDTO renomearDTO = new RenomearListaPedidoDTO("Pedido Final Aprovado - Diretoria");
        ListaPedidoResponseDTO listaARenomeada = listaPedidoService.renomear(listaACriada.getId(), renomearDTO);

        assertEquals("Pedido Final Aprovado - Diretoria", listaARenomeada.getTitulo());
        assertEquals(listaACriada.getId(), listaARenomeada.getId());
        assertEquals(fornecedorCriado.getId(), listaARenomeada.getEmpresaId());

        // ==========================================
        // 3. CA-03 & RN-02: Isolamento Estrito de Itens por Lista
        // ==========================================
        CriarListaPedidoDTO listaBDTO = new CriarListaPedidoDTO(fornecedorCriado.getId(), "Lista B");
        ListaPedidoResponseDTO listaBCriada = listaPedidoService.criar(listaBDTO);

        // Adiciona item na Lista A
        CriarItemPedidoDTO itemFiltroDTO = new CriarItemPedidoDTO(
                "Filtro de Óleo Lubrificante",
                new BigDecimal("10"),
                new BigDecimal("35.00")
        );
        ItemPedidoResponseDTO itemA = listaPedidoService.adicionarItem(listaACriada.getId(), itemFiltroDTO);
        assertNotNull(itemA.getId());
        assertEquals(new BigDecimal("350.00").setScale(2, RoundingMode.HALF_UP), itemA.getSubtotal());

        // Lista B não deve conter o item adicionado na Lista A
        List<ItemPedidoResponseDTO> itensListaB = listaPedidoService.listarItens(listaBCriada.getId());
        assertTrue(itensListaB.isEmpty());

        List<ItemPedidoResponseDTO> itensListaA = listaPedidoService.listarItens(listaACriada.getId());
        assertEquals(1, itensListaA.size());
        assertEquals("Filtro de Óleo Lubrificante", itensListaA.get(0).getDescricao());

        // ==========================================
        // 4. CA-04: Cálculo Correto de Subtotais dos Itens e da Lista
        // ==========================================
        CriarListaPedidoDTO listaItensDTO = new CriarListaPedidoDTO(fornecedorCriado.getId(), "Lista de Ferramentas");
        ListaPedidoResponseDTO listaFerramentas = listaPedidoService.criar(listaItensDTO);

        // Item 1: Parafuso Sextavado (100 x 0,50 = 50,00)
        ItemPedidoResponseDTO parafuso = listaPedidoService.adicionarItem(
                listaFerramentas.getId(),
                new CriarItemPedidoDTO("Parafuso Sextavado", new BigDecimal("100"), new BigDecimal("0.50"))
        );
        assertEquals(new BigDecimal("50.00").setScale(2, RoundingMode.HALF_UP), parafuso.getSubtotal());

        // Item 2: Chave de Boca 14mm (2 x 25,00 = 50,00)
        ItemPedidoResponseDTO chave = listaPedidoService.adicionarItem(
                listaFerramentas.getId(),
                new CriarItemPedidoDTO("Chave de Boca 14mm", new BigDecimal("2"), new BigDecimal("25.00"))
        );
        assertEquals(new BigDecimal("50.00").setScale(2, RoundingMode.HALF_UP), chave.getSubtotal());

        // Consulta lista de ferramentas atualizada
        ListaPedidoResponseDTO listaFerramentasAtualizada = listaPedidoService.buscarPorId(listaFerramentas.getId());
        assertEquals(new BigDecimal("100.00").setScale(2, RoundingMode.HALF_UP), listaFerramentasAtualizada.getSubtotalItens());
        assertEquals(new BigDecimal("100.00").setScale(2, RoundingMode.HALF_UP), listaFerramentasAtualizada.getTotalLiquido());

        // ==========================================
        // 5. CA-05: Aplicação de Desconto Fixo com Função max(0, Subtotal - Desconto)
        // ==========================================
        CriarListaPedidoDTO listaDescontoDTO = new CriarListaPedidoDTO(fornecedorCriado.getId(), "Lista Desconto");
        ListaPedidoResponseDTO listaDesconto = listaPedidoService.criar(listaDescontoDTO);

        listaPedidoService.adicionarItem(
                listaDesconto.getId(),
                new CriarItemPedidoDTO("Item Base", BigDecimal.ONE, new BigDecimal("200.00"))
        );

        // Aplica desconto de R$ 50,00 -> Total Líquido R$ 150,00
        ListaPedidoResponseDTO comDesconto50 = listaPedidoService.aplicarDesconto(
                listaDesconto.getId(),
                new AplicarDescontoDTO(new BigDecimal("50.00"))
        );
        assertEquals(new BigDecimal("200.00").setScale(2, RoundingMode.HALF_UP), comDesconto50.getSubtotalItens());
        assertEquals(new BigDecimal("50.00").setScale(2, RoundingMode.HALF_UP), comDesconto50.getDescontoReais());
        assertEquals(new BigDecimal("150.00").setScale(2, RoundingMode.HALF_UP), comDesconto50.getTotalLiquido());

        // Aplica desconto superior ao subtotal (R$ 250,00) -> Piso zero R$ 0,00
        ListaPedidoResponseDTO comDesconto250 = listaPedidoService.aplicarDesconto(
                listaDesconto.getId(),
                new AplicarDescontoDTO(new BigDecimal("250.00"))
        );
        assertEquals(new BigDecimal("200.00").setScale(2, RoundingMode.HALF_UP), comDesconto250.getSubtotalItens());
        assertEquals(new BigDecimal("250.00").setScale(2, RoundingMode.HALF_UP), comDesconto250.getDescontoReais());
        assertEquals(zero, comDesconto250.getTotalLiquido());
        assertTrue(comDesconto250.getTotalLiquido().compareTo(BigDecimal.ZERO) >= 0);

        // Tentativa de desconto negativo deve lançar ValidacaoException (RN-05)
        assertThrows(ValidacaoException.class, () ->
                listaPedidoService.aplicarDesconto(listaDesconto.getId(), new AplicarDescontoDTO(new BigDecimal("-15.00")))
        );

        // ==========================================
        // 6. CA-06: Exportação Completa Formatada em Markdown
        // ==========================================
        FornecedorResponseDTO fornecedorCampo = fornecedorService.criar(new CriarFornecedorDTO(
                "Alimentos do Campo S.A.", "Alimentos do Campo", "11.222.333/0001-44", "contato@campo.com"
        ));

        ListaPedidoResponseDTO listaExportacao = listaPedidoService.criar(new CriarListaPedidoDTO(
                fornecedorCampo.getId(), "Pedido de Alimentos"
        ));

        listaPedidoService.adicionarItem(
                listaExportacao.getId(),
                new CriarItemPedidoDTO("Item Alimento", BigDecimal.ONE, new BigDecimal("80.00"))
        );

        listaPedidoService.aplicarDesconto(
                listaExportacao.getId(),
                new AplicarDescontoDTO(new BigDecimal("10.00"))
        );

        String markdown = listaPedidoService.exportarMarkdown(listaExportacao.getId());

        assertNotNull(markdown);
        assertTrue(markdown.contains("Alimentos do Campo S.A."));
        assertTrue(markdown.contains("Descrição"));
        assertTrue(markdown.contains("Qtd"));
        assertTrue(markdown.contains("Preço Unitário (R$)"));
        assertTrue(markdown.contains("Subtotal (R$)"));
        assertTrue(markdown.contains("R$ 80,00")); // Subtotal
        assertTrue(markdown.contains("R$ 10,00")); // Desconto
        assertTrue(markdown.contains("R$ 70,00")); // Total Líquido
    }
}
