package com.maas.pedidos_conectado.web.dto;

import java.math.BigDecimal;

public class CriarItemPedidoDTO {
    private String descricao;
    private BigDecimal quantidade;
    private BigDecimal precoUnitario;

    public CriarItemPedidoDTO() {
    }

    public CriarItemPedidoDTO(String descricao, BigDecimal quantidade, BigDecimal precoUnitario) {
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }
}
