package com.msaas.back.comerciante.infrastructure;

import com.msaas.back.comerciante.domain.Comerciante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataComercianteRepository extends JpaRepository<Comerciante, Long> {

    Optional<Comerciante> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDocumento(String documento);
}
