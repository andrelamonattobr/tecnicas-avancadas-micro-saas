package com.maas.pedidos_conectado.web;

import com.maas.pedidos_conectado.service.ListaPedidoService;
import com.maas.pedidos_conectado.web.dto.*;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listas")
@CrossOrigin(origins = "*")
public class ListaPedidoController {

    private final ListaPedidoService listaPedidoService;

    public ListaPedidoController(ListaPedidoService listaPedidoService) {
        this.listaPedidoService = listaPedidoService;
    }

    @PostMapping
    public ResponseEntity<ListaPedidoResponseDTO> criar(@RequestBody CriarListaPedidoDTO dto) {
        ListaPedidoResponseDTO criada = listaPedidoService.criar(dto);
        URI uri = URI.create("/api/listas/" + criada.getId());
        return ResponseEntity.created(uri).body(criada);
    }

    @GetMapping
    public ResponseEntity<List<ListaPedidoResponseDTO>> listar(
            @RequestParam(required = false) String empresaId) {
        List<ListaPedidoResponseDTO> listas = listaPedidoService.listar(empresaId);
        return ResponseEntity.ok(listas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListaPedidoResponseDTO> buscarPorId(@PathVariable String id) {
        ListaPedidoResponseDTO lista = listaPedidoService.buscarPorId(id);
        return ResponseEntity.ok(lista);
    }

    @PatchMapping("/{id}/titulo")
    public ResponseEntity<ListaPedidoResponseDTO> renomear(
            @PathVariable String id,
            @RequestBody RenomearListaPedidoDTO dto) {
        ListaPedidoResponseDTO atualizada = listaPedidoService.renomear(id, dto);
        return ResponseEntity.ok(atualizada);
    }

    @PostMapping("/{id}/itens")
    public ResponseEntity<ItemPedidoResponseDTO> adicionarItem(
            @PathVariable String id,
            @RequestBody CriarItemPedidoDTO dto) {
        ItemPedidoResponseDTO itemCriado = listaPedidoService.adicionarItem(id, dto);
        URI uri = URI.create("/api/listas/" + id + "/itens/" + itemCriado.getId());
        return ResponseEntity.created(uri).body(itemCriado);
    }

    @GetMapping("/{id}/itens")
    public ResponseEntity<List<ItemPedidoResponseDTO>> listarItens(@PathVariable String id) {
        List<ItemPedidoResponseDTO> itens = listaPedidoService.listarItens(id);
        return ResponseEntity.ok(itens);
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    public ResponseEntity<Void> removerItem(
            @PathVariable String id,
            @PathVariable String itemId) {
        listaPedidoService.removerItem(id, itemId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/desconto")
    public ResponseEntity<ListaPedidoResponseDTO> aplicarDesconto(
            @PathVariable String id,
            @RequestBody AplicarDescontoDTO dto) {
        ListaPedidoResponseDTO atualizada = listaPedidoService.aplicarDesconto(id, dto);
        return ResponseEntity.ok(atualizada);
    }
}
