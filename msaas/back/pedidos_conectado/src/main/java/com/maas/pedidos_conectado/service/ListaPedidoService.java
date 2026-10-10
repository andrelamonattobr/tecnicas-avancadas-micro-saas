package com.maas.pedidos_conectado.service;

import com.maas.pedidos_conectado.domain.ListaPedido;
import com.maas.pedidos_conectado.domain.exception.EntidadeNaoEncontradaException;
import com.maas.pedidos_conectado.domain.exception.ValidacaoException;
import com.maas.pedidos_conectado.repository.FornecedorRepository;
import com.maas.pedidos_conectado.repository.ListaPedidoRepository;
import com.maas.pedidos_conectado.web.dto.CriarListaPedidoDTO;
import com.maas.pedidos_conectado.web.dto.ListaPedidoResponseDTO;
import com.maas.pedidos_conectado.web.dto.RenomearListaPedidoDTO;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListaPedidoService {

    private final ListaPedidoRepository listaPedidoRepository;
    private final FornecedorRepository fornecedorRepository;

    public ListaPedidoService(ListaPedidoRepository listaPedidoRepository, FornecedorRepository fornecedorRepository) {
        this.listaPedidoRepository = listaPedidoRepository;
        this.fornecedorRepository = fornecedorRepository;
    }

    @Transactional
    public ListaPedidoResponseDTO criar(CriarListaPedidoDTO dto) {
        if (dto == null) {
            throw new ValidacaoException("Dados da lista de pedido não informados.");
        }

        // RN-01: Vínculo Mandatório de Empresa
        String empresaId = dto.getEmpresaId();
        if (empresaId == null || empresaId.isBlank()) {
            throw new ValidacaoException("Identificador da empresa fornecedora é obrigatório.");
        }
        if (!fornecedorRepository.existsById(empresaId.trim())) {
            throw new ValidacaoException("Empresa fornecedora com ID '" + empresaId + "' não existe.");
        }

        ListaPedido novaLista = ListaPedido.criar(empresaId.trim(), dto.getTitulo());
        ListaPedido salva = listaPedidoRepository.save(novaLista);

        // CA-01: Status financeiro inicial indica subtotal R$ 0,00, desconto R$ 0,00 e total líquido R$ 0,00
        return ListaPedidoResponseDTO.fromEntity(salva, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Transactional
    public ListaPedidoResponseDTO renomear(String id, RenomearListaPedidoDTO dto) {
        if (id == null || id.isBlank()) {
            throw new ValidacaoException("ID da lista não informado.");
        }
        if (dto == null) {
            throw new ValidacaoException("Dados para renomeação não informados.");
        }

        ListaPedido lista = listaPedidoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Lista de pedido não encontrada com ID: " + id));

        // RN-03: Renomeação Dinâmica da Lista
        lista.renomear(dto.getTitulo());
        ListaPedido atualizada = listaPedidoRepository.save(lista);

        return ListaPedidoResponseDTO.fromEntity(atualizada, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public ListaPedidoResponseDTO buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            throw new ValidacaoException("ID da lista não informado.");
        }
        ListaPedido lista = listaPedidoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Lista de pedido não encontrada com ID: " + id));
        return ListaPedidoResponseDTO.fromEntity(lista, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public List<ListaPedidoResponseDTO> listar(String empresaId) {
        List<ListaPedido> listas;
        if (empresaId != null && !empresaId.isBlank()) {
            listas = listaPedidoRepository.findByEmpresaId(empresaId.trim());
        } else {
            listas = listaPedidoRepository.findAll();
        }
        return listas.stream()
                .map(l -> ListaPedidoResponseDTO.fromEntity(l, BigDecimal.ZERO, BigDecimal.ZERO))
                .toList();
    }
}
