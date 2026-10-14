
package com.mhmstore.api.service;

import com.mhmstore.api.model.Almacen;

import java.util.List;

public interface AlmacenService {

    List<Almacen> listar();

    Almacen obtenerPorId(Long id);

    Almacen crear(Almacen almacen);

    Almacen actualizar(Long id, Almacen almacen);

    void eliminar(Long id);
}
