package com.mhmstore.api.service;

import org.springframework.stereotype.Service;
import com.mhmstore.api.dto.ProductoPatchRequest;
import com.mhmstore.api.dto.ProductoRequest;
import com.mhmstore.api.dto.ProductoResponse;

import com.mhmstore.api.exception.CodigoDuplicadoException;
import com.mhmstore.api.exception.ProductoNotFoundException;

import com.mhmstore.api.model.Almacen;
import com.mhmstore.api.model.Producto;

import com.mhmstore.api.repository.ProductoRepository;
import java.util.List;

@Service
public class ProductoServiceImpl implements ProductoService {
    private static final int STOCK_BAJO_UMBRAL = 5;

    private final ProductoRepository repository;

    public ProductoServiceImpl(ProductoRepository repository) { this.repository = repository; }

    @Override
    public List<ProductoResponse> listar(Almacen almacen, Boolean disponible, Boolean stockBajo, String q) {
        String query = q == null ? null : q.trim().toLowerCase();
        return repository.findAll().stream()
                .filter(p -> almacen == null || p.getAlmacen() == almacen)
                .filter(p -> disponible == null || (p.getCantidad() > 0) == disponible)
                .filter(p -> stockBajo == null || (p.getCantidad() <= STOCK_BAJO_UMBRAL) == stockBajo)
                .filter(p -> query == null || query.isBlank()
                        || p.getModelo().toLowerCase().contains(query)
                        || p.getCodigo().toLowerCase().contains(query))
                .map(this::toResponse).toList();
    }

    @Override
    public ProductoResponse obtener(Long id) { return toResponse(buscar(id)); }

    @Override
    public ProductoResponse crear(ProductoRequest request) {
        validarCodigoDisponible(request.getCodigo(), null);
        Producto producto = new Producto(null,
                request.getCodigo().trim().toUpperCase(),
                request.getModelo().trim(),
                request.getColor().trim(),
                request.getCategoria(),
                request.getPrecio(),
                request.getCantidad(),
                request.getAlmacen());
        return toResponse(repository.save(producto));
    }

    @Override
    public ProductoResponse reemplazar(Long id, ProductoRequest request) {
        Producto producto = buscar(id);
        validarCodigoDisponible(request.getCodigo(), id);
        producto.setCodigo(request.getCodigo().trim().toUpperCase());
        producto.setModelo(request.getModelo().trim());
        producto.setColor(request.getColor().trim());
        producto.setCategoria(request.getCategoria());
        producto.setPrecio(request.getPrecio());
        producto.setCantidad(request.getCantidad());
        producto.setAlmacen(request.getAlmacen());
        return toResponse(repository.save(producto));
    }

    @Override
    public ProductoResponse actualizarParcial(Long id, ProductoPatchRequest request) {
        Producto producto = buscar(id);
        if (request.getCodigo() != null) {
            validarCodigoDisponible(request.getCodigo(), id);
            producto.setCodigo(request.getCodigo().trim().toUpperCase());
        }
        if (request.getModelo() != null) producto.setModelo(request.getModelo().trim());
        if (request.getColor() != null) producto.setColor(request.getColor().trim());
        if (request.getCategoria() != null) producto.setCategoria(request.getCategoria());
        if (request.getPrecio() != null) producto.setPrecio(request.getPrecio());
        if (request.getCantidad() != null) producto.setCantidad(request.getCantidad());
        if (request.getAlmacen() != null) producto.setAlmacen(request.getAlmacen());
        return toResponse(repository.save(producto));
    }

    @Override
    public void eliminar(Long id) {
        buscar(id);
        repository.deleteById(id);
    }

    private Producto buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProductoNotFoundException(id));
    }

    private void validarCodigoDisponible(String codigo, Long idActual) {
        repository.findByCodigoIgnoreCase(codigo.trim()).ifPresent(existing -> {
            if (idActual == null || !existing.getId().equals(idActual)) throw new CodigoDuplicadoException(codigo);
        });
    }

    private ProductoResponse toResponse(Producto p) {
        return new ProductoResponse(p.getId(), p.getCodigo(), p.getModelo(), p.getColor(), p.getCategoria(),
                p.getPrecio(), p.getCantidad(), p.getAlmacen());
    }
}
