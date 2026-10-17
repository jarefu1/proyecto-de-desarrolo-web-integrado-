package com.mhmstore.api.dto;

import com.mhmstore.api.model.Rol;
import java.time.LocalDateTime;

public record UsuarioResponse(Long id, String nombre, String email, Rol rol, LocalDateTime fechaRegistro) {}
