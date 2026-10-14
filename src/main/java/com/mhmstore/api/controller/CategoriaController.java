package com.mhmstore.api.controller;

import com.mhmstore.api.model.Categoria;
import com.mhmstore.api.service.CategoriaService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<Categoria> listar() {
        return categoriaService.listar();
    }

    @GetMapping("/{id}")
    public Categoria obtenerPorId(@PathVariable Long id) {
        return categoriaService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<Categoria> crear(
            @Valid @RequestBody Categoria categoria) {

        Categoria creada = categoriaService.crear(categoria);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creada);
    }

    @PutMapping("/{id}")
    public Categoria actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Categoria categoria) {

        return categoriaService.actualizar(id, categoria);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}