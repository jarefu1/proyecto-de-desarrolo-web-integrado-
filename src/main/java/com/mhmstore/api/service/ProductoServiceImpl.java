
package com.mhmstore.api.service;

import com.mhmstore.api.dto.ProductoPatchRequest;
import com.mhmstore.api.dto.ProductoRequest;
import com.mhmstore.api.dto.ProductoResponse;
import com.mhmstore.api.exception.CodigoDuplicadoException;
import com.mhmstore.api.exception.ProductoNotFoundException;
import com.mhmstore.api.model.Almacen;
import com.mhmstore.api.model.Categoria;
import com.mhmstore.api.model.Producto;
import com.mhmstore.api.repository.AlmacenRepository;
import com.mhmstore.api.repository.CategoriaRepository;
import com.mhmstore.api.repository.ProductoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private static final int STOCK_BAJO_UMBRAL = 5;

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final AlmacenRepository almacenRepository;

    public ProductoServiceImpl(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            AlmacenRepository almacenRepository) {

        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.almacenRepository = almacenRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(
            Long almacenId,
            Boolean disponible,
            Boolean stockBajo,
            String q) {

        String query = q == null ? null : q.trim().toLowerCase();

        return productoRepository.findAll().stream()
                .filter(p -> almacenId == null
                        || p.getAlmacen().getId().equals(almacenId))
                .filter(p -> disponible == null
                        || (p.getCantidad() > 0) == disponible)
                .filter(p -> stockBajo == null
                        || (p.getCantidad() <= STOCK_BAJO_UMBRAL) == stockBajo)
                .filter(p -> query == null || query.isBlank()
                        || p.getModelo().toLowerCase().contains(query)
                        || p.getCodigo().toLowerCase().contains(query))
                .map(this::toResponse)
                .toList();
    }

    
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> buscarProductosConStockBajo(
            Integer limite) {

        return productoRepository
                .buscarProductosConStockBajo(limite)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Override
    public ProductoResponse crear(ProductoRequest request) {

        validarCodigoDisponible(request.getCodigo(), null);

        Categoria categoria = buscarCategoria(request.getCategoriaId());
        Almacen almacen = buscarAlmacen(request.getAlmacenId());

        Producto producto = new Producto(
                null,
                request.getCodigo().trim().toUpperCase(),
                request.getModelo().trim(),
                request.getColor().trim(),
                categoria,
                request.getPrecio(),
                request.getCantidad(),
                almacen
        );

        return toResponse(productoRepository.save(producto));
    }

    @Override
    public ProductoResponse reemplazar(Long id, ProductoRequest request) {

        Producto producto = buscar(id);

        validarCodigoDisponible(request.getCodigo(), id);

        Categoria categoria = buscarCategoria(request.getCategoriaId());
        Almacen almacen = buscarAlmacen(request.getAlmacenId());

        producto.setCodigo(request.getCodigo().trim().toUpperCase());
        producto.setModelo(request.getModelo().trim());
        producto.setColor(request.getColor().trim());
        producto.setCategoria(categoria);
        producto.setPrecio(request.getPrecio());
        producto.setCantidad(request.getCantidad());
        producto.setAlmacen(almacen);

        return toResponse(productoRepository.save(producto));
    }

    @Override
    public ProductoResponse actualizarParcial(
            Long id,
            ProductoPatchRequest request) {

        Producto producto = buscar(id);

        if (request.getCodigo() != null) {
            validarCodigoDisponible(request.getCodigo(), id);
            producto.setCodigo(request.getCodigo().trim().toUpperCase());
        }

        if (request.getModelo() != null) {
            producto.setModelo(request.getModelo().trim());
        }

        if (request.getColor() != null) {
            producto.setColor(request.getColor().trim());
        }

        if (request.getCategoriaId() != null) {
            producto.setCategoria(
                    buscarCategoria(request.getCategoriaId())
            );
        }

        if (request.getPrecio() != null) {
            producto.setPrecio(request.getPrecio());
        }

        if (request.getCantidad() != null) {
            producto.setCantidad(request.getCantidad());
        }

        if (request.getAlmacenId() != null) {
            producto.setAlmacen(
                    buscarAlmacen(request.getAlmacenId())
            );
        }

        return toResponse(productoRepository.save(producto));
    }

    @Override
    public void eliminar(Long id) {
        Producto producto = buscar(id);
        productoRepository.delete(producto);
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe la categoría con ID " + id
                ));
    }

    private Almacen buscarAlmacen(Long id) {
        return almacenRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe el almacén con ID " + id
                ));
    }

    private void validarCodigoDisponible(String codigo, Long idActual) {
        productoRepository.findByCodigoIgnoreCase(codigo.trim())
                .ifPresent(existing -> {
                    if (idActual == null
                            || !existing.getId().equals(idActual)) {
                        throw new CodigoDuplicadoException(codigo);
                    }
                });
    }

    private ProductoResponse toResponse(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getCodigo(),
                producto.getModelo(),
                producto.getColor(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre(),
                producto.getPrecio(),
                producto.getCantidad(),
                producto.getAlmacen().getId(),
                producto.getAlmacen().getNombre()
        );
    }
}
