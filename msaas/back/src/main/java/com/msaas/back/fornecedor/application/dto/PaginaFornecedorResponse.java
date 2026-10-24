package com.msaas.back.fornecedor.application.dto;

import java.util.List;

public record PaginaFornecedorResponse(
        List<FornecedorResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
