package com.mhmstore.api.controller;

import com.mhmstore.api.model.Almacen;
import com.mhmstore.api.service.AlmacenService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/almacenes")
public class AlmacenController {

    private final AlmacenService almacenService;

    public AlmacenController(AlmacenService almacenService) {
        this.almacenService = almacenService;
    }

    @GetMapping
    public List<Almacen> listar() {
        return almacenService.listar();
    }

    @GetMapping("/{id}")
    public Almacen obtenerPorId(@PathVariable Long id) {
        return almacenService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<Almacen> crear(
            @Valid @RequestBody Almacen almacen) {

        Almacen creado = almacenService.crear(almacen);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creado);
    }

    @PutMapping("/{id}")
    public Almacen actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Almacen almacen) {

        return almacenService.actualizar(id, almacen);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        almacenService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}