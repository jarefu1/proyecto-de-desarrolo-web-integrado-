package com.mhmstore.api.dto;

import com.mhmstore.api.model.Permiso;
import java.util.Set;

public record AuthResponse(
    String token,
    String tipo,
    String email,
    String rol,
    Set<Permiso> permisos
) {
    public AuthResponse(String token, String tipo, String email, String rol) {
        this(token, tipo, email, rol, Set.of());
    }
}
