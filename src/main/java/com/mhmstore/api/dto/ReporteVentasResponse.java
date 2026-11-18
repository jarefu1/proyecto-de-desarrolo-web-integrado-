package com.mhmstore.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReporteVentasResponse(
    Long id,
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin,
    Integer totalProductosVendidos,
    BigDecimal totalIngresos,
    List<DetalleReporteResponse> detalles
) {}
