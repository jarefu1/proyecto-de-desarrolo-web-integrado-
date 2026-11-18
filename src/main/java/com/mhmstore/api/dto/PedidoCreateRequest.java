package com.mhmstore.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PedidoCreateRequest(
    @NotNull(message = "El ID del carrito es obligatorio")
    @Positive(message = "El ID del carrito debe ser positivo")
    Long carritoId
) {}
