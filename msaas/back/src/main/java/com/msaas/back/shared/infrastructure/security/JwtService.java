package com.msaas.back.shared.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.msaas.back.comerciante.domain.Comerciante;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class JwtService {

    private static final String ISSUER = "msaas-api";

    private final Algorithm algorithm;
    private final long expirationHours;

    public JwtService(
            @Value("${jwt.secret:msaas-chave-secreta-padrao-desenvolvimento-2026}") String secret,
            @Value("${jwt.expiration-hours:24}") long expirationHours
    ) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.expirationHours = expirationHours;
    }

    public String gerarToken(Comerciante comerciante) {
        Instant agora = Instant.now();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(comerciante.getEmail())
                .withClaim("id", comerciante.getId())
                .withClaim("nomeEstabelecimento", comerciante.getNomeEstabelecimento())
                .withIssuedAt(agora)
                .withExpiresAt(agora.plus(expirationHours, ChronoUnit.HOURS))
                .sign(algorithm);
    }

    public Optional<ComercianteAutenticado> validarToken(String token) {
        try {
            DecodedJWT decoded = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token);

            Long id = decoded.getClaim("id").asLong();
            String email = decoded.getSubject();
            String nomeEstabelecimento = decoded.getClaim("nomeEstabelecimento").asString();

            if (id == null || email == null) {
                return Optional.empty();
            }
            return Optional.of(new ComercianteAutenticado(id, email, nomeEstabelecimento));
        } catch (JWTVerificationException ex) {
            return Optional.empty();
        }
    }
}
