package com.msaas.back.comerciante.application.dto;

public record AuthResponse(
        Long id,
        String nomeEstabelecimento,
        String email,
        String token
) {
}
