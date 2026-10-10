package com.maas.pedidos_conectado.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.maas.pedidos_conectado.domain.Fornecedor;
import com.maas.pedidos_conectado.domain.exception.EntidadeNaoEncontradaException;
import com.maas.pedidos_conectado.domain.exception.ValidacaoException;
import com.maas.pedidos_conectado.repository.FornecedorRepository;
import com.maas.pedidos_conectado.web.dto.CriarFornecedorDTO;
import com.maas.pedidos_conectado.web.dto.FornecedorResponseDTO;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FornecedorServiceTest {

    @Mock
    private FornecedorRepository fornecedorRepository;

    @InjectMocks
    private FornecedorService fornecedorService;

    private CriarFornecedorDTO dtoValido;

    @BeforeEach
    void setUp() {
        dtoValido = new CriarFornecedorDTO(
                "Distribuidora Nacional de Peças Ltda",
                "Distribuidora Nacional",
                "12.345.678/0001-90",
                "contato@pecas.com.br"
        );
    }

    @Test
    @DisplayName("Deve cadastrar fornecedor com sucesso quando dados válidos forem fornecidos")
    void deveCadastrarFornecedorComSucesso() {
        Fornecedor fornecedorSalvo = new Fornecedor(
                "f123",
                dtoValido.getRazaoSocial(),
                dtoValido.getNomeFantasia(),
                dtoValido.getCnpj(),
                dtoValido.getContato()
        );

        when(fornecedorRepository.save(any(Fornecedor.class))).thenReturn(fornecedorSalvo);

        FornecedorResponseDTO resposta = fornecedorService.criar(dtoValido);

        assertNotNull(resposta);
        assertEquals("f123", resposta.getId());
        assertEquals("Distribuidora Nacional de Peças Ltda", resposta.getRazaoSocial());
        assertEquals("Distribuidora Nacional", resposta.getNomeFantasia());
        assertEquals("12.345.678/0001-90", resposta.getCnpj());
        assertEquals("contato@pecas.com.br", resposta.getContato());
        verify(fornecedorRepository, times(1)).save(any(Fornecedor.class));
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando razão social for nula ou menor que 3 caracteres")
    void deveLancarExcecaoQuandoRazaoSocialInvalida() {
        CriarFornecedorDTO dtoInvalido = new CriarFornecedorDTO("AB", "", "", "");

        ValidacaoException ex = assertThrows(ValidacaoException.class, () -> fornecedorService.criar(dtoInvalido));
        assertTrue(ex.getMessage().contains("entre 3 e 120 caracteres"));

        CriarFornecedorDTO dtoNulo = new CriarFornecedorDTO(null, "", "", "");
        assertThrows(ValidacaoException.class, () -> fornecedorService.criar(dtoNulo));
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando razão social exceder 120 caracteres")
    void deveLancarExcecaoQuandoRazaoSocialExceder120Caracteres() {
        String razaoLonga = "A".repeat(121);
        CriarFornecedorDTO dtoInvalido = new CriarFornecedorDTO(razaoLonga, "", "", "");

        ValidacaoException ex = assertThrows(ValidacaoException.class, () -> fornecedorService.criar(dtoInvalido));
        assertTrue(ex.getMessage().contains("entre 3 e 120 caracteres"));
    }

    @Test
    @DisplayName("Deve buscar fornecedor por ID com sucesso")
    void deveBuscarFornecedorPorId() {
        Fornecedor fornecedor = new Fornecedor("f1", "Peças Ltda", "", "", "");
        when(fornecedorRepository.findById("f1")).thenReturn(Optional.of(fornecedor));

        FornecedorResponseDTO resposta = fornecedorService.buscarPorId("f1");

        assertNotNull(resposta);
        assertEquals("f1", resposta.getId());
        assertEquals("Peças Ltda", resposta.getRazaoSocial());
    }

    @Test
    @DisplayName("Deve lançar EntidadeNaoEncontradaException quando ID não existir")
    void deveLancarExcecaoQuandoFornecedorNaoExistir() {
        when(fornecedorRepository.findById("inexistente")).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> fornecedorService.buscarPorId("inexistente"));
    }

    @Test
    @DisplayName("Deve listar todos os fornecedores cadastrados")
    void deveListarTodosOsFornecedores() {
        List<Fornecedor> lista = List.of(
                new Fornecedor("1", "Fornecedor 1", "", "", ""),
                new Fornecedor("2", "Fornecedor 2", "", "", "")
        );
        when(fornecedorRepository.findAll()).thenReturn(lista);

        List<FornecedorResponseDTO> resultado = fornecedorService.listarTodos();

        assertEquals(2, resultado.size());
        assertEquals("Fornecedor 1", resultado.get(0).getRazaoSocial());
        assertEquals("Fornecedor 2", resultado.get(1).getRazaoSocial());
    }
}
