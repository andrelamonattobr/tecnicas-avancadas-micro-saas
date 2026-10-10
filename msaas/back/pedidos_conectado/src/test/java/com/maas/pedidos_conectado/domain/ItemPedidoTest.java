package com.maas.pedidos_conectado.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.maas.pedidos_conectado.domain.exception.ValidacaoException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemPedidoTest {

    @Test
    @DisplayName("RN-04: Deve calcular subtotal do item corretamente (quantidade * preco_unitario)")
    void deveCalcularSubtotalDoItem() {
        ItemPedido item = ItemPedido.criar("lista-1", "Parafuso Sextavado", new BigDecimal("100"), new BigDecimal("0.50"));
        BigDecimal subtotalEsperado = new BigDecimal("50.00").setScale(2, RoundingMode.HALF_UP);
        assertEquals(subtotalEsperado, item.getSubtotal());

        ItemPedido item2 = ItemPedido.criar("lista-1", "Chave de Boca 14mm", new BigDecimal("2"), new BigDecimal("25.00"));
        BigDecimal subtotalEsperado2 = new BigDecimal("50.00").setScale(2, RoundingMode.HALF_UP);
        assertEquals(subtotalEsperado2, item2.getSubtotal());
    }

    @Test
    @DisplayName("Cenário 8: Deve lançar ValidacaoException quando quantidade for menor ou igual a zero")
    void deveRejeitarQuantidadeMenorOuIgualAZero_Cenario8() {
        ValidacaoException exZero = assertThrows(ValidacaoException.class, () ->
                ItemPedido.criar("lista-1", "Produto", BigDecimal.ZERO, new BigDecimal("10.00"))
        );
        assertEquals("Quantidade do item deve ser estritamente maior que zero.", exZero.getMessage());

        ValidacaoException exNegativa = assertThrows(ValidacaoException.class, () ->
                ItemPedido.criar("lista-1", "Produto", new BigDecimal("-1"), new BigDecimal("10.00"))
        );
        assertEquals("Quantidade do item deve ser estritamente maior que zero.", exNegativa.getMessage());
    }

    @Test
    @DisplayName("Cenário 9: Deve lançar ValidacaoException quando preço unitário for negativo")
    void deveRejeitarPrecoUnitarioNegativo_Cenario9() {
        ValidacaoException ex = assertThrows(ValidacaoException.class, () ->
                ItemPedido.criar("lista-1", "Produto", new BigDecimal("1"), new BigDecimal("-5.00"))
        );
        assertEquals("Preço unitário não pode ser negativo.", ex.getMessage());
    }

    @Test
    @DisplayName("Deve permitir preço unitário zero (gratuito/bonificação)")
    void devePermitirPrecoUnitarioZero() {
        ItemPedido item = ItemPedido.criar("lista-1", "Brinde Promocional", new BigDecimal("5"), BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), item.getSubtotal());
    }

    @Test
    @DisplayName("Deve rejeitar descrição com menos de 2 caracteres ou mais de 120 caracteres")
    void deveRejeitarDescricaoInvalida() {
        assertThrows(ValidacaoException.class, () ->
                ItemPedido.criar("lista-1", "A", new BigDecimal("1"), new BigDecimal("10.00"))
        );

        assertThrows(ValidacaoException.class, () ->
                ItemPedido.criar("lista-1", "A".repeat(121), new BigDecimal("1"), new BigDecimal("10.00"))
        );
    }
}
