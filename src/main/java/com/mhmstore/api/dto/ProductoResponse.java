package com.mhmstore.api.dto;

import com.mhmstore.api.model.Almacen;
import com.mhmstore.api.model.Categoria;

import java.math.BigDecimal;

public record ProductoResponse(Long id, String codigo, String modelo, String color, Categoria categoria,
                               BigDecimal precio, Integer cantidad, Almacen almacen) {}
