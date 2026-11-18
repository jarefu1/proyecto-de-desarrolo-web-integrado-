package com.mhmstore.api.dto;

import com.mhmstore.api.model.EstadoPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
    Long id,
    Long carritoId,
    Long usuarioId,
    String usuarioEmail,
    LocalDateTime fechaEnvio,
    EstadoPedido estado,
    BigDecimal total,
    String metodoPago,
    LocalDateTime fechaPago,
    List<ItemCarritoResponse> items
) {}
