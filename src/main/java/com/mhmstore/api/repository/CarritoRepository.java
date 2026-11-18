package com.mhmstore.api.repository;

import com.mhmstore.api.model.Carrito;
import com.mhmstore.api.model.EstadoCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Carrito c where c.id = :id")
    Optional<Carrito> findByIdForUpdate(@Param("id") Long id);

    List<Carrito> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    Optional<Carrito> findFirstByUsuarioIdAndEstadoOrderByFechaCreacionDesc(
            Long usuarioId,
            EstadoCarrito estado
    );
}
