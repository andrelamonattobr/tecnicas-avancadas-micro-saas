package com.msaas.back.comerciante.application;

import com.msaas.back.comerciante.application.dto.AuthResponse;
import com.msaas.back.comerciante.application.dto.LoginRequest;
import com.msaas.back.comerciante.application.dto.RegistroComercianteRequest;
import com.msaas.back.comerciante.application.dto.RegistroComercianteResponse;
import com.msaas.back.comerciante.domain.Comerciante;
import com.msaas.back.comerciante.domain.ComercianteRepository;
import com.msaas.back.shared.domain.exception.CredenciaisInvalidasException;
import com.msaas.back.shared.domain.exception.RegraNegocioException;
import com.msaas.back.shared.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComercianteAuthService {

    private final ComercianteRepository comercianteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public ComercianteAuthService(
            ComercianteRepository comercianteRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.comercianteRepository = comercianteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegistroComercianteResponse registrar(RegistroComercianteRequest request) {
        if (request == null) {
            throw new RegraNegocioException("Dados de cadastro são obrigatórios");
        }

        Comerciante.validarSenhaBruta(request.senha());

        if (request.email() == null || request.email().isBlank()) {
            throw new RegraNegocioException("O e-mail é obrigatório");
        }
        String emailNormalizado = request.email().trim().toLowerCase();
        if (comercianteRepository.existePorEmailIgnoreCase(emailNormalizado)) {
            throw new RegraNegocioException("Este endereço de e-mail já está em uso");
        }

        String documentoHigienizado = Comerciante.higienizarDigitos(request.documento());
        if (documentoHigienizado != null && comercianteRepository.existePorDocumento(documentoHigienizado)) {
            throw new RegraNegocioException("Este documento já está em uso");
        }

        String senhaHash = passwordEncoder.encode(request.senha());

        Comerciante novo = new Comerciante(
                request.nomeResponsavel(),
                request.nomeEstabelecimento(),
                emailNormalizado,
                senhaHash,
                request.telefone(),
                documentoHigienizado
        );

        Comerciante salvo = comercianteRepository.salvar(novo);

        return new RegistroComercianteResponse(
                salvo.getId(),
                salvo.getNomeEstabelecimento(),
                salvo.getEmail(),
                "Conta criada com sucesso"
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse autenticar(LoginRequest request) {
        if (request == null || request.email() == null || request.senha() == null) {
            throw new CredenciaisInvalidasException("E-mail ou senha inválidos");
        }

        Comerciante comerciante = comercianteRepository
                .buscarPorEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail ou senha inválidos"));

        if (!comerciante.isAtivo() || !passwordEncoder.matches(request.senha(), comerciante.getSenha())) {
            throw new CredenciaisInvalidasException("E-mail ou senha inválidos");
        }

        String token = jwtService.gerarToken(comerciante);

        return new AuthResponse(
                comerciante.getId(),
                comerciante.getNomeEstabelecimento(),
                comerciante.getEmail(),
                token
        );
    }
}
