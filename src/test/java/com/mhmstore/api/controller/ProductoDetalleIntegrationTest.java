package com.mhmstore.api.controller;

import com.mhmstore.api.model.*;
import com.mhmstore.api.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import java.math.BigDecimal;
import java.io.ByteArrayOutputStream;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties="app.storage.product-images=target/test-details-images")
@AutoConfigureMockMvc
@Transactional
class ProductoDetalleIntegrationTest {
 @Autowired MockMvc mvc;
 @Autowired ProductoRepository productos;
 @Autowired CategoriaRepository categorias;
 @Autowired AlmacenRepository almacenes;
 @Autowired ProductoImagenRepository imagenes;
 Producto rojo,azul;
 @BeforeEach void preparar(){
  Categoria c=categorias.save(new Categoria("Detalle mochilas","Coleccion"));
  Almacen a=almacenes.save(new Almacen("Detalle almacen","Local",""));
  rojo=crear("DET-ROJO","Rojo",c,a,"80.00");rojo.setColorHex("#a83c3c");rojo.setMaterial("Lona");rojo.setCapacidad("20 L");rojo.setDimensiones("40 x 30 x 15 cm");rojo.setPeso("600 g");productos.saveAndFlush(rojo);
  azul=crear("DET-AZUL","Azul",c,a,"90.00");
  Producto oculto=crear("DET-OCULTO","Negro",c,a,"70.00");oculto.setActivo(false);productos.saveAndFlush(oculto);
  crear("DET-OTRA","Verde",categorias.save(new Categoria("Otra categoria detalle","")),a,"60.00");
 }
 Producto crear(String sku,String color,Categoria c,Almacen a,String precio){return productos.saveAndFlush(new Producto(null,sku,"Modelo detalle",color,c,new BigDecimal(precio),4,a));}
 byte[] png()throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2,2,java.awt.image.BufferedImage.TYPE_INT_RGB),"png",out);return out.toByteArray();}
 @Test void variantesSoloPublicadasDelMismoModeloYCategoria()throws Exception{
  mvc.perform(get("/api/v1/productos/"+rojo.getId()+"/detalle")).andExpect(status().isOk()).andExpect(jsonPath("$.variantes.length()").value(2)).andExpect(jsonPath("$.producto.colorHex").value("#a83c3c")).andExpect(jsonPath("$.producto.material").value("Lona")).andExpect(jsonPath("$.producto.capacidad").value("20 L"));
  mvc.perform(get("/api/v1/productos/"+azul.getId()+"/detalle")).andExpect(status().isOk()).andExpect(jsonPath("$.producto.color").value("Azul")).andExpect(jsonPath("$.producto.precio").value(90.00));
 }
 @Test @WithMockUser(roles="ADMIN") void fotoPerteneceSoloAlSkuSeleccionado()throws Exception{
  byte[] bytes=png();mvc.perform(multipart("/api/v1/productos/"+rojo.getId()+"/galeria").file(new MockMultipartFile("imagen","rojo.png","image/png",bytes))).andExpect(status().isCreated());
  Long id=imagenes.findByProductoIdOrderByIdAsc(rojo.getId()).get(0).getId();
  mvc.perform(get("/api/v1/productos/"+rojo.getId()+"/detalle")).andExpect(jsonPath("$.imagenes.length()").value(1));
  mvc.perform(get("/api/v1/productos/"+azul.getId()+"/detalle")).andExpect(jsonPath("$.imagenes.length()").value(0));
  mvc.perform(get("/api/v1/productos/"+rojo.getId()+"/galeria/"+id)).andExpect(status().isOk()).andExpect(content().bytes(bytes));
  mvc.perform(delete("/api/v1/productos/"+azul.getId()+"/galeria/"+id)).andExpect(status().isNotFound());
  mvc.perform(delete("/api/v1/productos/"+rojo.getId()+"/galeria/"+id)).andExpect(status().isNoContent());
  assertThat(imagenes.findByProductoIdOrderByIdAsc(rojo.getId())).isEmpty();
 }
 @Test @WithMockUser(roles="ADMIN") void imagenPrincipalSeGuardaEnElColorElegido()throws Exception{
  mvc.perform(multipart("/api/v1/productos/"+rojo.getId()+"/imagen").file(new MockMultipartFile("imagen","azul.png","image/png",png())).param("varianteId",azul.getId().toString())).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(azul.getId())).andExpect(jsonPath("$.color").value("Azul"));
  assertThat(productos.findById(rojo.getId()).orElseThrow().getImagenUrl()).isNull();
  assertThat(productos.findById(azul.getId()).orElseThrow().getImagenUrl()).isNotBlank();
  mvc.perform(get("/api/v1/productos/"+azul.getId()+"/imagen")).andExpect(status().isOk()).andExpect(content().bytes(png()));
  java.nio.file.Files.deleteIfExists(java.nio.file.Path.of("target/test-details-images",productos.findById(azul.getId()).orElseThrow().getImagenUrl()));
 }
 @Test @WithMockUser(roles="ADMIN") void galeriaRespetaColorElegidoYRechazaOtraMochila()throws Exception{
  mvc.perform(multipart("/api/v1/productos/"+rojo.getId()+"/galeria").file(new MockMultipartFile("imagen","azul.png","image/png",png())).param("varianteId",azul.getId().toString())).andExpect(status().isCreated());
  assertThat(imagenes.countByProductoId(rojo.getId())).isZero();assertThat(imagenes.countByProductoId(azul.getId())).isEqualTo(1);
  Producto otra=productos.findByCodigoIgnoreCase("DET-OTRA").orElseThrow();
  for(String ruta:new String[]{"imagen","galeria"}) mvc.perform(multipart("/api/v1/productos/"+rojo.getId()+"/"+ruta).file(new MockMultipartFile("imagen","foto.png","image/png",png())).param("varianteId",otra.getId().toString())).andExpect(status().isBadRequest());
  assertThat(imagenes.countByProductoId(otra.getId())).isZero();assertThat(otra.getImagenUrl()).isNull();
 }
 @Test void variantesPublicasExcluyenColoresOcultos()throws Exception{
  mvc.perform(get("/api/v1/productos/"+rojo.getId()+"/variantes")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
 }
 @Test @WithMockUser(roles="ADMIN") void gestorPuedeElegirTambienUnColorOculto()throws Exception{
  mvc.perform(get("/api/v1/productos/"+rojo.getId()+"/variantes")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
 }
 @Test @WithMockUser(roles="CLIENTE") void clienteNoPuedeSubirFotos()throws Exception{
  mvc.perform(multipart("/api/v1/productos/"+rojo.getId()+"/galeria").file(new MockMultipartFile("imagen","foto.png","image/png",png()))).andExpect(status().isForbidden());
 }
 @Test @WithMockUser(roles="ADMIN") void rechazaImagenTruncadaAunqueTengaCabeceraPng()throws Exception{
  mvc.perform(multipart("/api/v1/productos/"+rojo.getId()+"/galeria").file(new MockMultipartFile("imagen","falso.png","image/png",new byte[]{(byte)0x89,0x50,0x4e,0x47,13,10,26,10}))).andExpect(status().isBadRequest());
 }
 @Test @WithMockUser(roles="ADMIN") void especificacionesSeGuardanYColorHexSeValida()throws Exception{
  String json="""
  {"codigo":"DET-NUEVO","modelo":"Modelo nuevo","color":"Rojo","colorHex":"%s","categoriaId":%d,"almacenId":%d,"precio":120.00,"cantidad":3,"material":"Nylon","capacidad":"25 L","peso":"500 g","dimensiones":"40 x 30 cm"}
  """;
  mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content(json.formatted("#cc3333",rojo.getCategoria().getId(),rojo.getAlmacen().getId()))).andExpect(status().isCreated()).andExpect(jsonPath("$.material").value("Nylon")).andExpect(jsonPath("$.peso").value("500 g"));
  mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content(json.formatted("color-invalido",rojo.getCategoria().getId(),rojo.getAlmacen().getId()))).andExpect(status().isBadRequest());
 }
}
