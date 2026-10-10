package com.maas.pedidos_conectado.web.dto;

import com.maas.pedidos_conectado.domain.ItemPedido;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ItemPedidoResponseDTO {
    private String id;
    private String listaId;
    private String descricao;
    private BigDecimal quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;

    public ItemPedidoResponseDTO() {
    }

    public ItemPedidoResponseDTO(String id, String listaId, String descricao,
                                 BigDecimal quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {
        this.id = id;
        this.listaId = listaId;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.precoUnitario = (precoUnitario != null) ? precoUnitario.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.subtotal = (subtotal != null) ? subtotal.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public static ItemPedidoResponseDTO fromEntity(ItemPedido item) {
        if (item == null) return null;
        return new ItemPedidoResponseDTO(
                item.getId(),
                item.getListaId(),
                item.getDescricao(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getSubtotal()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getListaId() {
        return listaId;
    }

    public void setListaId(String listaId) {
        this.listaId = listaId;
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

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
