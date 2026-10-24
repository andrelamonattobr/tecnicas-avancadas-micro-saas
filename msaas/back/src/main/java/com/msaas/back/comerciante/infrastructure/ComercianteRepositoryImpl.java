package com.msaas.back.comerciante.infrastructure;

import com.msaas.back.comerciante.domain.Comerciante;
import com.msaas.back.comerciante.domain.ComercianteRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ComercianteRepositoryImpl implements ComercianteRepository {

    private final SpringDataComercianteRepository jpaRepository;

    public ComercianteRepositoryImpl(SpringDataComercianteRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Comerciante salvar(Comerciante comerciante) {
        return jpaRepository.save(comerciante);
    }

    @Override
    public Optional<Comerciante> buscarPorId(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Comerciante> buscarPorEmailIgnoreCase(String email) {
        return jpaRepository.findByEmailIgnoreCase(email);
    }

    @Override
    public boolean existePorEmailIgnoreCase(String email) {
        return jpaRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existePorDocumento(String documento) {
        return jpaRepository.existsByDocumento(documento);
    }
}
