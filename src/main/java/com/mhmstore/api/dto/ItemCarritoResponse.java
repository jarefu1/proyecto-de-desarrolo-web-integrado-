package com.mhmstore.api.dto;

import java.math.BigDecimal;

public record ItemCarritoResponse(
    Long id,
    Long productoId,
    String productoCodigo,
    String productoModelo,
    String productoImagenUrl,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal
) {}
