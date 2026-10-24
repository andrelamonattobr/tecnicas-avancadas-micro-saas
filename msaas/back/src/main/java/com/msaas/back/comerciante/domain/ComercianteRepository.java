package com.msaas.back.comerciante.domain;

import java.util.Optional;

public interface ComercianteRepository {

    Comerciante salvar(Comerciante comerciante);

    Optional<Comerciante> buscarPorId(Long id);

    Optional<Comerciante> buscarPorEmailIgnoreCase(String email);

    boolean existePorEmailIgnoreCase(String email);

    boolean existePorDocumento(String documento);
}
