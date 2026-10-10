package com.maas.pedidos_conectado.domain;

import com.maas.pedidos_conectado.domain.exception.ValidacaoException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tb_fornecedor")
public class Fornecedor {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "razao_social", length = 120, nullable = false)
    private String razaoSocial;

    @Column(name = "nome_fantasia", length = 100)
    private String nomeFantasia;

    @Column(name = "cnpj", length = 20)
    private String cnpj;

    @Column(name = "contato", length = 100)
    private String contato;

    protected Fornecedor() {
        // Construtor padrão exigido pelo JPA
    }

    public Fornecedor(String id, String razaoSocial, String nomeFantasia, String cnpj, String contato) {
        this.id = (id != null && !id.isBlank()) ? id : UUID.randomUUID().toString();
        setRazaoSocial(razaoSocial);
        setNomeFantasia(nomeFantasia);
        setCnpj(cnpj);
        setContato(contato);
    }

    public static Fornecedor criar(String razaoSocial, String nomeFantasia, String cnpj, String contato) {
        return new Fornecedor(UUID.randomUUID().toString(), razaoSocial, nomeFantasia, cnpj, contato);
    }

    public String getId() {
        return id;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        if (razaoSocial == null || razaoSocial.trim().length() < 3 || razaoSocial.trim().length() > 120) {
            throw new ValidacaoException("Razão social é obrigatória e deve ter entre 3 e 120 caracteres.");
        }
        this.razaoSocial = razaoSocial.trim();
    }

    public String getNomeFantasia() {
        return nomeFantasia != null ? nomeFantasia : "";
    }

    public void setNomeFantasia(String nomeFantasia) {
        if (nomeFantasia != null && nomeFantasia.trim().length() > 100) {
            throw new ValidacaoException("Nome fantasia não pode exceder 100 caracteres.");
        }
        this.nomeFantasia = (nomeFantasia != null) ? nomeFantasia.trim() : "";
    }

    public String getCnpj() {
        return cnpj != null ? cnpj : "";
    }

    public void setCnpj(String cnpj) {
        if (cnpj != null && cnpj.trim().length() > 20) {
            throw new ValidacaoException("CNPJ não pode exceder 20 caracteres.");
        }
        this.cnpj = (cnpj != null) ? cnpj.trim() : "";
    }

    public String getContato() {
        return contato != null ? contato : "";
    }

    public void setContato(String contato) {
        if (contato != null && contato.trim().length() > 100) {
            throw new ValidacaoException("Contato não pode exceder 100 caracteres.");
        }
        this.contato = (contato != null) ? contato.trim() : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Fornecedor that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
