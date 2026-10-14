package com.mhmstore.api.service;

import com.mhmstore.api.model.Almacen;
import com.mhmstore.api.repository.AlmacenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlmacenServiceImpl implements AlmacenService {

    private final AlmacenRepository almacenRepository;

    public AlmacenServiceImpl(AlmacenRepository almacenRepository) {
        this.almacenRepository = almacenRepository;
    }

    @Override
    public List<Almacen> listar() {
        return almacenRepository.findAll();
    }


    @Override
    public Almacen obtenerPorId(Long id) {
        return almacenRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Almacén no encontrado"));
    }

    @Override
    public Almacen crear(Almacen almacen) {
        almacenRepository
                .findByNombreIgnoreCase(almacen.getNombre())
                .ifPresent(a -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Ya existe un almacén con ese nombre");
                });

        almacen.setId(null);
        return almacenRepository.save(almacen);
    }

    @Override
    public Almacen actualizar(Long id, Almacen datos) {
        Almacen existente = obtenerPorId(id);

        almacenRepository
                .findByNombreIgnoreCase(datos.getNombre())
                .filter(a -> !a.getId().equals(id))
                .ifPresent(a -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Ya existe un almacén con ese nombre");
                });

        existente.setNombre(datos.getNombre());
        existente.setUbicacion(datos.getUbicacion());
        existente.setDescripcion(datos.getDescripcion());

        return almacenRepository.save(existente);
    }

    @Override
    public void eliminar(Long id) {
        Almacen almacen = obtenerPorId(id);
        almacenRepository.delete(almacen);
    }
}
