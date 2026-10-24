package com.msaas.back.fornecedor.presentation;

import com.msaas.back.fornecedor.application.FornecedorService;
import com.msaas.back.fornecedor.application.dto.FornecedorCreateRequest;
import com.msaas.back.fornecedor.application.dto.FornecedorResponse;
import com.msaas.back.fornecedor.application.dto.FornecedorUpdateRequest;
import com.msaas.back.fornecedor.application.dto.PaginaFornecedorResponse;
import com.msaas.back.shared.infrastructure.security.ComercianteAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fornecedores")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    public FornecedorController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @PostMapping
    public ResponseEntity<FornecedorResponse> criar(
            @AuthenticationPrincipal ComercianteAutenticado autenticado,
            @RequestBody FornecedorCreateRequest request
    ) {
        FornecedorResponse response = fornecedorService.criar(autenticado.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PaginaFornecedorResponse> listarPaginado(
            @AuthenticationPrincipal ComercianteAutenticado autenticado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "true") boolean ativo,
            @RequestParam(required = false) String busca
    ) {
        PaginaFornecedorResponse response = fornecedorService.listarPaginado(
                autenticado.id(),
                page,
                size,
                ativo,
                busca
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FornecedorResponse> buscarPorId(
            @AuthenticationPrincipal ComercianteAutenticado autenticado,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(fornecedorService.buscarPorId(autenticado.id(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FornecedorResponse> atualizar(
            @AuthenticationPrincipal ComercianteAutenticado autenticado,
            @PathVariable Long id,
            @RequestBody FornecedorUpdateRequest request
    ) {
        return ResponseEntity.ok(fornecedorService.atualizar(autenticado.id(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<FornecedorResponse> inativar(
            @AuthenticationPrincipal ComercianteAutenticado autenticado,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(fornecedorService.inativar(autenticado.id(), id));
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<FornecedorResponse> inativarPatch(
            @AuthenticationPrincipal ComercianteAutenticado autenticado,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(fornecedorService.inativar(autenticado.id(), id));
    }

    @PatchMapping("/{id}/reativar")
    public ResponseEntity<FornecedorResponse> reativarPatch(
            @AuthenticationPrincipal ComercianteAutenticado autenticado,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(fornecedorService.reativar(autenticado.id(), id));
    }
}
