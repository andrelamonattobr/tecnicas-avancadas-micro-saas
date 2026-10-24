package com.msaas.back.comerciante.application.dto;

public record RegistroComercianteRequest(
        String nomeResponsavel,
        String nomeEstabelecimento,
        String email,
        String senha,
        String telefone,
        String documento
) {
}
