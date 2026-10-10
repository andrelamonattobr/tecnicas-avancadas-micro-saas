package com.maas.pedidos_conectado.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maas.pedidos_conectado.domain.exception.EntidadeNaoEncontradaException;
import com.maas.pedidos_conectado.domain.exception.ValidacaoException;
import com.maas.pedidos_conectado.service.ListaPedidoService;
import com.maas.pedidos_conectado.web.dto.CriarListaPedidoDTO;
import com.maas.pedidos_conectado.web.dto.ListaPedidoResponseDTO;
import com.maas.pedidos_conectado.web.dto.RenomearListaPedidoDTO;
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
    @DisplayName("GET /api/listas - Deve retornar lista de pedidos")
    void deveRetornarListas() throws Exception {
        ListaPedidoResponseDTO item = new ListaPedidoResponseDTO(
                "l1", "emp-1", "Lista 1", Instant.now(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );
        when(listaPedidoService.listar(null)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/listas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("l1"))
                .andExpect(jsonPath("$[0].titulo").value("Lista 1"));
    }
}
