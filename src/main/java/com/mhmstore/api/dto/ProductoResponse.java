
package com.mhmstore.api.dto;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String codigo,
        String modelo,
        String color,
        Long categoriaId,
        String categoriaNombre,
        BigDecimal precio,
        Integer cantidad,
        Long almacenId,
        String almacenNombre
) {
}
