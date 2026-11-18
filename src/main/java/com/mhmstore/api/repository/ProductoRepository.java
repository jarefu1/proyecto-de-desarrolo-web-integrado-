
package com.mhmstore.api.repository;

import com.mhmstore.api.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository
        extends JpaRepository<Producto, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        SELECT p FROM Producto p
        WHERE (:soloPublicos = false OR p.activo = true)
          AND (:almacenId IS NULL OR p.almacen.id = :almacenId)
          AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
          AND (:disponible IS NULL OR (:disponible = true AND p.cantidad > 0)
               OR (:disponible = false AND p.cantidad = 0))
          AND (:stockBajo IS NULL OR (:stockBajo = true AND p.cantidad > 0 AND p.cantidad <= p.stockMinimo)
               OR (:stockBajo = false AND p.cantidad > p.stockMinimo))
          AND (:query IS NULL OR :query = '' OR
               LOWER(p.modelo) LIKE LOWER(CONCAT('%', :query, '%')) OR
               LOWER(p.codigo) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY p.id
        """)
    Page<Producto> buscarFiltrados(
            @Param("almacenId") Long almacenId,
            @Param("categoriaId") Long categoriaId,
            @Param("disponible") Boolean disponible,
            @Param("stockBajo") Boolean stockBajo,
            @Param("soloPublicos") boolean soloPublicos,
            @Param("query") String query,
            Pageable pageable);

    long countByCategoriaId(Long id);
    @Query("select count(p) from Producto p where p.cantidad > p.stockMinimo")
    long contarDisponibles();
    @Query("select count(p) from Producto p where p.cantidad > 0 and p.cantidad <= p.stockMinimo")
    long contarBajos();
    long countByCantidad(Integer cantidad);

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
