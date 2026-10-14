
package com.mhmstore.api.service;

import com.mhmstore.api.model.Categoria;
import com.mhmstore.api.repository.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(
            CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    @Override
    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoría no encontrada"));
    }

    @Override
    public Categoria crear(Categoria categoria) {
        categoriaRepository
                .findByNombreIgnoreCase(categoria.getNombre())
                .ifPresent(c -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Ya existe una categoría con ese nombre");
                });

        categoria.setId(null);
        return categoriaRepository.save(categoria);
    }

    @Override
    public Categoria actualizar(Long id, Categoria datos) {
        Categoria existente = obtenerPorId(id);

        categoriaRepository
                .findByNombreIgnoreCase(datos.getNombre())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Ya existe una categoría con ese nombre");
                });

        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());

        return categoriaRepository.save(existente);
    }

    @Override
    public void eliminar(Long id) {
        Categoria categoria = obtenerPorId(id);
        categoriaRepository.delete(categoria);
    }
}
