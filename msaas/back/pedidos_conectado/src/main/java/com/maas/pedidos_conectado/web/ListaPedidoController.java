package com.maas.pedidos_conectado.web;

import com.maas.pedidos_conectado.service.ListaPedidoService;
import com.maas.pedidos_conectado.web.dto.CriarListaPedidoDTO;
import com.maas.pedidos_conectado.web.dto.ListaPedidoResponseDTO;
import com.maas.pedidos_conectado.web.dto.RenomearListaPedidoDTO;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
