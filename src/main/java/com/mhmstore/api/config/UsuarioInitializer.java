package com.mhmstore.api.config;

import com.mhmstore.api.model.Permiso;
import com.mhmstore.api.model.Rol;
import com.mhmstore.api.model.Usuario;
import com.mhmstore.api.repository.UsuarioRepository;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Crea un administrador y/o trabajador inicial si se configuran variables de entorno. */
@Configuration
public class UsuarioInitializer {
    @Bean
    CommandLineRunner crearAdminInicial(UsuarioRepository repository, PasswordEncoder encoder,
            @Value("${security.bootstrap-admin.email:}") String email,
            @Value("${security.bootstrap-admin.password:}") String password,
            @Value("${security.bootstrap-worker.email:}") String workerEmail,
            @Value("${security.bootstrap-worker.password:}") String workerPassword) {
        return args -> {
            crearSiConfigurado(repository, encoder, email, password, "Administrador", Rol.ADMIN,
                    EnumSet.allOf(Permiso.class));
            crearSiConfigurado(repository, encoder, workerEmail, workerPassword, "Trabajador", Rol.TRABAJADOR,
                    EnumSet.of(Permiso.VER_CATALOGOS, Permiso.GESTIONAR_CARRITO,
                            Permiso.GESTIONAR_PEDIDOS, Permiso.GESTIONAR_INVENTARIO));
        };
    }

    private void crearSiConfigurado(UsuarioRepository repository, PasswordEncoder encoder,
            String email, String password, String nombre, Rol rol, Set<Permiso> permisos) {
        if (email == null || password == null || email.isBlank() || password.isBlank()) return;
        if (password.length() < 6 || password.length() > 72) {
            throw new IllegalStateException("La contraseña inicial de " + rol + " debe tener entre 6 y 72 caracteres.");
        }
        if (!password.equals(password.strip())) {
            throw new IllegalStateException("La contraseña inicial de " + rol + " no puede tener espacios al inicio ni al final.");
        }
        if (repository.existsByEmailIgnoreCase(email.trim())) return;
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email.trim());
        usuario.setContrasena(encoder.encode(password));
        usuario.setRol(rol);
        usuario.setPermisos(permisos);
        repository.save(usuario);
    }
}
