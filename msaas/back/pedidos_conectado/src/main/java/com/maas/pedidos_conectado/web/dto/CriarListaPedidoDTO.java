package com.maas.pedidos_conectado.web.dto;

public class CriarListaPedidoDTO {
    private String empresaId;
    private String titulo;

    public CriarListaPedidoDTO() {
    }

    public CriarListaPedidoDTO(String empresaId, String titulo) {
        this.empresaId = empresaId;
        this.titulo = titulo;
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(String empresaId) {
        this.empresaId = empresaId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
}
