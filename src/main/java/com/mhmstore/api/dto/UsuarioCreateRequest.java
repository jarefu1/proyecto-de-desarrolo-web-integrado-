package com.mhmstore.api.dto;

import com.mhmstore.api.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioCreateRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    String nombre,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 12, message = "La contraseña debe tener como máximo 12 caracteres")
@Pattern(
    regexp = "^\\S(.*\\S)?$",
    message = "La contraseña no puede tener espacios al inicio ni al final"
)
String contrasena,

    @NotNull(message = "El rol es obligatorio")
    Rol rol
) {}
