package com.mhmstore.api.dto;

import com.mhmstore.api.model.Permiso;
import com.mhmstore.api.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UsuarioUpdateRequest(
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    String nombre,

    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
    String email,

    @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres")
    @Pattern(
        regexp = "^\\S(.*\\S)?$",
        message = "La contraseña no puede tener espacios al inicio ni al final"
    )
    String contrasena,

    Rol rol,

    Set<Permiso> permisos
) {}
