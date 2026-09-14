package com.mhmstore.api.service;

import com.mhmstore.api.dto.ProductoPatchRequest;
import com.mhmstore.api.dto.ProductoRequest;
import com.mhmstore.api.dto.ProductoResponse;
import com.mhmstore.api.model.Almacen;

import java.util.List;

public interface ProductoService {
    List<ProductoResponse> listar(Almacen almacen, Boolean disponible, Boolean stockBajo, String q);
    ProductoResponse obtener(Long id);
    ProductoResponse crear(ProductoRequest request);
    ProductoResponse reemplazar(Long id, ProductoRequest request);
    ProductoResponse actualizarParcial(Long id, ProductoPatchRequest request);
    void eliminar(Long id);
}
