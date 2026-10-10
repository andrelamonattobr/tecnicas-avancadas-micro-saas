package com.maas.pedidos_conectado.web.dto;

import java.math.BigDecimal;

public class AplicarDescontoDTO {
    private BigDecimal descontoReais;

    public AplicarDescontoDTO() {
    }

    public AplicarDescontoDTO(BigDecimal descontoReais) {
        this.descontoReais = descontoReais;
    }

    public BigDecimal getDescontoReais() {
        return descontoReais;
    }

    public void setDescontoReais(BigDecimal descontoReais) {
        this.descontoReais = descontoReais;
    }
}
