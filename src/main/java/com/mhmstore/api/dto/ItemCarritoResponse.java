package com.mhmstore.api.dto;

import java.math.BigDecimal;

public record ItemCarritoResponse(
    Long id,
    Long productoId,
    String productoCodigo,
    String productoModelo,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal
) {}
