
package com.mhmstore.api.repository;

import com.mhmstore.api.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository
        extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigoIgnoreCase(String codigo);

    @Query("""
        SELECT p
        FROM Producto p
        WHERE p.cantidad <= :limite
        ORDER BY p.cantidad ASC
        """)
    List<Producto> buscarProductosConStockBajo(
            @Param("limite") Integer limite
    );
}
