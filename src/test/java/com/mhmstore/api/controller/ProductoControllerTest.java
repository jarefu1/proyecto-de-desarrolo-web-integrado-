package com.mhmstore.api.controller;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Usuario
 */
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void crearProducto_debeResponder201() throws Exception {

        String json = """
                {
                    "codigo": "MOCH001",
                    "modelo": "Mochila Urbana",
                    "color": "Negro",
                    "categoria": "URBANA",
                    "precio": 89.90,
                    "cantidad": 10,
                    "almacen": "PRINCIPAL"
                }
                """;

        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }
    
    @Test
void obtenerProductoInexistente_debeResponder404() throws Exception {

    mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .get("/api/v1/productos/99999")
    )
    .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers
                    .status()
                    .isNotFound()
    );
}
}