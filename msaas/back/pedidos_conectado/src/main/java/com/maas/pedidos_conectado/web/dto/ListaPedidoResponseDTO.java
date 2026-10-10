package com.maas.pedidos_conectado.web.dto;

import com.maas.pedidos_conectado.domain.ListaPedido;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ListaPedidoResponseDTO {
    private String id;
    private String empresaId;
    private String titulo;
    private Instant dataCriacao;
    private BigDecimal subtotalItens;
    private BigDecimal descontoReais;
    private BigDecimal totalLiquido;
    private List<ItemPedidoResponseDTO> itens = new ArrayList<>();

    public ListaPedidoResponseDTO() {
    }

    public ListaPedidoResponseDTO(String id, String empresaId, String titulo, Instant dataCriacao,
                                  BigDecimal subtotalItens, BigDecimal descontoReais, BigDecimal totalLiquido) {
        this(id, empresaId, titulo, dataCriacao, subtotalItens, descontoReais, totalLiquido, new ArrayList<>());
    }

    public ListaPedidoResponseDTO(String id, String empresaId, String titulo, Instant dataCriacao,
                                  BigDecimal subtotalItens, BigDecimal descontoReais, BigDecimal totalLiquido,
                                  List<ItemPedidoResponseDTO> itens) {
        this.id = id;
        this.empresaId = empresaId;
        this.titulo = titulo;
        this.dataCriacao = dataCriacao;
        this.subtotalItens = (subtotalItens != null) ? subtotalItens.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.descontoReais = (descontoReais != null) ? descontoReais.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.totalLiquido = (totalLiquido != null) ? totalLiquido.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.itens = (itens != null) ? itens : new ArrayList<>();
    }

    public static ListaPedidoResponseDTO fromEntity(ListaPedido lista, BigDecimal subtotalItens, BigDecimal totalLiquido) {
        return fromEntity(lista, subtotalItens, totalLiquido, new ArrayList<>());
    }

    public static ListaPedidoResponseDTO fromEntity(ListaPedido lista, BigDecimal subtotalItens, BigDecimal totalLiquido, List<ItemPedidoResponseDTO> itens) {
        if (lista == null) return null;
        BigDecimal desc = lista.getDescontoReais();
        return new ListaPedidoResponseDTO(
                lista.getId(),
                lista.getEmpresaId(),
                lista.getTitulo(),
                lista.getDataCriacao(),
                subtotalItens != null ? subtotalItens : BigDecimal.ZERO,
                desc != null ? desc : BigDecimal.ZERO,
                totalLiquido != null ? totalLiquido : BigDecimal.ZERO,
                itens != null ? itens : new ArrayList<>()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Instant getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Instant dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public BigDecimal getSubtotalItens() {
        return subtotalItens;
    }

    public void setSubtotalItens(BigDecimal subtotalItens) {
        this.subtotalItens = subtotalItens;
    }

    public BigDecimal getDescontoReais() {
        return descontoReais;
    }

    public void setDescontoReais(BigDecimal descontoReais) {
        this.descontoReais = descontoReais;
    }

    public BigDecimal getTotalLiquido() {
        return totalLiquido;
    }

    public void setTotalLiquido(BigDecimal totalLiquido) {
        this.totalLiquido = totalLiquido;
    }

    public List<ItemPedidoResponseDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoResponseDTO> itens) {
        this.itens = itens;
    }
}
