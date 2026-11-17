package com.mhmstore.api.controller;

import com.mhmstore.api.model.Permiso;
import com.mhmstore.api.model.Rol;
import com.mhmstore.api.model.Usuario;
import com.mhmstore.api.repository.UsuarioRepository;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuarioControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario admin;
    private Usuario trabajador;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setEmail("admin@mhmstore.com");
        admin.setContrasena(passwordEncoder.encode("Admin123456"));
        admin.setRol(Rol.ADMIN);
        admin.setPermisos(EnumSet.allOf(Permiso.class));
        admin = usuarioRepository.save(admin);

        trabajador = new Usuario();
        trabajador.setNombre("Trabajador");
        trabajador.setEmail("trabajador@mhmstore.com");
        trabajador.setContrasena(passwordEncoder.encode("Trabajador123"));
        trabajador.setRol(Rol.TRABAJADOR);
        trabajador.setPermisos(Set.of(Permiso.VER_CATALOGOS, Permiso.GESTIONAR_CARRITO));
        trabajador = usuarioRepository.save(trabajador);
    }

    @Test
    void login_conCredencialesValidas_debeDevolverTokenYPermisos() throws Exception {
        String json = """
                {
                  "email": "admin@mhmstore.com",
                  "contrasena": "Admin123456"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.permisos").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_debePoderCrearTrabajadorConPermisosSeleccionados() throws Exception {
        String json = """
                {
                  "nombre": "Carlos Ventas",
                  "email": "carlos@mhmstore.com",
                  "contrasena": "ClaveSegura123",
                  "rol": "TRABAJADOR",
                  "permisos": ["VER_CATALOGOS", "GESTIONAR_CARRITO", "GESTIONAR_PEDIDOS", "VER_REPORTES"]
                }
                """;

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("carlos@mhmstore.com"))
                .andExpect(jsonPath("$.rol").value("TRABAJADOR"))
                .andExpect(jsonPath("$.permisos").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_debePoderActualizarPermisosDeTrabajador() throws Exception {
        String json = """
                {
                  "permisos": ["VER_CATALOGOS", "GESTIONAR_INVENTARIO", "GESTIONAR_PRODUCTOS"]
                }
                """;

        mockMvc.perform(patch("/api/v1/usuarios/" + trabajador.getId() + "/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(trabajador.getId()))
                .andExpect(jsonPath("$.permisos").isArray());
    }

    @Test
    @WithMockUser(roles = "TRABAJADOR")
    void trabajador_noDebePoderAccederAGestionDeUsuarios() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isForbidden());
    }
}
