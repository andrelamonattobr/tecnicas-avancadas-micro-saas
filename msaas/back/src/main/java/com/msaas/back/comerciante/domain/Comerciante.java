package com.msaas.back.comerciante.domain;

import com.msaas.back.shared.domain.exception.RegraNegocioException;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comerciantes")
public class Comerciante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nomeResponsavel;

    @Column(nullable = false, length = 100)
    private String nomeEstabelecimento;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 120)
    private String senha;

    @Column(length = 11)
    private String telefone;

    @Column(unique = true, length = 14)
    private String documento;

    @Column(nullable = false)
    private boolean ativo;

    @Column(nullable = false)
    private LocalDateTime dataCadastro;

    protected Comerciante() {
    }

    public Comerciante(
            String nomeResponsavel,
            String nomeEstabelecimento,
            String email,
            String senhaHash,
            String telefone,
            String documento
    ) {
        String estabelecimentoLimpo = validarNomeEstabelecimento(nomeEstabelecimento);
        this.nomeEstabelecimento = estabelecimentoLimpo;
        this.nomeResponsavel = validarNomeResponsavel(nomeResponsavel, estabelecimentoLimpo);
        this.email = validarENormalizarEmail(email);
        if (senhaHash == null || senhaHash.isBlank()) {
            throw new RegraNegocioException("A senha criptografada é obrigatória");
        }
        this.senha = senhaHash;
        this.telefone = higienizarEValidarTelefone(telefone);
        this.documento = higienizarEValidarDocumento(documento);
        this.ativo = true;
        this.dataCadastro = LocalDateTime.now();
    }

    public static void validarSenhaBruta(String senhaBruta) {
        if (senhaBruta == null || senhaBruta.length() < 8 || senhaBruta.length() > 64) {
            throw new RegraNegocioException("A senha deve ter no mínimo 8 caracteres, incluindo letras e números");
        }
        boolean temLetra = false;
        boolean temNumero = false;
        for (char c : senhaBruta.toCharArray()) {
            if (Character.isLetter(c)) {
                temLetra = true;
            } else if (Character.isDigit(c)) {
                temNumero = true;
            }
        }
        if (!temLetra || !temNumero) {
            throw new RegraNegocioException("A senha deve ter no mínimo 8 caracteres, incluindo letras e números");
        }
    }

    public static String higienizarDigitos(String valor) {
        if (valor == null) {
            return null;
        }
        String apenasDigitos = valor.replaceAll("\\D", "");
        return apenasDigitos.isEmpty() ? null : apenasDigitos;
    }

    private static String validarNomeEstabelecimento(String nomeEstabelecimento) {
        if (nomeEstabelecimento == null || nomeEstabelecimento.trim().length() < 2 || nomeEstabelecimento.trim().length() > 100) {
            throw new RegraNegocioException("O nome do estabelecimento deve ter entre 2 e 100 caracteres");
        }
        return nomeEstabelecimento.trim();
    }

    private static String validarNomeResponsavel(String nomeResponsavel, String padraoEstabelecimento) {
        String candidato = (nomeResponsavel == null || nomeResponsavel.isBlank())
                ? padraoEstabelecimento
                : nomeResponsavel.trim();
        if (candidato.length() < 3 || candidato.length() > 100) {
            throw new RegraNegocioException("O nome do responsável deve ter entre 3 e 100 caracteres");
        }
        return candidato;
    }

    private static String validarENormalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RegraNegocioException("O e-mail é obrigatório");
        }
        String normalizado = email.trim().toLowerCase();
        if (normalizado.length() > 120 || !normalizado.contains("@") || !normalizado.contains(".")) {
            throw new RegraNegocioException("Formato de e-mail inválido");
        }
        return normalizado;
    }

    private static String higienizarEValidarTelefone(String telefone) {
        String digitos = higienizarDigitos(telefone);
        if (digitos == null) {
            return null;
        }
        if (digitos.length() != 10 && digitos.length() != 11) {
            throw new RegraNegocioException("O telefone deve conter 10 ou 11 dígitos numéricos com DDD");
        }
        return digitos;
    }

    private static String higienizarEValidarDocumento(String documento) {
        String digitos = higienizarDigitos(documento);
        if (digitos == null) {
            return null;
        }
        if (digitos.length() != 11 && digitos.length() != 14) {
            throw new RegraNegocioException("O documento deve conter 11 dígitos (CPF) ou 14 dígitos (CNPJ)");
        }
        return digitos;
    }

    public void inativar() {
        this.ativo = false;
    }

    public Long getId() {
        return id;
    }

    public String getNomeResponsavel() {
        return nomeResponsavel;
    }

    public String getNomeEstabelecimento() {
        return nomeEstabelecimento;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getDocumento() {
        return documento;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
}
