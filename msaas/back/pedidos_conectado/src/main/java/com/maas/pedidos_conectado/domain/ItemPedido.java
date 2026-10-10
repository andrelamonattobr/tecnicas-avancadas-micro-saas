package com.maas.pedidos_conectado.domain;

import com.maas.pedidos_conectado.domain.exception.ValidacaoException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tb_item_pedido")
public class ItemPedido {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "lista_id", length = 36, nullable = false)
    private String listaId;

    @Column(name = "descricao", length = 120, nullable = false)
    private String descricao;

    @Column(name = "quantidade", precision = 12, scale = 4, nullable = false)
    private BigDecimal quantidade;

    @Column(name = "preco_unitario", precision = 12, scale = 2, nullable = false)
    private BigDecimal precoUnitario;

    protected ItemPedido() {
        // Construtor padrão JPA
    }

    public ItemPedido(String id, String listaId, String descricao, BigDecimal quantidade, BigDecimal precoUnitario) {
        this.id = (id != null && !id.isBlank()) ? id : UUID.randomUUID().toString();
        setListaId(listaId);
        setDescricao(descricao);
        setQuantidade(quantidade);
        setPrecoUnitario(precoUnitario);
    }

    public static ItemPedido criar(String listaId, String descricao, BigDecimal quantidade, BigDecimal precoUnitario) {
        return new ItemPedido(UUID.randomUUID().toString(), listaId, descricao, quantidade, precoUnitario);
    }

    /**
     * RN-04: SubtotalItem = quantidade * preco_unitario
     */
    public BigDecimal getSubtotal() {
        if (quantidade == null || precoUnitario == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return quantidade.multiply(precoUnitario).setScale(2, RoundingMode.HALF_UP);
    }

    public String getId() {
        return id;
    }

    public String getListaId() {
        return listaId;
    }

    public void setListaId(String listaId) {
        if (listaId == null || listaId.isBlank()) {
            throw new ValidacaoException("Identificador da lista de pedidos é obrigatório.");
        }
        this.listaId = listaId.trim();
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        if (descricao == null || descricao.trim().length() < 2 || descricao.trim().length() > 120) {
            throw new ValidacaoException("Descrição do item é obrigatória e deve ter entre 2 e 120 caracteres.");
        }
        this.descricao = descricao.trim();
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException("Quantidade do item deve ser estritamente maior que zero.");
        }
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario != null ? precoUnitario : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        if (precoUnitario == null || precoUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacaoException("Preço unitário não pode ser negativo.");
        }
        this.precoUnitario = precoUnitario.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemPedido that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
