package com.msaas.back.comerciante;

import com.msaas.back.comerciante.application.ComercianteAuthService;
import com.msaas.back.comerciante.application.dto.AuthResponse;
import com.msaas.back.comerciante.application.dto.LoginRequest;
import com.msaas.back.comerciante.application.dto.RegistroComercianteRequest;
import com.msaas.back.comerciante.application.dto.RegistroComercianteResponse;
import com.msaas.back.comerciante.domain.Comerciante;
import com.msaas.back.comerciante.domain.ComercianteRepository;
import com.msaas.back.shared.domain.exception.CredenciaisInvalidasException;
import com.msaas.back.shared.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ComercianteAuthAcceptanceTest {

    @Autowired
    private ComercianteAuthService comercianteAuthService;

    @Autowired
    private ComercianteRepository comercianteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("CA-01: Cadastro de conta de comerciante com dados válidos")
    void deveCadastrarComercianteComDadosValidos() {
        RegistroComercianteRequest request = new RegistroComercianteRequest(
                "João Silva",
                "Mercadinho São José",
                "joao@mercadinho.com.br",
                "Compra123",
                "64999991111",
                "12345678901"
        );

        RegistroComercianteResponse response = comercianteAuthService.registrar(request);

        assertNotNull(response);
        assertEquals("Conta criada com sucesso", response.mensagem());

        Comerciante salvo = comercianteRepository.buscarPorEmailIgnoreCase("joao@mercadinho.com.br")
                .orElseThrow();
        assertTrue(salvo.isAtivo());
        assertNotNull(salvo.getDataCadastro());
        assertNotEquals("Compra123", salvo.getSenha());
        assertTrue(passwordEncoder.matches("Compra123", salvo.getSenha()));
    }

    @Test
    @DisplayName("CA-02: Rejeição de cadastro com e-mail duplicado (case-insensitive)")
    void deveRejeitarCadastroComEmailDuplicado() {
        comercianteAuthService.registrar(new RegistroComercianteRequest(
                "João Silva",
                "Mercadinho São José",
                "joao@mercadinho.com.br",
                "Compra123",
                null,
                null
        ));

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () ->
                comercianteAuthService.registrar(new RegistroComercianteRequest(
                        "Outro João",
                        "Outro Mercadinho",
                        "JOAO@mercadinho.com.br",
                        "Compra123",
                        null,
                        null
                ))
        );

        assertEquals("Este endereço de e-mail já está em uso", ex.getMessage());
    }

    @Test
    @DisplayName("CA-03: Login realizado com sucesso por comerciante ativo")
    void deveRealizarLoginComSucessoPorComercianteAtivo() {
        comercianteAuthService.registrar(new RegistroComercianteRequest(
                "João Silva",
                "Mercadinho São José",
                "joao@mercadinho.com.br",
                "Compra123",
                null,
                null
        ));

        AuthResponse auth = comercianteAuthService.autenticar(new LoginRequest(
                "joao@mercadinho.com.br",
                "Compra123"
        ));

        assertNotNull(auth);
        assertNotNull(auth.token());
        assertFalse(auth.token().isBlank());
        assertEquals("Mercadinho São José", auth.nomeEstabelecimento());
    }

    @Test
    @DisplayName("CA-04: Bloqueio de login com credenciais incorretas")
    void deveBloquearLoginComCredenciaisIncorretas() {
        comercianteAuthService.registrar(new RegistroComercianteRequest(
                "João Silva",
                "Mercadinho São José",
                "joao@mercadinho.com.br",
                "Compra123",
                null,
                null
        ));

        CredenciaisInvalidasException ex = assertThrows(CredenciaisInvalidasException.class, () ->
                comercianteAuthService.autenticar(new LoginRequest(
                        "joao@mercadinho.com.br",
                        "SenhaErrada99"
                ))
        );

        assertEquals("E-mail ou senha inválidos", ex.getMessage());
    }

    @Test
    @DisplayName("CA-05: Rejeição de senha fraca no momento do cadastro")
    void deveRejeitarSenhaFracaNoCadastro() {
        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () ->
                comercianteAuthService.registrar(new RegistroComercianteRequest(
                        "João Silva",
                        "Mercadinho São José",
                        "novo@mercadinho.com.br",
                        "12345",
                        null,
                        null
                ))
        );

        assertEquals("A senha deve ter no mínimo 8 caracteres, incluindo letras e números", ex.getMessage());
    }
}
