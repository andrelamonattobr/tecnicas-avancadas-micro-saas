package com.msaas.back.fornecedor.application.dto;

import com.msaas.back.fornecedor.domain.Fornecedor;

import java.time.LocalDateTime;

public record FornecedorResponse(
        Long id,
        String nome,
        String nomeContato,
        String telefoneWhatsapp,
        String documento,
        String email,
        String categoria,
        boolean ativo,
        LocalDateTime dataCadastro,
        String mensagem
) {
    public static FornecedorResponse de(Fornecedor fornecedor, String mensagem) {
        return new FornecedorResponse(
                fornecedor.getId(),
                fornecedor.getNome(),
                fornecedor.getNomeContato(),
                fornecedor.getTelefoneWhatsapp(),
                fornecedor.getDocumento(),
                fornecedor.getEmail(),
                fornecedor.getCategoria(),
                fornecedor.isAtivo(),
                fornecedor.getDataCadastro(),
                mensagem
        );
    }
}
