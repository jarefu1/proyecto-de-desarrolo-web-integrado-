package com.mhmstore.api.service;

import com.mhmstore.api.dto.UsuarioCreateRequest;
import com.mhmstore.api.dto.UsuarioResponse;
import com.mhmstore.api.model.Usuario;
import com.mhmstore.api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest request) {
        String email = request.email().trim();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        // Se conserva la contraseña tal como fue escrita; solo se almacena su hash BCrypt.
        usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
        usuario.setRol(request.rol());
        return respuesta(usuarioRepository.save(usuario));
    }

    private UsuarioResponse respuesta(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getEmail(),
                usuario.getRol(), usuario.getFechaRegistro());
    }
}
