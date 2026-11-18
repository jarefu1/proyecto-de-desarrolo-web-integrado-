
package com.mhmstore.api.dto;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String codigo,
        String modelo,
        String color,
        String descripcion,
        Long categoriaId,
        String categoriaNombre,
        BigDecimal precio,
        Integer cantidad,
        Long almacenId,
        String almacenNombre,
        String imagenUrl,
        Integer stockMinimo,
        Boolean activo,
        java.time.LocalDateTime fechaCreacion,
        java.time.LocalDateTime fechaActualizacion
) {
}
