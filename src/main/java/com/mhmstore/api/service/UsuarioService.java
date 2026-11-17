package com.mhmstore.api.service;

import com.mhmstore.api.dto.UsuarioCreateRequest;
import com.mhmstore.api.dto.UsuarioPermisosRequest;
import com.mhmstore.api.dto.UsuarioResponse;
import com.mhmstore.api.dto.UsuarioUpdateRequest;
import com.mhmstore.api.model.Permiso;
import com.mhmstore.api.model.Rol;
import com.mhmstore.api.model.Usuario;
import com.mhmstore.api.repository.UsuarioRepository;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
                .map(this::respuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        return respuesta(buscarUsuario(id));
    }

    @Transactional(readOnly = true)
    public List<Permiso> listarPermisosDisponibles() {
        return Arrays.asList(Permiso.values());
    }

    public UsuarioResponse crear(UsuarioCreateRequest request) {
        String email = request.email().trim();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
        usuario.setRol(request.rol());
        usuario.setPermisos(resolverPermisos(request.rol(), request.permisos()));

        return respuesta(usuarioRepository.save(usuario));
    }

    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = buscarUsuario(id);

        if (request.nombre() != null && !request.nombre().isBlank()) {
            usuario.setNombre(request.nombre().trim());
        }

        if (request.email() != null && !request.email().isBlank()) {
            String nuevoEmail = request.email().trim();
            usuarioRepository.findByEmailIgnoreCase(nuevoEmail).ifPresent(existente -> {
                if (!existente.getId().equals(id)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
                }
            });
            usuario.setEmail(nuevoEmail);
        }

        if (request.contrasena() != null && !request.contrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
        }

        if (request.rol() != null) {
            usuario.setRol(request.rol());
        }

        if (usuario.getRol() == Rol.ADMIN) {
            usuario.setPermisos(EnumSet.allOf(Permiso.class));
        } else if (request.permisos() != null) {
            usuario.setPermisos(new HashSet<>(request.permisos()));
        }

        return respuesta(usuarioRepository.save(usuario));
    }

    public UsuarioResponse actualizarPermisos(Long id, UsuarioPermisosRequest request) {
        Usuario usuario = buscarUsuario(id);
        if (usuario.getRol() == Rol.ADMIN) {
            usuario.setPermisos(EnumSet.allOf(Permiso.class));
        } else {
            usuario.setPermisos(new HashSet<>(request.permisos()));
        }
        return respuesta(usuarioRepository.save(usuario));
    }

    public void eliminar(Long id) {
        Usuario usuario = buscarUsuario(id);
        usuarioRepository.delete(usuario);
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el usuario con ID " + id));
    }

    private Set<Permiso> resolverPermisos(Rol rol, Set<Permiso> solicitados) {
        if (rol == Rol.ADMIN) {
            return EnumSet.allOf(Permiso.class);
        }
        if (solicitados != null && !solicitados.isEmpty()) {
            return new HashSet<>(solicitados);
        }
        return EnumSet.of(
                Permiso.VER_CATALOGOS,
                Permiso.GESTIONAR_CARRITO,
                Permiso.GESTIONAR_PEDIDOS
        );
    }

    private UsuarioResponse respuesta(Usuario usuario) {
        Set<Permiso> permisosEfectivos = usuario.getRol() == Rol.ADMIN
                ? EnumSet.allOf(Permiso.class)
                : (usuario.getPermisos() != null ? usuario.getPermisos() : Set.of());

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getFechaRegistro(),
                permisosEfectivos
        );
    }
}
