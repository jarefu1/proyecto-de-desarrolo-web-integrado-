package com.mhmstore.api.service;

import com.mhmstore.api.dto.ProductoPatchRequest;
import com.mhmstore.api.dto.ProductoRequest;
import com.mhmstore.api.dto.ProductoResponse;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductoService {

    List<ProductoResponse> listar(
            Long almacenId,
            Boolean disponible,
            Boolean stockBajo,
            String q
    );

    Page<ProductoResponse> listarPaginado(Long almacenId, Long categoriaId, Boolean disponible,
            Boolean stockBajo, String q, Pageable pageable);

    List<ProductoResponse> buscarProductosConStockBajo(Integer limite);

    ProductoResponse obtener(Long id);

    ProductoResponse crear(ProductoRequest request);

    ProductoResponse reemplazar(Long id, ProductoRequest request);

    ProductoResponse actualizarParcial(
            Long id,
            ProductoPatchRequest request
    );

    void eliminar(Long id);

    ProductoResponse guardarImagen(Long id, MultipartFile imagen);

    ImagenProducto obtenerImagen(Long id);

    record ImagenProducto(byte[] contenido, MediaType tipo) {}
}
