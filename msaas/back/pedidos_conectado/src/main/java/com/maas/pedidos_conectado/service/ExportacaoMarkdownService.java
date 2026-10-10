package com.maas.pedidos_conectado.service;

import com.maas.pedidos_conectado.domain.Fornecedor;
import com.maas.pedidos_conectado.domain.ItemPedido;
import com.maas.pedidos_conectado.domain.ListaPedido;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ExportacaoMarkdownService {

    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneOffset.UTC);

    private static final Locale LOCALE_BR = Locale.forLanguageTag("pt-BR");

    /**
     * RN-07: Gera string em formato Markdown contendo:
     * 1. Cabeçalho com o nome/título da lista e data de criação.
     * 2. Informações da empresa fornecedora (razão social, CNPJ e contato).
     * 3. Tabela com as colunas: Item, Descrição, Qtd, Preço Unitário (R$), Subtotal (R$).
     * 4. Bloco de resumo financeiro discriminando: Subtotal dos Itens, Desconto Aplicado (R$) e Total Final Líquido (R$).
     */
    public String gerarMarkdown(ListaPedido lista, Fornecedor fornecedor, List<ItemPedido> itens,
                                BigDecimal subtotalGeral, BigDecimal totalLiquido) {
        StringBuilder sb = new StringBuilder();

        // 1. Cabeçalho
        sb.append("# Pedido de Compra: ").append(lista.getTitulo()).append("\n\n");
        sb.append("**Data de Criação:** ").append(DATA_FORMATTER.format(lista.getDataCriacao())).append(" (UTC)\n\n");

        // 2. Informações do Fornecedor
        sb.append("## Informações da Empresa Fornecedora\n");
        sb.append("- **Razão Social:** ").append(fornecedor.getRazaoSocial()).append("\n");
        String cnpj = fornecedor.getCnpj().isBlank() ? "Não informado" : fornecedor.getCnpj();
        sb.append("- **CNPJ:** ").append(cnpj).append("\n");
        String contato = fornecedor.getContato().isBlank() ? "Não informado" : fornecedor.getContato();
        sb.append("- **Contato:** ").append(contato).append("\n\n");

        // 3. Tabela de Itens
        sb.append("## Itens do Pedido\n\n");
        sb.append("| Item | Descrição | Qtd | Preço Unitário (R$) | Subtotal (R$) |\n");
        sb.append("| :---: | :--- | :---: | :---: | :---: |\n");

        if (itens == null || itens.isEmpty()) {
            sb.append("| - | *(Nenhum item adicionado)* | - | - | - |\n\n");
        } else {
            for (int i = 0; i < itens.size(); i++) {
                ItemPedido item = itens.get(i);
                sb.append("| ")
                        .append(i + 1).append(" | ")
                        .append(item.getDescricao()).append(" | ")
                        .append(formatarQuantidade(item.getQuantidade())).append(" | ")
                        .append(formatarMoeda(item.getPrecoUnitario())).append(" | ")
                        .append(formatarMoeda(item.getSubtotal())).append(" |\n");
            }
            sb.append("\n");
        }

        // 4. Bloco de Resumo Financeiro
        sb.append("## Resumo Financeiro\n");
        sb.append("- **Subtotal dos Itens (Subtotal):** ").append(formatarMoeda(subtotalGeral)).append("\n");
        sb.append("- **Desconto Aplicado (R$) (Desconto):** ").append(formatarMoeda(lista.getDescontoReais())).append("\n");
        sb.append("- **Total Final Líquido (R$) (Total Líquido):** ").append(formatarMoeda(totalLiquido)).append("\n");

        return sb.toString();
    }

    public static String formatarMoeda(BigDecimal valor) {
        BigDecimal v = (valor != null) ? valor.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(LOCALE_BR);
        simbolos.setDecimalSeparator(',');
        simbolos.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,##0.00", simbolos);
        return "R$ " + df.format(v);
    }

    public static String formatarQuantidade(BigDecimal quantidade) {
        if (quantidade == null) {
            return "0";
        }
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(LOCALE_BR);
        simbolos.setDecimalSeparator(',');
        simbolos.setGroupingSeparator('.');
        if (quantidade.stripTrailingZeros().scale() <= 0) {
            DecimalFormat df = new DecimalFormat("#,##0", simbolos);
            return df.format(quantidade);
        } else {
            DecimalFormat df = new DecimalFormat("#,##0.##", simbolos);
            return df.format(quantidade);
        }
    }
}
