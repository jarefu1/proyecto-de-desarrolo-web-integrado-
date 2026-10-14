package com.mhmstore.api.service;

import com.mhmstore.api.dto.ProductoResponse;
import com.mhmstore.api.exception.ProductoNotFoundException;
import com.mhmstore.api.model.Producto;
import com.mhmstore.api.repository.ProductoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class InventarioServiceImpl implements InventarioService {

    private final ProductoRepository productoRepository;

    public InventarioServiceImpl(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional
    public ProductoResponse registrarSalida(Long productoId, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cantidad solicitada debe ser mayor a 0"
            );
        }

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException(productoId));

        if (producto.getCantidad() < cantidad) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Stock insuficiente. Disponible: " + producto.getCantidad()
                            + ", solicitado: " + cantidad
            );
        }

        producto.setCantidad(producto.getCantidad() - cantidad);

        return toResponse(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public ProductoResponse registrarEntrada(Long productoId, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cantidad de entrada debe ser mayor a 0"
            );
        }

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException(productoId));

        producto.setCantidad(producto.getCantidad() + cantidad);

        return toResponse(productoRepository.save(producto));
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