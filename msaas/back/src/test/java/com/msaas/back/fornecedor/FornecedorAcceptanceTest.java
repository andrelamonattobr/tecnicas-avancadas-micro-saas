package com.msaas.back.fornecedor;

import com.msaas.back.comerciante.application.ComercianteAuthService;
import com.msaas.back.comerciante.application.dto.RegistroComercianteRequest;
import com.msaas.back.comerciante.application.dto.RegistroComercianteResponse;
import com.msaas.back.fornecedor.application.FornecedorService;
import com.msaas.back.fornecedor.application.dto.FornecedorCreateRequest;
import com.msaas.back.fornecedor.application.dto.FornecedorResponse;
import com.msaas.back.fornecedor.application.dto.FornecedorUpdateRequest;
import com.msaas.back.fornecedor.application.dto.PaginaFornecedorResponse;
import com.msaas.back.shared.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FornecedorAcceptanceTest {

    @Autowired
    private FornecedorService fornecedorService;

    @Autowired
    private ComercianteAuthService comercianteAuthService;

    private Long criarComerciante(String nome, String email) {
        RegistroComercianteResponse res = comercianteAuthService.registrar(new RegistroComercianteRequest(
                nome,
                "Comércio de " + nome,
                email,
                "Compra123",
                null,
                null
        ));
        return res.id();
    }

    @Test
    @DisplayName("CA-01: Cadastro de fornecedor com higienização automática de máscara")
    void deveCadastrarFornecedorComHigienizacaoAutomatica() {
        Long joaoId = criarComerciante("João", "joao.forn1@mercadinho.com.br");

        FornecedorResponse response = fornecedorService.criar(joaoId, new FornecedorCreateRequest(
                "Distribuidora Central",
                "Carlos Vendedor",
                "(64) 99999-1234",
                "12.345.678/0001-99",
                "contato@central.com.br",
                "Bebidas"
        ));

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals("64999991234", response.telefoneWhatsapp());
        assertEquals("12345678000199", response.documento());
        assertTrue(response.ativo());
        assertEquals("Fornecedor cadastrado com sucesso", response.mensagem());
    }

    @Test
    @DisplayName("CA-02: Rejeição de telefone duplicado para o mesmo comerciante")
    void deveRejeitarTelefoneDuplicadoParaMesmoComerciante() {
        Long joaoId = criarComerciante("João", "joao.forn2@mercadinho.com.br");

        fornecedorService.criar(joaoId, new FornecedorCreateRequest(
                "Distribuidora Central",
                "Carlos Vendedor",
                "64999991234",
                null,
                null,
                "Bebidas"
        ));

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () ->
                fornecedorService.criar(joaoId, new FornecedorCreateRequest(
                        "Outro Fornecedor",
                        "Pedro",
                        "(64) 99999-1234",
                        null,
                        null,
                        "Hortifrúti"
                ))
        );

        assertEquals("Telefone de WhatsApp já cadastrado para outro fornecedor da sua conta", ex.getMessage());
    }

    @Test
    @DisplayName("CA-03: Permissão de mesmo fornecedor em contas de comerciantes diferentes (Isolamento Multi-Inquilino)")
    void devePermitirMesmoFornecedorEmContasDiferentesEIsolarListagem() {
        Long joaoId = criarComerciante("João", "joao.forn3@mercadinho.com.br");
        Long mariaId = criarComerciante("Maria", "maria.forn3@mercadinho.com.br");

        fornecedorService.criar(joaoId, new FornecedorCreateRequest(
                "Distribuidora Central João",
                "Carlos",
                "64999991234",
                "12345678000199",
                null,
                "Bebidas"
        ));

        FornecedorResponse respostaMaria = fornecedorService.criar(mariaId, new FornecedorCreateRequest(
                "Distribuidora Central Maria",
                "Carlos",
                "64999991234",
                "12345678000199",
                null,
                "Bebidas"
        ));

        assertNotNull(respostaMaria.id());

        PaginaFornecedorResponse paginaJoao = fornecedorService.listarPaginado(joaoId, 0, 10, true, null);
        PaginaFornecedorResponse paginaMaria = fornecedorService.listarPaginado(mariaId, 0, 10, true, null);

        assertEquals(1, paginaJoao.totalElements());
        assertEquals("Distribuidora Central João", paginaJoao.content().getFirst().nome());

        assertEquals(1, paginaMaria.totalElements());
        assertEquals("Distribuidora Central Maria", paginaMaria.content().getFirst().nome());
    }

    @Test
    @DisplayName("CA-04: Inativação e ocultação na listagem padrão paginada")
    void deveInativarFornecedorEOcultarNaListagemPadrao() {
        Long joaoId = criarComerciante("João", "joao.forn4@mercadinho.com.br");

        FornecedorResponse criado = fornecedorService.criar(joaoId, new FornecedorCreateRequest(
                "Distribuidora Central",
                "Carlos",
                "64999991234",
                "12345678000199",
                null,
                "Bebidas"
        ));

        fornecedorService.inativar(joaoId, criado.id());

        PaginaFornecedorResponse ativos = fornecedorService.listarPaginado(joaoId, 0, 10, true, null);
        assertEquals(0, ativos.totalElements());

        PaginaFornecedorResponse inativos = fornecedorService.listarPaginado(joaoId, 0, 10, false, null);
        assertEquals(1, inativos.totalElements());
        assertFalse(inativos.content().getFirst().ativo());
    }

    @Test
    @DisplayName("Edição cadastral mantendo ID e Documento imutáveis")
    void deveEditarFornecedorEBloquearAlteracaoDeDocumento() {
        Long joaoId = criarComerciante("João", "joao.forn5@mercadinho.com.br");

        FornecedorResponse criado = fornecedorService.criar(joaoId, new FornecedorCreateRequest(
                "Distribuidora Central",
                "Carlos",
                "64999991234",
                "12.345.678/0001-99",
                "antigo@central.com.br",
                "Bebidas"
        ));

        FornecedorResponse atualizado = fornecedorService.atualizar(
                joaoId,
                criado.id(),
                new FornecedorUpdateRequest(
                        "Distribuidora Central Atualizada",
                        "Carlos Novo",
                        "(64) 98888-7777",
                        "12345678000199",
                        "novo@central.com.br",
                        "Embalagens"
                )
        );

        assertEquals(criado.id(), atualizado.id());
        assertEquals("Distribuidora Central Atualizada", atualizado.nome());
        assertEquals("64988887777", atualizado.telefoneWhatsapp());
        assertEquals("12345678000199", atualizado.documento());

        assertThrows(RegraNegocioException.class, () ->
                fornecedorService.atualizar(
                        joaoId,
                        criado.id(),
                        new FornecedorUpdateRequest(
                                "Distribuidora Central Atualizada",
                                "Carlos Novo",
                                "64988887777",
                                "99999999000199",
                                "novo@central.com.br",
                                "Embalagens"
                        )
                )
        );
    }
}
