
package com.mhmstore.api.repository;

import com.mhmstore.api.model.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlmacenRepository
        extends JpaRepository<Almacen, Long> {

    Optional<Almacen> findByNombreIgnoreCase(String nombre);
}
