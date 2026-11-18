package com.mhmstore.api.repository;
import com.mhmstore.api.model.ProductoImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ProductoImagenRepository extends JpaRepository<ProductoImagen,Long> {
 List<ProductoImagen> findByProductoIdOrderByIdAsc(Long productoId);
 Optional<ProductoImagen> findByIdAndProductoId(Long id, Long productoId);
 long countByProductoId(Long productoId);
}
