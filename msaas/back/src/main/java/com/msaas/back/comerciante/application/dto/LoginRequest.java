package com.msaas.back.comerciante.application.dto;

public record LoginRequest(
        String email,
        String senha
) {
}
