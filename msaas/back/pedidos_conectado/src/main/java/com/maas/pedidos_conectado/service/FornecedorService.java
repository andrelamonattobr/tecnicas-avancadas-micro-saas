package com.maas.pedidos_conectado.service;

import com.maas.pedidos_conectado.domain.Fornecedor;
import com.maas.pedidos_conectado.domain.exception.EntidadeNaoEncontradaException;
import com.maas.pedidos_conectado.domain.exception.ValidacaoException;
import com.maas.pedidos_conectado.repository.FornecedorRepository;
import com.maas.pedidos_conectado.web.dto.CriarFornecedorDTO;
import com.maas.pedidos_conectado.web.dto.FornecedorResponseDTO;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    public FornecedorService(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    @Transactional
    public FornecedorResponseDTO criar(CriarFornecedorDTO dto) {
        if (dto == null) {
            throw new ValidacaoException("Dados do fornecedor não informados.");
        }
        Fornecedor fornecedor = Fornecedor.criar(
                dto.getRazaoSocial(),
                dto.getNomeFantasia(),
                dto.getCnpj(),
                dto.getContato()
        );
        Fornecedor salvo = fornecedorRepository.save(fornecedor);
        return FornecedorResponseDTO.fromEntity(salvo);
    }

    @Transactional(readOnly = true)
    public FornecedorResponseDTO buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            throw new ValidacaoException("ID do fornecedor não informado.");
        }
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Fornecedor não encontrado com ID: " + id));
        return FornecedorResponseDTO.fromEntity(fornecedor);
    }

    @Transactional(readOnly = true)
    public List<FornecedorResponseDTO> listarTodos() {
        return fornecedorRepository.findAll()
                .stream()
                .map(FornecedorResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean existePorId(String id) {
        return id != null && !id.isBlank() && fornecedorRepository.existsById(id);
    }
}
