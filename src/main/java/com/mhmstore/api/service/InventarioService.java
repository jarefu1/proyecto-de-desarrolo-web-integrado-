package com.mhmstore.api.service;

import com.mhmstore.api.dto.ProductoResponse;

public interface InventarioService {

    ProductoResponse registrarSalida(Long productoId, Integer cantidad);

    ProductoResponse registrarEntrada(Long productoId, Integer cantidad);
}