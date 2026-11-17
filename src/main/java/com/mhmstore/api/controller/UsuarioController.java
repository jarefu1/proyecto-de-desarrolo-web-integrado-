package com.mhmstore.api.controller;

import com.mhmstore.api.dto.UsuarioCreateRequest;
import com.mhmstore.api.dto.UsuarioPermisosRequest;
import com.mhmstore.api.dto.UsuarioResponse;
import com.mhmstore.api.dto.UsuarioUpdateRequest;
import com.mhmstore.api.model.Permiso;
import com.mhmstore.api.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @GetMapping("/permisos")
    public ResponseEntity<List<Permiso>> listarPermisosDisponibles() {
        return ResponseEntity.ok(usuarioService.listarPermisosDisponibles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    /** Solo ADMIN puede crear usuarios y asignarles permisos; la autorización se aplica en SecurityConfig. */
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    @PatchMapping("/{id}/permisos")
    public ResponseEntity<UsuarioResponse> actualizarPermisos(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioPermisosRequest request) {
        return ResponseEntity.ok(usuarioService.actualizarPermisos(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
