package com.mhmstore.api.dto;

import com.mhmstore.api.model.EstadoCarrito;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CarritoResponse(
    Long id,
    Long usuarioId,
    String usuarioEmail,
    LocalDateTime fechaCreacion,
    EstadoCarrito estado,
    List<ItemCarritoResponse> items,
    BigDecimal total
) {}
