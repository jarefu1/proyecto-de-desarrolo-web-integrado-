package com.mhmstore.api.dto;

import com.mhmstore.api.model.Permiso;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UsuarioPermisosRequest(
    @NotNull(message = "La lista de permisos es obligatoria")
    Set<Permiso> permisos
) {}
