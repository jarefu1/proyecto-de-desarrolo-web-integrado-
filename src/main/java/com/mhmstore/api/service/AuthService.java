package com.mhmstore.api.service;

import com.mhmstore.api.dto.AuthResponse;
import com.mhmstore.api.dto.LoginRequest;
import com.mhmstore.api.dto.RegisterRequest;
import com.mhmstore.api.model.Permiso;
import com.mhmstore.api.model.Rol;
import com.mhmstore.api.model.Usuario;
import com.mhmstore.api.repository.UsuarioRepository;
import com.mhmstore.api.security.JwtService;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));

        if (!passwordEncoder.matches(request.contrasena(), usuario.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
        }

        Set<Permiso> permisosEfectivos = usuario.getRol() == Rol.ADMIN
                ? EnumSet.allOf(Permiso.class)
                : (usuario.getPermisos() != null ? usuario.getPermisos() : Set.of());

        return new AuthResponse(
                jwtService.generarToken(usuario),
                "Bearer",
                usuario.getEmail(),
                usuario.getRol().name(),
                permisosEfectivos
        );
    }

    public AuthResponse registrar(RegisterRequest request) {
        String email = request.email().trim();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo electrónico");
        }

        // El registro público crea únicamente clientes; los roles internos se asignan en administración.
        Rol rolFinal = Rol.CLIENTE;

        Usuario nuevo = new Usuario();
        nuevo.setNombre(request.nombre().trim());
        nuevo.setEmail(email);
        nuevo.setContrasena(passwordEncoder.encode(request.contrasena()));
        nuevo.setRol(rolFinal);

        nuevo.setPermisos(EnumSet.of(
                Permiso.VER_CATALOGOS,
                Permiso.GESTIONAR_CARRITO,
                Permiso.GESTIONAR_PEDIDOS
        ));

        Usuario guardado = usuarioRepository.save(nuevo);
        Set<Permiso> permisos = guardado.getPermisos();

        return new AuthResponse(
                jwtService.generarToken(guardado),
                "Bearer",
                guardado.getEmail(),
                guardado.getRol().name(),
                permisos
        );
    }
}
