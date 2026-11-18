package com.mhmstore.api.controller;

import com.mhmstore.api.model.*;
import com.mhmstore.api.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import java.math.BigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InventarioCatalogoIntegrationTest {
 @Autowired MockMvc mvc;
 @Autowired ProductoRepository productos;
 @Autowired CategoriaRepository categorias;
 @Autowired AlmacenRepository almacenes;
 Categoria categoria;
 @BeforeEach void preparar() {
  categoria=categorias.save(new Categoria("Categoria inventario", "Prueba aislada"));
  Almacen almacen=new Almacen(); almacen.setNombre("Almacen inventario"); almacen.setUbicacion("Local"); almacen=almacenes.save(almacen);
  crear("INV-A",8,3,true,almacen);crear("INV-B",3,3,true,almacen);crear("INV-C",0,3,true,almacen);crear("INV-D",5,3,false,almacen);
 }
 Producto crear(String sku,int stock,int minimo,boolean activo,Almacen almacen){Producto p=new Producto(null,sku,sku,"Negro",categoria,new BigDecimal("20.00"),stock,almacen);p.setStockMinimo(minimo);p.setActivo(activo);return productos.saveAndFlush(p);}
 @Test void catalogoPublicoOcultaInactivos() throws Exception {
  mvc.perform(get("/api/v1/productos/pagina").param("q","INV-")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(3));
  mvc.perform(get("/api/v1/inventario/resumen")).andExpect(status().isUnauthorized());
 }
 @Test @WithMockUser(roles="CLIENTE") void clienteNoPuedeAdministrar() throws Exception {
  mvc.perform(get("/api/v1/inventario/productos")).andExpect(status().isForbidden());
  mvc.perform(post("/api/v1/categorias").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"No permitido\"}")).andExpect(status().isForbidden());
 }
 @Test @WithMockUser(roles="ADMIN") void inventarioIncluyeOcultosYUsaMinimoPropio() throws Exception {
  mvc.perform(get("/api/v1/inventario/productos").param("categoria",categoria.getId().toString())).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(4));
  mvc.perform(get("/api/v1/inventario/productos").param("categoria",categoria.getId().toString()).param("stockBajo","true")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].codigo").value("INV-B")).andExpect(jsonPath("$.content[0].fechaCreacion").exists());
  mvc.perform(delete("/api/v1/categorias/"+categoria.getId())).andExpect(status().isConflict());
 }
 @Test @WithMockUser(authorities="PERMISO_GESTIONAR_INVENTARIO") void trabajadorSoloPuedeActualizarStock() throws Exception {
  Long id=productos.findByCodigoIgnoreCase("INV-A").orElseThrow().getId();
  mvc.perform(get("/api/v1/inventario/resumen")).andExpect(status().isOk());
  mvc.perform(delete("/api/v1/productos/"+id)).andExpect(status().isForbidden());
  mvc.perform(patch("/api/v1/inventario/"+id+"/salida").contentType(MediaType.APPLICATION_JSON).content("{\"cantidad\":5}")).andExpect(status().isOk()).andExpect(jsonPath("$.cantidad").value(3));
 }
 @Test @WithMockUser(roles="ADMIN") void editarProductoActualizaCatalogoYSePuedeEliminar() throws Exception {
  Producto p=productos.findByCodigoIgnoreCase("INV-A").orElseThrow();
  String datos="""
   {"codigo":"INV-A","modelo":"Modelo editado","color":"Azul","descripcion":"Nueva descripción","categoriaId":%d,"almacenId":%d,"precio":35.50,"cantidad":4,"stockMinimo":4,"activo":true}
   """.formatted(categoria.getId(),p.getAlmacen().getId());
  mvc.perform(put("/api/v1/productos/"+p.getId()).contentType(MediaType.APPLICATION_JSON).content(datos)).andExpect(status().isOk()).andExpect(jsonPath("$.stockMinimo").value(4));
  mvc.perform(get("/api/v1/productos/"+p.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.modelo").value("Modelo editado")).andExpect(jsonPath("$.precio").value(35.50));
  mvc.perform(delete("/api/v1/productos/"+p.getId())).andExpect(status().isNoContent());
  mvc.perform(get("/api/v1/productos/"+p.getId())).andExpect(status().isNotFound());
 }
 @Test @WithMockUser(roles="ADMIN") void categoriaSeCreaEditaYElimina() throws Exception {
  mvc.perform(post("/api/v1/categorias").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Categoria nueva\"}")).andExpect(status().isCreated());
  Long id=categorias.findByNombreIgnoreCase("Categoria nueva").orElseThrow().getId();
  mvc.perform(put("/api/v1/categorias/"+id).contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Categoria editada\"}")).andExpect(status().isOk());
  mvc.perform(delete("/api/v1/categorias/"+id)).andExpect(status().isNoContent());
 }
 @Test @WithMockUser(roles="ADMIN") void imagenSeAsociaYSePuedeLeer() throws Exception {
  Long id=productos.findByCodigoIgnoreCase("INV-A").orElseThrow().getId();
  byte[] png=java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+a3xkAAAAASUVORK5CYII=");
  mvc.perform(multipart("/api/v1/productos/"+id+"/imagen").file(new MockMultipartFile("imagen","foto.png","image/png",png))).andExpect(status().isOk()).andExpect(jsonPath("$.imagenUrl").value("/api/v1/productos/"+id+"/imagen"));
  mvc.perform(get("/api/v1/productos/"+id+"/imagen")).andExpect(status().isOk()).andExpect(content().bytes(png));
  mvc.perform(multipart("/api/v1/productos/"+id+"/imagen").file(new MockMultipartFile("imagen","falso.png","image/png","no soy imagen".getBytes()))).andExpect(status().isBadRequest());
  String nombre=productos.findById(id).orElseThrow().getImagenUrl();
  java.nio.file.Files.deleteIfExists(java.nio.file.Path.of("uploads/productos",nombre));
 }
}
