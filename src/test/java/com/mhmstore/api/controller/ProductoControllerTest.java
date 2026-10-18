package com.mhmstore.api.controller;

import com.mhmstore.api.model.Almacen;
import com.mhmstore.api.model.Categoria;
import com.mhmstore.api.repository.AlmacenRepository;
import com.mhmstore.api.repository.CategoriaRepository;
import com.mhmstore.api.repository.ProductoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private Long categoriaId;
    private Long almacenId;

    @BeforeEach
    void prepararDatos() {
        Categoria categoria = categoriaRepository.findAll()
                .stream()
                .filter(c -> c.getNombre().equalsIgnoreCase("Urbana"))
                .findFirst()
                .orElseGet(() -> {
                    Categoria nueva = new Categoria();
                    nueva.setNombre("Urbana");
                    nueva.setDescripcion("Mochilas de uso urbano");
                    return categoriaRepository.save(nueva);
                });

        categoriaId = categoria.getId();

        Almacen almacen = almacenRepository.findAll()
                .stream()
                .filter(a -> a.getNombre().equalsIgnoreCase("Principal"))
                .findFirst()
                .orElseGet(() -> {
                    Almacen nuevo = new Almacen();
                    nuevo.setNombre("Principal");
                    nuevo.setUbicacion("Almacén central");
                    nuevo.setDescripcion("Almacén principal de mochilas");
                    return almacenRepository.save(nuevo);
                });

        almacenId = almacen.getId();
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Simula un administrador para crear productos
    void crearProducto_debeResponder201() throws Exception {

        String json = """
                {
                  "codigo": "MOCH0011",
                  "modelo": "Mochila Urbana",
                  "color": "Negro",
                  "categoriaId": %d,
                  "precio": 89.90,
                  "cantidad": 10,
                  "almacenId": %d
                }
                """.formatted(categoriaId, almacenId);

        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "TRABAJADOR") // Simula un trabajador para consultar
    void obtenerProductoInexistente_debeResponder404() throws Exception {

        mockMvc.perform(get("/api/v1/productos/99999"))
                .andExpect(status().isNotFound());
    }
}