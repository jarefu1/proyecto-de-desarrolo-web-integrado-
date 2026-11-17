package com.mhmstore.api.dto;

import com.mhmstore.api.model.Permiso;
import com.mhmstore.api.model.Rol;
import java.time.LocalDateTime;
import java.util.Set;

public record UsuarioResponse(
    Long id,
    String nombre,
    String email,
    Rol rol,
    LocalDateTime fechaRegistro,
    Set<Permiso> permisos
) {
    public UsuarioResponse(Long id, String nombre, String email, Rol rol, LocalDateTime fechaRegistro) {
        this(id, nombre, email, rol, fechaRegistro, Set.of());
    }
}
