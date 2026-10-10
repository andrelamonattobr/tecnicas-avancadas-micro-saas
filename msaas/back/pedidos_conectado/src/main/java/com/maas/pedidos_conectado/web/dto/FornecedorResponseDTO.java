package com.maas.pedidos_conectado.web.dto;

import com.maas.pedidos_conectado.domain.Fornecedor;

public class FornecedorResponseDTO {
    private String id;
    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String contato;

    public FornecedorResponseDTO() {
    }

    public FornecedorResponseDTO(String id, String razaoSocial, String nomeFantasia, String cnpj, String contato) {
        this.id = id;
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.cnpj = cnpj;
        this.contato = contato;
    }

    public static FornecedorResponseDTO fromEntity(Fornecedor fornecedor) {
        if (fornecedor == null) return null;
        return new FornecedorResponseDTO(
                fornecedor.getId(),
                fornecedor.getRazaoSocial(),
                fornecedor.getNomeFantasia(),
                fornecedor.getCnpj(),
                fornecedor.getContato()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }
}
