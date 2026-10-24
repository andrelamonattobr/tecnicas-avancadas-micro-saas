package com.msaas.back.shared.infrastructure.security;

public record ComercianteAutenticado(
        Long id,
        String email,
        String nomeEstabelecimento
) {
}
