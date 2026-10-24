package com.msaas.back.fornecedor.infrastructure;

import com.msaas.back.fornecedor.domain.Fornecedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataFornecedorRepository extends JpaRepository<Fornecedor, Long> {

    Optional<Fornecedor> findByIdAndComercianteId(Long id, Long comercianteId);

    boolean existsByComercianteIdAndTelefoneWhatsapp(Long comercianteId, String telefoneWhatsapp);

    boolean existsByComercianteIdAndTelefoneWhatsappAndIdNot(Long comercianteId, String telefoneWhatsapp, Long id);

    boolean existsByComercianteIdAndDocumento(Long comercianteId, String documento);

    @Query("""
            SELECT f FROM Fornecedor f
            WHERE f.comerciante.id = :comercianteId
              AND f.ativo = :ativo
              AND (:busca IS NULL OR :busca = ''
                   OR LOWER(f.nome) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR LOWER(COALESCE(f.categoria, '')) LIKE LOWER(CONCAT('%', :busca, '%')))
            ORDER BY f.nome ASC
            """)
    Page<Fornecedor> buscarPaginadoPorComerciante(
            @Param("comercianteId") Long comercianteId,
            @Param("ativo") boolean ativo,
            @Param("busca") String busca,
            Pageable pageable
    );
}
