package com.msaas.back.fornecedor.application;

import com.msaas.back.comerciante.domain.Comerciante;
import com.msaas.back.comerciante.domain.ComercianteRepository;
import com.msaas.back.fornecedor.application.dto.FornecedorCreateRequest;
import com.msaas.back.fornecedor.application.dto.FornecedorResponse;
import com.msaas.back.fornecedor.application.dto.FornecedorUpdateRequest;
import com.msaas.back.fornecedor.application.dto.PaginaFornecedorResponse;
import com.msaas.back.fornecedor.domain.Fornecedor;
import com.msaas.back.fornecedor.domain.FornecedorRepository;
import com.msaas.back.shared.domain.exception.RecursoNaoEncontradoException;
import com.msaas.back.shared.domain.exception.RegraNegocioException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final ComercianteRepository comercianteRepository;

    public FornecedorService(
            FornecedorRepository fornecedorRepository,
            ComercianteRepository comercianteRepository
    ) {
        this.fornecedorRepository = fornecedorRepository;
        this.comercianteRepository = comercianteRepository;
    }

    @Transactional
    public FornecedorResponse criar(Long comercianteId, FornecedorCreateRequest request) {
        if (request == null) {
            throw new RegraNegocioException("Dados do fornecedor são obrigatórios");
        }

        Comerciante comerciante = comercianteRepository.buscarPorId(comercianteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Comerciante não encontrado"));

        String telefoneHigienizado = Fornecedor.higienizarEValidarTelefoneWhatsapp(request.telefoneWhatsapp());
        if (fornecedorRepository.existePorComercianteIdETelefoneWhatsapp(comercianteId, telefoneHigienizado)) {
            throw new RegraNegocioException("Telefone de WhatsApp já cadastrado para outro fornecedor da sua conta");
        }

        String documentoHigienizado = Comerciante.higienizarEValidarDocumento(request.documento());
        if (documentoHigienizado != null && fornecedorRepository.existePorComercianteIdEDocumento(comercianteId, documentoHigienizado)) {
            throw new RegraNegocioException("Documento já cadastrado para outro fornecedor da sua conta");
        }

        Fornecedor novo = new Fornecedor(
                comerciante,
                request.nome(),
                request.nomeContato(),
                telefoneHigienizado,
                documentoHigienizado,
                request.email(),
                request.categoria()
        );

        Fornecedor salvo = fornecedorRepository.salvar(novo);
        return FornecedorResponse.de(salvo, "Fornecedor cadastrado com sucesso");
    }

    @Transactional(readOnly = true)
    public PaginaFornecedorResponse listarPaginado(
            Long comercianteId,
            int page,
            int size,
            Boolean ativo,
            String busca
    ) {
        boolean statusFiltro = (ativo == null) || ativo;
        int paginaValida = Math.max(page, 0);
        int tamanhoValido = (size <= 0 || size > 100) ? 10 : size;

        Page<Fornecedor> resultado = fornecedorRepository.listarPorComerciante(
                comercianteId,
                statusFiltro,
                busca,
                PageRequest.of(paginaValida, tamanhoValido)
        );

        List<FornecedorResponse> itens = resultado.getContent()
                .stream()
                .map(f -> FornecedorResponse.de(f, null))
                .toList();

        return new PaginaFornecedorResponse(
                itens,
                resultado.getNumber(),
                resultado.getSize(),
                resultado.getTotalElements(),
                resultado.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public FornecedorResponse buscarPorId(Long comercianteId, Long fornecedorId) {
        Fornecedor fornecedor = buscarDoComerciante(comercianteId, fornecedorId);
        return FornecedorResponse.de(fornecedor, null);
    }

    @Transactional
    public FornecedorResponse atualizar(Long comercianteId, Long fornecedorId, FornecedorUpdateRequest request) {
        if (request == null) {
            throw new RegraNegocioException("Dados de atualização são obrigatórios");
        }

        Fornecedor fornecedor = buscarDoComerciante(comercianteId, fornecedorId);

        String telefoneHigienizado = Fornecedor.higienizarEValidarTelefoneWhatsapp(request.telefoneWhatsapp());
        if (fornecedorRepository.existePorComercianteIdETelefoneWhatsappExcluindoId(
                comercianteId,
                telefoneHigienizado,
                fornecedorId
        )) {
            throw new RegraNegocioException("Telefone de WhatsApp já cadastrado para outro fornecedor da sua conta");
        }

        fornecedor.atualizarDados(
                request.nome(),
                request.nomeContato(),
                telefoneHigienizado,
                request.documento(),
                request.email(),
                request.categoria()
        );

        Fornecedor salvo = fornecedorRepository.salvar(fornecedor);
        return FornecedorResponse.de(salvo, "Fornecedor atualizado com sucesso");
    }

    @Transactional
    public FornecedorResponse inativar(Long comercianteId, Long fornecedorId) {
        Fornecedor fornecedor = buscarDoComerciante(comercianteId, fornecedorId);
        fornecedor.inativar();
        Fornecedor salvo = fornecedorRepository.salvar(fornecedor);
        return FornecedorResponse.de(salvo, "Fornecedor inativado com sucesso");
    }

    @Transactional
    public FornecedorResponse reativar(Long comercianteId, Long fornecedorId) {
        Fornecedor fornecedor = buscarDoComerciante(comercianteId, fornecedorId);
        fornecedor.reativar();
        Fornecedor salvo = fornecedorRepository.salvar(fornecedor);
        return FornecedorResponse.de(salvo, "Fornecedor reativado com sucesso");
    }

    private Fornecedor buscarDoComerciante(Long comercianteId, Long fornecedorId) {
        return fornecedorRepository.buscarPorIdEComercianteId(fornecedorId, comercianteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado"));
    }
}
