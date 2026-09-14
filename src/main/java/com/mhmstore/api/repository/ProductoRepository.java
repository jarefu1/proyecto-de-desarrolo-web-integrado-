package com.mhmstore.api.repository;

import com.mhmstore.api.model.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository {
    List<Producto> findAll();
    Optional<Producto> findById(Long id);
    Optional<Producto> findByCodigoIgnoreCase(String codigo);
    Producto save(Producto producto);
    void deleteById(Long id);
}
