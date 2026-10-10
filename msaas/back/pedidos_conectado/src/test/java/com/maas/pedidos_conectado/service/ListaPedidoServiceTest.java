package com.maas.pedidos_conectado.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.maas.pedidos_conectado.domain.ListaPedido;
import com.maas.pedidos_conectado.domain.exception.EntidadeNaoEncontradaException;
import com.maas.pedidos_conectado.domain.exception.ValidacaoException;
import com.maas.pedidos_conectado.repository.FornecedorRepository;
import com.maas.pedidos_conectado.repository.ListaPedidoRepository;
import com.maas.pedidos_conectado.web.dto.CriarListaPedidoDTO;
import com.maas.pedidos_conectado.web.dto.ListaPedidoResponseDTO;
import com.maas.pedidos_conectado.web.dto.RenomearListaPedidoDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListaPedidoServiceTest {

    @Mock
    private ListaPedidoRepository listaPedidoRepository;

    @Mock
    private FornecedorRepository fornecedorRepository;

    @InjectMocks
    private ListaPedidoService listaPedidoService;

    @Test
    @DisplayName("CA-01: Deve criar nova lista de pedido vinculada a fornecedor com status financeiro inicial zerado")
    void deveCriarListaComSucessoEStatusFinanceiroZeradoInicial_CA01() {
        // Dado que uma empresa fornecedora existe
        String empresaId = "empresa-001";
        when(fornecedorRepository.existsById(empresaId)).thenReturn(true);

        CriarListaPedidoDTO dto = new CriarListaPedidoDTO(empresaId, "Reposição Emergencial Outubro");

        ListaPedido listaSalva = new ListaPedido(
                "lista-uuid-1",
                empresaId,
                "Reposição Emergencial Outubro",
                Instant.now(),
                BigDecimal.ZERO
        );

        when(listaPedidoRepository.save(any(ListaPedido.class))).thenReturn(listaSalva);

        // Quando eu criar uma nova lista de pedido vinculada a esta empresa
        ListaPedidoResponseDTO resposta = listaPedidoService.criar(dto);

        // Então a lista é criada com sucesso possuindo um identificador único
        assertNotNull(resposta);
        assertEquals("lista-uuid-1", resposta.getId());
        assertEquals(empresaId, resposta.getEmpresaId());
        assertEquals("Reposição Emergencial Outubro", resposta.getTitulo());

        // E o status financeiro inicial indica subtotal R$ 0,00, desconto R$ 0,00 e total líquido R$ 0,00
        BigDecimal zeroEsperado = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        assertEquals(zeroEsperado, resposta.getSubtotalItens());
        assertEquals(zeroEsperado, resposta.getDescontoReais());
        assertEquals(zeroEsperado, resposta.getTotalLiquido());

        verify(fornecedorRepository, times(1)).existsById(empresaId);
        verify(listaPedidoRepository, times(1)).save(any(ListaPedido.class));
    }

    @Test
    @DisplayName("RN-01: Deve rejeitar criação de lista vinculada a empresa fornecedora inexistente")
    void deveRejeitarCriacaoDeListaVinculadaAEmpresaInexistente_RN01() {
        String empresaInexistenteId = "empresa-fantasma";
        when(fornecedorRepository.existsById(empresaInexistenteId)).thenReturn(false);

        CriarListaPedidoDTO dto = new CriarListaPedidoDTO(empresaInexistenteId, "Lista Válida");

        ValidacaoException ex = assertThrows(ValidacaoException.class, () -> listaPedidoService.criar(dto));
        assertTrue(ex.getMessage().contains("não existe"));

        verify(listaPedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar criação de lista quando empresaId for nulo ou em branco")
    void deveRejeitarCriacaoQuandoEmpresaIdForVazio() {
        CriarListaPedidoDTO dtoVazio = new CriarListaPedidoDTO("   ", "Título Válido");

        ValidacaoException ex = assertThrows(ValidacaoException.class, () -> listaPedidoService.criar(dtoVazio));
        assertTrue(ex.getMessage().contains("Identificador da empresa fornecedora é obrigatório"));

        CriarListaPedidoDTO dtoNulo = new CriarListaPedidoDTO(null, "Título Válido");
        assertThrows(ValidacaoException.class, () -> listaPedidoService.criar(dtoNulo));
    }

    @Test
    @DisplayName("Deve rejeitar criação de lista quando título tiver menos de 3 ou mais de 100 caracteres")
    void deveRejeitarCriacaoComTituloInvalido() {
        String empresaId = "empresa-001";
        when(fornecedorRepository.existsById(empresaId)).thenReturn(true);

        CriarListaPedidoDTO dtoCurto = new CriarListaPedidoDTO(empresaId, "AB");
        ValidacaoException exCurto = assertThrows(ValidacaoException.class, () -> listaPedidoService.criar(dtoCurto));
        assertTrue(exCurto.getMessage().contains("entre 3 e 100 caracteres"));

        String tituloLongo = "A".repeat(101);
        CriarListaPedidoDTO dtoLongo = new CriarListaPedidoDTO(empresaId, tituloLongo);
        ValidacaoException exLongo = assertThrows(ValidacaoException.class, () -> listaPedidoService.criar(dtoLongo));
        assertTrue(exLongo.getMessage().contains("entre 3 e 100 caracteres"));
    }

    @Test
    @DisplayName("CA-02: Deve renomear a lista dinamicamente preservando o vínculo com a empresa e ID")
    void deveRenomearListaDinamicaPreservandoEmpresaEId_CA02() {
        // Dado que existe uma lista de pedido ativa com o título "Pedido Rascunho"
        String listaId = "lista-xyz";
        String empresaIdOriginal = "empresa-001";
        Instant dataOriginal = Instant.now();
        ListaPedido listaExistente = new ListaPedido(listaId, empresaIdOriginal, "Pedido Rascunho", dataOriginal, BigDecimal.ZERO);

        when(listaPedidoRepository.findById(listaId)).thenReturn(Optional.of(listaExistente));
        when(listaPedidoRepository.save(any(ListaPedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Quando eu solicitar a renomeação do título para "Pedido Final Aprovado - Diretoria"
        RenomearListaPedidoDTO renomearDTO = new RenomearListaPedidoDTO("Pedido Final Aprovado - Diretoria");
        ListaPedidoResponseDTO resposta = listaPedidoService.renomear(listaId, renomearDTO);

        // Então o título da lista é atualizado para "Pedido Final Aprovado - Diretoria"
        assertNotNull(resposta);
        assertEquals("Pedido Final Aprovado - Diretoria", resposta.getTitulo());

        // E o identificador e o fornecedor vinculado permanecem inalterados
        assertEquals(listaId, resposta.getId());
        assertEquals(empresaIdOriginal, resposta.getEmpresaId());
        assertEquals(dataOriginal, resposta.getDataCriacao());

        verify(listaPedidoRepository, times(1)).save(listaExistente);
    }

    @Test
    @DisplayName("RN-03: Deve rejeitar renomeação quando novo título for inválido")
    void deveRejeitarRenomeacaoComTituloInvalido_RN03() {
        String listaId = "lista-xyz";
        ListaPedido listaExistente = new ListaPedido(listaId, "empresa-001", "Pedido Rascunho", Instant.now(), BigDecimal.ZERO);
        when(listaPedidoRepository.findById(listaId)).thenReturn(Optional.of(listaExistente));

        RenomearListaPedidoDTO dtoCurto = new RenomearListaPedidoDTO("Oi");
        ValidacaoException exCurto = assertThrows(ValidacaoException.class, () -> listaPedidoService.renomear(listaId, dtoCurto));
        assertTrue(exCurto.getMessage().contains("entre 3 e 100 caracteres"));

        RenomearListaPedidoDTO dtoLongo = new RenomearListaPedidoDTO("A".repeat(101));
        assertThrows(ValidacaoException.class, () -> listaPedidoService.renomear(listaId, dtoLongo));
    }

    @Test
    @DisplayName("Deve lançar EntidadeNaoEncontradaException ao tentar renomear lista inexistente")
    void deveLancarExcecaoAoRenomearListaInexistente() {
        when(listaPedidoRepository.findById("inexistente")).thenReturn(Optional.empty());

        RenomearListaPedidoDTO dto = new RenomearListaPedidoDTO("Novo Título");
        assertThrows(EntidadeNaoEncontradaException.class, () -> listaPedidoService.renomear("inexistente", dto));
    }
}
