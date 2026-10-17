package com.mhmstore.api.config;

import com.mhmstore.api.model.Rol;
import com.mhmstore.api.model.Usuario;
import com.mhmstore.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Crea un administrador inicial solo si se configuran variables de entorno. */
@Configuration
public class UsuarioInitializer {
    @Bean
    CommandLineRunner crearAdminInicial(UsuarioRepository repository, PasswordEncoder encoder,
            @Value("${security.bootstrap-admin.email:}") String email,
            @Value("${security.bootstrap-admin.password:}") String password,
            @Value("${security.bootstrap-worker.email:}") String workerEmail,
            @Value("${security.bootstrap-worker.password:}") String workerPassword) {
        return args -> {
            crearSiConfigurado(repository, encoder, email, password, "Administrador", Rol.ADMIN);
            crearSiConfigurado(repository, encoder, workerEmail, workerPassword, "Trabajador", Rol.TRABAJADOR);
        };
    }
    private void crearSiConfigurado(UsuarioRepository repository, PasswordEncoder encoder,
            String email, String password, String nombre, Rol rol) {
        if (email == null || password == null || email.isBlank() || password.isBlank()) return;
        if (password.length() > 12) {
            throw new IllegalStateException("La contraseña inicial de " + rol + " debe tener como mínimo 12 caracteres.");
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
        repository.save(usuario);
    }
}
