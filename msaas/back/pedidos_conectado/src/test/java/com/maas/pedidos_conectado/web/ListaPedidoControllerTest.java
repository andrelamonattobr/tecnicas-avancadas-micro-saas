package com.maas.pedidos_conectado.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maas.pedidos_conectado.domain.exception.EntidadeNaoEncontradaException;
import com.maas.pedidos_conectado.domain.exception.ValidacaoException;
import com.maas.pedidos_conectado.service.ListaPedidoService;
import com.maas.pedidos_conectado.web.dto.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ListaPedidoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ListaPedidoService listaPedidoService;

    @InjectMocks
    private ListaPedidoController listaPedidoController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(listaPedidoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    @DisplayName("POST /api/listas - Deve criar lista com sucesso e retornar 201 Created")
    void deveCriarListaComSucesso() throws Exception {
        CriarListaPedidoDTO dto = new CriarListaPedidoDTO("emp-1", "Pedido Outubro");
        ListaPedidoResponseDTO responseDTO = new ListaPedidoResponseDTO(
                "lista-1",
                "emp-1",
                "Pedido Outubro",
                Instant.now(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        when(listaPedidoService.criar(any(CriarListaPedidoDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/listas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/listas/lista-1"))
                .andExpect(jsonPath("$.id").value("lista-1"))
                .andExpect(jsonPath("$.empresaId").value("emp-1"))
                .andExpect(jsonPath("$.titulo").value("Pedido Outubro"))
                .andExpect(jsonPath("$.subtotalItens").value(0.00))
                .andExpect(jsonPath("$.descontoReais").value(0.00))
                .andExpect(jsonPath("$.totalLiquido").value(0.00));
    }

    @Test
    @DisplayName("POST /api/listas - Deve retornar 400 Bad Request quando validação falhar")
    void deveRetornarBadRequestQuandoCriacaoFalhar() throws Exception {
        CriarListaPedidoDTO dto = new CriarListaPedidoDTO("inexistente", "Pedido");

        when(listaPedidoService.criar(any(CriarListaPedidoDTO.class)))
                .thenThrow(new ValidacaoException("Empresa fornecedora com ID 'inexistente' não existe."));

        mockMvc.perform(post("/api/listas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Empresa fornecedora com ID 'inexistente' não existe."));
    }

    @Test
    @DisplayName("PATCH /api/listas/{id}/titulo - Deve renomear lista com sucesso")
    void deveRenomearListaComSucesso() throws Exception {
        RenomearListaPedidoDTO dto = new RenomearListaPedidoDTO("Novo Título");
        ListaPedidoResponseDTO responseDTO = new ListaPedidoResponseDTO(
                "lista-1",
                "emp-1",
                "Novo Título",
                Instant.now(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        when(listaPedidoService.renomear(eq("lista-1"), any(RenomearListaPedidoDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/listas/lista-1/titulo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Novo Título"));
    }

    @Test
    @DisplayName("PATCH /api/listas/{id}/titulo - Deve retornar 404 Not Found para lista inexistente")
    void deveRetornarNotFoundAoRenomearListaInexistente() throws Exception {
        RenomearListaPedidoDTO dto = new RenomearListaPedidoDTO("Novo Título");

        when(listaPedidoService.renomear(eq("inexistente"), any(RenomearListaPedidoDTO.class)))
                .thenThrow(new EntidadeNaoEncontradaException("Lista de pedido não encontrada com ID: inexistente"));

        mockMvc.perform(patch("/api/listas/inexistente/titulo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/listas/{id}/itens - Deve adicionar item à lista com sucesso (201 Created)")
    void deveAdicionarItemComSucesso() throws Exception {
        CriarItemPedidoDTO itemDto = new CriarItemPedidoDTO("Parafuso Sextavado", new BigDecimal("100"), new BigDecimal("0.50"));
        ItemPedidoResponseDTO responseDTO = new ItemPedidoResponseDTO(
                "item-1", "lista-1", "Parafuso Sextavado", new BigDecimal("100"), new BigDecimal("0.50"), new BigDecimal("50.00")
        );

        when(listaPedidoService.adicionarItem(eq("lista-1"), any(CriarItemPedidoDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/listas/lista-1/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/listas/lista-1/itens/item-1"))
                .andExpect(jsonPath("$.id").value("item-1"))
                .andExpect(jsonPath("$.descricao").value("Parafuso Sextavado"))
                .andExpect(jsonPath("$.subtotal").value(50.00));
    }

    @Test
    @DisplayName("GET /api/listas/{id}/itens - Deve listar itens exclusivos da lista")
    void deveListarItensDaLista() throws Exception {
        ItemPedidoResponseDTO item1 = new ItemPedidoResponseDTO(
                "item-1", "lista-1", "Parafuso Sextavado", new BigDecimal("100"), new BigDecimal("0.50"), new BigDecimal("50.00")
        );
        when(listaPedidoService.listarItens("lista-1")).thenReturn(List.of(item1));

        mockMvc.perform(get("/api/listas/lista-1/itens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Parafuso Sextavado"))
                .andExpect(jsonPath("$[0].subtotal").value(50.00));
    }

    @Test
    @DisplayName("DELETE /api/listas/{id}/itens/{itemId} - Deve remover item com sucesso (204 No Content)")
    void deveRemoverItemComSucesso() throws Exception {
        doNothing().when(listaPedidoService).removerItem("lista-1", "item-1");

        mockMvc.perform(delete("/api/listas/lista-1/itens/item-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /api/listas/{id}/desconto - Deve aplicar desconto fixo e retornar total líquido recalculado")
    void deveAplicarDescontoComSucesso() throws Exception {
        AplicarDescontoDTO dto = new AplicarDescontoDTO(new BigDecimal("50.00"));
        ListaPedidoResponseDTO responseDTO = new ListaPedidoResponseDTO(
                "lista-1", "emp-1", "Pedido", Instant.now(),
                new BigDecimal("200.00"), new BigDecimal("50.00"), new BigDecimal("150.00")
        );

        when(listaPedidoService.aplicarDesconto(eq("lista-1"), any(AplicarDescontoDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/listas/lista-1/desconto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotalItens").value(200.00))
                .andExpect(jsonPath("$.descontoReais").value(50.00))
                .andExpect(jsonPath("$.totalLiquido").value(150.00));
    }

    @Test
    @DisplayName("PUT /api/listas/{id}/desconto - Deve retornar 400 Bad Request se desconto for negativo")
    void deveRetornarBadRequestQuandoDescontoNegativo() throws Exception {
        AplicarDescontoDTO dto = new AplicarDescontoDTO(new BigDecimal("-10.00"));

        when(listaPedidoService.aplicarDesconto(eq("lista-1"), any(AplicarDescontoDTO.class)))
                .thenThrow(new ValidacaoException("Desconto em R$ não pode ser negativo."));

        mockMvc.perform(put("/api/listas/lista-1/desconto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Desconto em R$ não pode ser negativo."));
    }

    @Test
    @DisplayName("GET /api/listas/{id}/exportar-markdown - Deve retornar texto em formato Markdown")
    void deveExportarMarkdownEndpoint() throws Exception {
        String markdownExemplo = "# Pedido de Compra: Teste\n**Data:** 10/10/2026";
        when(listaPedidoService.exportarMarkdown("lista-1")).thenReturn(markdownExemplo);

        mockMvc.perform(get("/api/listas/lista-1/exportar-markdown"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/markdown"))
                .andExpect(content().string(markdownExemplo));
    }
}
