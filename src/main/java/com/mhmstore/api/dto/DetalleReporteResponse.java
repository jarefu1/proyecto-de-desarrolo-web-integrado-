package com.mhmstore.api.dto;

import java.math.BigDecimal;

public record DetalleReporteResponse(
    Long id,
    Long productoId,
    String productoCodigo,
    String productoModelo,
    Integer cantidadVendida,
    BigDecimal ingresos
) {}
