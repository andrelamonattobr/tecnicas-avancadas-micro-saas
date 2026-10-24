package com.msaas.back.fornecedor.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface FornecedorRepository {

    Fornecedor salvar(Fornecedor fornecedor);

    Optional<Fornecedor> buscarPorIdEComercianteId(Long id, Long comercianteId);

    boolean existePorComercianteIdETelefoneWhatsapp(Long comercianteId, String telefoneWhatsapp);

    boolean existePorComercianteIdETelefoneWhatsappExcluindoId(Long comercianteId, String telefoneWhatsapp, Long idExcluido);

    boolean existePorComercianteIdEDocumento(Long comercianteId, String documento);

    Page<Fornecedor> listarPorComerciante(Long comercianteId, boolean ativo, String busca, Pageable pageable);
}
