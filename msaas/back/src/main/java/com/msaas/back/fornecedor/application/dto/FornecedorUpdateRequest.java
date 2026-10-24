package com.msaas.back.fornecedor.application.dto;

public record FornecedorUpdateRequest(
        String nome,
        String nomeContato,
        String telefoneWhatsapp,
        String documento,
        String email,
        String categoria
) {
}
