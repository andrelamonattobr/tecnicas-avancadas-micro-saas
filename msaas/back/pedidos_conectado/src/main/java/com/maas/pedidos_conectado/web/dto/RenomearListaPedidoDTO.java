package com.maas.pedidos_conectado.web.dto;

public class RenomearListaPedidoDTO {
    private String titulo;

    public RenomearListaPedidoDTO() {
    }

    public RenomearListaPedidoDTO(String titulo) {
        this.titulo = titulo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
}
