package com.maas.pedidos_conectado.domain;

import com.maas.pedidos_conectado.domain.exception.ValidacaoException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tb_lista_pedido")
public class ListaPedido {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "empresa_id", length = 36, nullable = false)
    private String empresaId;

    @Column(name = "titulo", length = 100, nullable = false)
    private String titulo;

    @Column(name = "data_criacao", nullable = false)
    private Instant dataCriacao;

    @Column(name = "desconto_reais", precision = 12, scale = 2, nullable = false)
    private BigDecimal descontoReais;

    protected ListaPedido() {
        // Construtor padrão JPA
    }

    public ListaPedido(String id, String empresaId, String titulo, Instant dataCriacao, BigDecimal descontoReais) {
        this.id = (id != null && !id.isBlank()) ? id : UUID.randomUUID().toString();
        setEmpresaId(empresaId);
        setTitulo(titulo);
        this.dataCriacao = (dataCriacao != null) ? dataCriacao : Instant.now();
        setDescontoReais(descontoReais != null ? descontoReais : BigDecimal.ZERO);
    }

    public static ListaPedido criar(String empresaId, String titulo) {
        return new ListaPedido(UUID.randomUUID().toString(), empresaId, titulo, Instant.now(), BigDecimal.ZERO);
    }

    public void renomear(String novoTitulo) {
        setTitulo(novoTitulo);
    }

    public String getId() {
        return id;
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(String empresaId) {
        if (empresaId == null || empresaId.isBlank()) {
            throw new ValidacaoException("Identificador da empresa fornecedora é obrigatório.");
        }
        this.empresaId = empresaId.trim();
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        if (titulo == null || titulo.trim().length() < 3 || titulo.trim().length() > 100) {
            throw new ValidacaoException("Título da lista é obrigatório e deve ter entre 3 e 100 caracteres.");
        }
        this.titulo = titulo.trim();
    }

    public Instant getDataCriacao() {
        return dataCriacao;
    }

    public BigDecimal getDescontoReais() {
        return descontoReais != null ? descontoReais : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public void setDescontoReais(BigDecimal descontoReais) {
        if (descontoReais == null) {
            this.descontoReais = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            return;
        }
        if (descontoReais.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacaoException("Desconto em R$ não pode ser negativo.");
        }
        this.descontoReais = descontoReais.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListaPedido that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
