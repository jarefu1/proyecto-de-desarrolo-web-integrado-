package com.mhmstore.api.repository;

import com.mhmstore.api.model.EstadoPedido;
import com.mhmstore.api.model.Pedido;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    boolean existsByCarritoId(Long carritoId);

    List<Pedido> findByCarritoUsuarioEmailIgnoreCaseOrderByFechaEnvioDesc(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pedido p WHERE p.id = :id")
    Optional<Pedido> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        SELECT p
        FROM Pedido p
        JOIN FETCH p.carrito c
        WHERE p.fechaEnvio BETWEEN :fechaInicio AND :fechaFin
        ORDER BY p.fechaEnvio DESC
        """)
    List<Pedido> buscarPedidosPorRangoFechas(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query("""
        SELECT p
        FROM Pedido p
        WHERE p.estado = :estado
        ORDER BY p.fechaEnvio DESC
        """)
    List<Pedido> buscarPorEstado(@Param("estado") EstadoPedido estado);
}
