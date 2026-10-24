package com.msaas.back.comerciante.application.dto;

public record RegistroComercianteResponse(
        Long id,
        String nomeEstabelecimento,
        String email,
        String mensagem
) {
}
