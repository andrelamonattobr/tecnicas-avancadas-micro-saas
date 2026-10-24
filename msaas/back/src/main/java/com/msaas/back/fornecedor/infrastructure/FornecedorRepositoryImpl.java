package com.msaas.back.fornecedor.infrastructure;

import com.msaas.back.fornecedor.domain.Fornecedor;
import com.msaas.back.fornecedor.domain.FornecedorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class FornecedorRepositoryImpl implements FornecedorRepository {

    private final SpringDataFornecedorRepository jpaRepository;

    public FornecedorRepositoryImpl(SpringDataFornecedorRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Fornecedor salvar(Fornecedor fornecedor) {
        return jpaRepository.save(fornecedor);
    }

    @Override
    public Optional<Fornecedor> buscarPorIdEComercianteId(Long id, Long comercianteId) {
        return jpaRepository.findByIdAndComercianteId(id, comercianteId);
    }

    @Override
    public boolean existePorComercianteIdETelefoneWhatsapp(Long comercianteId, String telefoneWhatsapp) {
        return jpaRepository.existsByComercianteIdAndTelefoneWhatsapp(comercianteId, telefoneWhatsapp);
    }

    @Override
    public boolean existePorComercianteIdETelefoneWhatsappExcluindoId(
            Long comercianteId,
            String telefoneWhatsapp,
            Long idExcluido
    ) {
        return jpaRepository.existsByComercianteIdAndTelefoneWhatsappAndIdNot(
                comercianteId,
                telefoneWhatsapp,
                idExcluido
        );
    }

    @Override
    public boolean existePorComercianteIdEDocumento(Long comercianteId, String documento) {
        return jpaRepository.existsByComercianteIdAndDocumento(comercianteId, documento);
    }

    @Override
    public Page<Fornecedor> listarPorComerciante(
            Long comercianteId,
            boolean ativo,
            String busca,
            Pageable pageable
    ) {
        return jpaRepository.buscarPaginadoPorComerciante(comercianteId, ativo, busca, pageable);
    }
}
