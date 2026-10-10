package com.maas.pedidos_conectado.service;

import static org.junit.jupiter.api.Assertions.*;

import com.maas.pedidos_conectado.domain.Fornecedor;
import com.maas.pedidos_conectado.domain.ItemPedido;
import com.maas.pedidos_conectado.domain.ListaPedido;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExportacaoMarkdownServiceTest {

    private ExportacaoMarkdownService exportacaoMarkdownService;

    @BeforeEach
    void setUp() {
        exportacaoMarkdownService = new ExportacaoMarkdownService();
    }

    @Test
    @DisplayName("CA-06: Exportação Completa Formatada em Markdown com fornecedor, itens e resumo financeiro")
    void deveExportarPedidoCompletoFormatadoEmMarkdown_CA06() {
        // Dado um pedido com fornecedor "Alimentos do Campo S.A.", 1 item de R$ 80,00 e desconto de R$ 10,00
        Fornecedor fornecedor = new Fornecedor(
                "forn-1",
                "Alimentos do Campo S.A.",
                "Alimentos do Campo",
                "12.345.678/0001-99",
                "vendas@alimentosdocampo.com.br"
        );

        ListaPedido lista = new ListaPedido(
                "lista-1",
                "forn-1",
                "Pedido Mensal de Suprimentos",
                Instant.parse("2026-10-10T12:00:00Z"),
                new BigDecimal("10.00")
        );

        ItemPedido item = new ItemPedido(
                "item-1",
                "lista-1",
                "Pacote de Arroz 5kg",
                new BigDecimal("1"),
                new BigDecimal("80.00")
        );

        BigDecimal subtotal = new BigDecimal("80.00").setScale(2, RoundingMode.HALF_UP);
        BigDecimal desconto = new BigDecimal("10.00").setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalLiquido = new BigDecimal("70.00").setScale(2, RoundingMode.HALF_UP);

        // Quando eu requisitar a exportação do pedido para Markdown
        String markdown = exportacaoMarkdownService.gerarMarkdown(
                lista, fornecedor, List.of(item), subtotal, totalLiquido
        );

        assertNotNull(markdown);

        // Então o texto gerado contém a razão social "Alimentos do Campo S.A."
        assertTrue(markdown.contains("Alimentos do Campo S.A."), "Deve conter a razão social do fornecedor");

        // E contém o cabeçalho com o título da lista e data de criação (RN-07)
        assertTrue(markdown.contains("# Pedido de Compra: Pedido Mensal de Suprimentos"));
        assertTrue(markdown.contains("**Data de Criação:** 10/10/2026 12:00:00 (UTC)"));

        // E contém as informações do fornecedor (CNPJ e contato)
        assertTrue(markdown.contains("12.345.678/0001-99"));
        assertTrue(markdown.contains("vendas@alimentosdocampo.com.br"));

        // E contém a tabela formatada com colunas de Descrição, Qtd, Preço Unitário e Subtotal
        assertTrue(markdown.contains("Item"), "Tabela deve conter coluna Item");
        assertTrue(markdown.contains("Descrição"), "Tabela deve conter coluna Descrição");
        assertTrue(markdown.contains("Qtd"), "Tabela deve conter coluna Qtd");
        assertTrue(markdown.contains("Preço Unitário (R$)"), "Tabela deve conter coluna Preço Unitário (R$)");
        assertTrue(markdown.contains("Subtotal (R$)"), "Tabela deve conter coluna Subtotal (R$)");

        // Linha do item
        assertTrue(markdown.contains("Pacote de Arroz 5kg"));
        assertTrue(markdown.contains("R$ 80,00"));

        // E apresenta o resumo financeiro com Subtotal R$ 80,00, Desconto R$ 10,00 e Total Líquido R$ 70,00
        assertTrue(markdown.contains("Subtotal dos Itens"), "Resumo deve discriminar Subtotal dos Itens");
        assertTrue(markdown.contains("R$ 80,00"), "Subtotal deve ser R$ 80,00");

        assertTrue(markdown.contains("Desconto Aplicado (R$)"), "Resumo deve discriminar Desconto Aplicado (R$)");
        assertTrue(markdown.contains("R$ 10,00"), "Desconto deve ser R$ 10,00");

        assertTrue(markdown.contains("Total Final Líquido (R$)"), "Resumo deve discriminar Total Final Líquido (R$)");
        assertTrue(markdown.contains("R$ 70,00"), "Total Líquido deve ser R$ 70,00");
    }

    @Test
    @DisplayName("RN-07: Deve exportar lista com múltiplos itens formatando tabela e numeração sequencial")
    void deveExportarListaComMultiplosItens() {
        Fornecedor fornecedor = new Fornecedor("forn-1", "Ferragens Brasil Ltda", "", "", "");
        ListaPedido lista = new ListaPedido("l1", "forn-1", "Oficina Manutenção", Instant.now(), BigDecimal.ZERO);

        ItemPedido item1 = new ItemPedido("i1", "l1", "Parafuso Sextavado", new BigDecimal("100"), new BigDecimal("0.50"));
        ItemPedido item2 = new ItemPedido("i2", "l1", "Chave de Boca 14mm", new BigDecimal("2"), new BigDecimal("25.00"));

        BigDecimal subtotal = new BigDecimal("100.00");
        BigDecimal totalLiquido = new BigDecimal("100.00");

        String markdown = exportacaoMarkdownService.gerarMarkdown(
                lista, fornecedor, List.of(item1, item2), subtotal, totalLiquido
        );

        assertTrue(markdown.contains("| 1 | Parafuso Sextavado | 100 | R$ 0,50 | R$ 50,00 |"));
        assertTrue(markdown.contains("| 2 | Chave de Boca 14mm | 2 | R$ 25,00 | R$ 50,00 |"));
        assertTrue(markdown.contains("R$ 100,00"));
    }

    @Test
    @DisplayName("RN-07: Deve exportar lista vazia exibindo aviso na tabela e totais zerados")
    void deveExportarListaVazia() {
        Fornecedor fornecedor = new Fornecedor("forn-1", "Loja de Materiais Ltda", "", "", "");
        ListaPedido lista = new ListaPedido("l1", "forn-1", "Lista Vazia", Instant.now(), BigDecimal.ZERO);

        String markdown = exportacaoMarkdownService.gerarMarkdown(
                lista, fornecedor, List.of(), BigDecimal.ZERO, BigDecimal.ZERO
        );

        assertTrue(markdown.contains("*(Nenhum item adicionado)*"));
        assertTrue(markdown.contains("R$ 0,00"));
    }

    @Test
    @DisplayName("RN-06 e RN-07: Deve exportar lista com desconto superior ao subtotal mantendo piso zero R$ 0,00")
    void deveExportarListaComDescontoSuperiorComPisoZero() {
        Fornecedor fornecedor = new Fornecedor("forn-1", "Auto Peças Ltda", "", "", "");
        ListaPedido lista = new ListaPedido("l1", "forn-1", "Pedido Promocional", Instant.now(), new BigDecimal("150.00"));
        ItemPedido item = new ItemPedido("i1", "l1", "Óleo Motor", BigDecimal.ONE, new BigDecimal("100.00"));

        BigDecimal subtotal = new BigDecimal("100.00");
        BigDecimal totalLiquido = BigDecimal.ZERO; // max(0, 100 - 150) = 0

        String markdown = exportacaoMarkdownService.gerarMarkdown(
                lista, fornecedor, List.of(item), subtotal, totalLiquido
        );

        assertTrue(markdown.contains("R$ 100,00")); // Subtotal
        assertTrue(markdown.contains("R$ 150,00")); // Desconto
    }
}
