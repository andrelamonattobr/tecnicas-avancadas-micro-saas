package com.msaas.back.fornecedor.domain;

import com.msaas.back.comerciante.domain.Comerciante;
import com.msaas.back.shared.domain.exception.RegraNegocioException;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "fornecedores")
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comerciante_id", nullable = false)
    private Comerciante comerciante;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 80)
    private String nomeContato;

    @Column(nullable = false, length = 11)
    private String telefoneWhatsapp;

    @Column(length = 14)
    private String documento;

    @Column(length = 120)
    private String email;

    @Column(length = 60)
    private String categoria;

    @Column(nullable = false)
    private boolean ativo;

    @Column(nullable = false)
    private LocalDateTime dataCadastro;

    protected Fornecedor() {
    }

    public Fornecedor(
            Comerciante comerciante,
            String nome,
            String nomeContato,
            String telefoneWhatsapp,
            String documento,
            String email,
            String categoria
    ) {
        if (comerciante == null) {
            throw new RegraNegocioException("O comerciante autenticado é obrigatório");
        }
        this.comerciante = comerciante;
        this.documento = Comerciante.higienizarEValidarDocumento(documento);
        aplicarDadosCadastrais(nome, nomeContato, telefoneWhatsapp, email, categoria);
        this.ativo = true;
        this.dataCadastro = LocalDateTime.now();
    }

    public void atualizarDados(
            String nome,
            String nomeContato,
            String telefoneWhatsapp,
            String documentoInformado,
            String email,
            String categoria
    ) {
        String docHigienizado = Comerciante.higienizarDigitos(documentoInformado);
        if (docHigienizado != null && !Objects.equals(this.documento, docHigienizado)) {
            throw new RegraNegocioException("O documento do fornecedor não pode ser alterado após o cadastro");
        }
        aplicarDadosCadastrais(nome, nomeContato, telefoneWhatsapp, email, categoria);
    }

    private void aplicarDadosCadastrais(
            String nome,
            String nomeContato,
            String telefoneWhatsapp,
            String email,
            String categoria
    ) {
        this.nome = validarNome(nome);
        this.nomeContato = validarTextoOpcional(nomeContato, 2, 80, "nome do contato");
        this.telefoneWhatsapp = higienizarEValidarTelefoneWhatsapp(telefoneWhatsapp);
        this.email = validarEmailOpcional(email);
        this.categoria = validarTextoOpcional(categoria, 2, 60, "categoria");
    }

    public static String higienizarEValidarTelefoneWhatsapp(String telefoneWhatsapp) {
        String digitos = Comerciante.higienizarDigitos(telefoneWhatsapp);
        if (digitos == null || (digitos.length() != 10 && digitos.length() != 11)) {
            throw new RegraNegocioException("O telefone de WhatsApp é obrigatório e deve ter 10 ou 11 dígitos numéricos");
        }
        return digitos;
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.trim().length() < 3 || nome.trim().length() > 100) {
            throw new RegraNegocioException("O nome do fornecedor deve ter entre 3 e 100 caracteres");
        }
        return nome.trim();
    }

    private static String validarTextoOpcional(String valor, int min, int max, String nomeCampo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpo = valor.trim();
        if (limpo.length() < min || limpo.length() > max) {
            throw new RegraNegocioException("O campo " + nomeCampo + " deve ter entre " + min + " e " + max + " caracteres");
        }
        return limpo;
    }

    private static String validarEmailOpcional(String email) {
        return (email == null || email.isBlank()) ? null : Comerciante.validarENormalizarEmail(email);
    }

    public void inativar() {
        this.ativo = false;
    }

    public void reativar() {
        this.ativo = true;
    }

    public Long getId() {
        return id;
    }

    public Comerciante getComerciante() {
        return comerciante;
    }

    public String getNome() {
        return nome;
    }

    public String getNomeContato() {
        return nomeContato;
    }

    public String getTelefoneWhatsapp() {
        return telefoneWhatsapp;
    }

    public String getDocumento() {
        return documento;
    }

    public String getEmail() {
        return email;
    }

    public String getCategoria() {
        return categoria;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
}
