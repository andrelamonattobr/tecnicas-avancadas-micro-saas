package com.maas.pedidos_conectado.service;

import com.maas.pedidos_conectado.domain.ItemPedido;
import com.maas.pedidos_conectado.domain.ListaPedido;
import com.maas.pedidos_conectado.domain.exception.EntidadeNaoEncontradaException;
import com.maas.pedidos_conectado.domain.exception.ValidacaoException;
import com.maas.pedidos_conectado.repository.FornecedorRepository;
import com.maas.pedidos_conectado.repository.ItemPedidoRepository;
import com.maas.pedidos_conectado.repository.ListaPedidoRepository;
import com.maas.pedidos_conectado.web.dto.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListaPedidoService {

    private final ListaPedidoRepository listaPedidoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ExportacaoMarkdownService exportacaoMarkdownService;

    public ListaPedidoService(ListaPedidoRepository listaPedidoRepository,
                              FornecedorRepository fornecedorRepository,
                              ItemPedidoRepository itemPedidoRepository,
                              ExportacaoMarkdownService exportacaoMarkdownService) {
        this.listaPedidoRepository = listaPedidoRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.exportacaoMarkdownService = exportacaoMarkdownService;
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
        BigDecimal zero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return ListaPedidoResponseDTO.fromEntity(salva, zero, zero, List.of());
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

        return montarResponseDTO(atualizada);
    }

    @Transactional(readOnly = true)
    public ListaPedidoResponseDTO buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            throw new ValidacaoException("ID da lista não informado.");
        }
        ListaPedido lista = listaPedidoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Lista de pedido não encontrada com ID: " + id));

        return montarResponseDTO(lista);
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
                .map(this::montarResponseDTO)
                .toList();
    }

    // --- Métodos do Ciclo 2: Gestão de Itens e Cálculos Financeiros ---

    @Transactional
    public ItemPedidoResponseDTO adicionarItem(String listaId, CriarItemPedidoDTO dto) {
        if (listaId == null || listaId.isBlank()) {
            throw new ValidacaoException("ID da lista não informado.");
        }
        if (!listaPedidoRepository.existsById(listaId)) {
            throw new EntidadeNaoEncontradaException("Lista de pedido não encontrada com ID: " + listaId);
        }
        if (dto == null) {
            throw new ValidacaoException("Dados do item não informados.");
        }

        // RN-02: Item exclusivo da lista; RN-04: Subtotal = quantidade * preco_unitario
        ItemPedido item = ItemPedido.criar(
                listaId,
                dto.getDescricao(),
                dto.getQuantidade(),
                dto.getPrecoUnitario()
        );

        ItemPedido salvo = itemPedidoRepository.save(item);
        return ItemPedidoResponseDTO.fromEntity(salvo);
    }

    @Transactional(readOnly = true)
    public List<ItemPedidoResponseDTO> listarItens(String listaId) {
        if (listaId == null || listaId.isBlank()) {
            throw new ValidacaoException("ID da lista não informado.");
        }
        if (!listaPedidoRepository.existsById(listaId)) {
            throw new EntidadeNaoEncontradaException("Lista de pedido não encontrada com ID: " + listaId);
        }

        // RN-02: Isolamento estrito por lista
        List<ItemPedido> itens = itemPedidoRepository.findByListaId(listaId);
        return itens.stream()
                .map(ItemPedidoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public void removerItem(String listaId, String itemId) {
        if (listaId == null || listaId.isBlank()) {
            throw new ValidacaoException("ID da lista não informado.");
        }
        if (!listaPedidoRepository.existsById(listaId)) {
            throw new EntidadeNaoEncontradaException("Lista de pedido não encontrada com ID: " + listaId);
        }
        if (itemId == null || itemId.isBlank()) {
            throw new ValidacaoException("ID do item não informado.");
        }

        ItemPedido item = itemPedidoRepository.findById(itemId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Item não encontrado com ID: " + itemId));

        if (!listaId.equals(item.getListaId())) {
            throw new ValidacaoException("Item '" + itemId + "' não pertence à lista '" + listaId + "'.");
        }

        itemPedidoRepository.delete(item);
    }

    @Transactional
    public ListaPedidoResponseDTO aplicarDesconto(String listaId, AplicarDescontoDTO dto) {
        if (listaId == null || listaId.isBlank()) {
            throw new ValidacaoException("ID da lista não informado.");
        }
        ListaPedido lista = listaPedidoRepository.findById(listaId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Lista de pedido não encontrada com ID: " + listaId));

        if (dto == null || dto.getDescontoReais() == null) {
            throw new ValidacaoException("Valor do desconto não informado.");
        }

        // RN-05: Desconto fixo em reais deve ser não negativo
        lista.setDescontoReais(dto.getDescontoReais());
        ListaPedido atualizada = listaPedidoRepository.save(lista);

        return montarResponseDTO(atualizada);
    }

    /**
     * RN-04: Subtotal Geral = Somatório dos subtotais dos itens
     */
    public BigDecimal calcularSubtotalGeral(List<ItemPedido> itens) {
        if (itens == null || itens.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * RN-06: Total Líquido = max(0, Subtotal Geral - Desconto em R$) com piso zero estrito
     */
    public BigDecimal calcularTotalLiquido(BigDecimal subtotalGeral, BigDecimal descontoReais) {
        BigDecimal subtotal = (subtotalGeral != null) ? subtotalGeral : BigDecimal.ZERO;
        BigDecimal desconto = (descontoReais != null) ? descontoReais : BigDecimal.ZERO;

        BigDecimal liquido = subtotal.subtract(desconto);
        if (liquido.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return liquido.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public String exportarMarkdown(String listaId) {
        if (listaId == null || listaId.isBlank()) {
            throw new ValidacaoException("ID da lista não informado.");
        }
        ListaPedido lista = listaPedidoRepository.findById(listaId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Lista de pedido não encontrada com ID: " + listaId));

        com.maas.pedidos_conectado.domain.Fornecedor fornecedor = fornecedorRepository.findById(lista.getEmpresaId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Empresa fornecedora vinculada não encontrada com ID: " + lista.getEmpresaId()));

        List<ItemPedido> itens = itemPedidoRepository.findByListaId(listaId);
        BigDecimal subtotalGeral = calcularSubtotalGeral(itens);
        BigDecimal totalLiquido = calcularTotalLiquido(subtotalGeral, lista.getDescontoReais());

        return exportacaoMarkdownService.gerarMarkdown(lista, fornecedor, itens, subtotalGeral, totalLiquido);
    }

    private ListaPedidoResponseDTO montarResponseDTO(ListaPedido lista) {
        List<ItemPedido> itens = itemPedidoRepository.findByListaId(lista.getId());
        BigDecimal subtotalGeral = calcularSubtotalGeral(itens);
        BigDecimal totalLiquido = calcularTotalLiquido(subtotalGeral, lista.getDescontoReais());

        List<ItemPedidoResponseDTO> itensDTO = itens.stream()
                .map(ItemPedidoResponseDTO::fromEntity)
                .toList();

        return ListaPedidoResponseDTO.fromEntity(lista, subtotalGeral, totalLiquido, itensDTO);
    }
}
