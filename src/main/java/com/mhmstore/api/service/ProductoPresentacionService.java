package com.mhmstore.api.service;

import com.mhmstore.api.dto.ProductoResponse;
import com.mhmstore.api.model.ProductoImagen;
import com.mhmstore.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.support.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;

@Service
@Transactional
public class ProductoPresentacionService {
 private final ProductoRepository productos;
 private final ProductoImagenRepository imagenes;
 private final ProductoServiceImpl servicio;
 private final Path directorio;
 public ProductoPresentacionService(ProductoRepository p, ProductoImagenRepository i, ProductoServiceImpl s,
   @Value("${app.storage.product-images:uploads/productos}") String carpeta) {productos=p;imagenes=i;servicio=s;directorio=Path.of(carpeta).toAbsolutePath().normalize();}
 public record Foto(Long id, String url) {}
 public record Detalle(ProductoResponse producto, List<ProductoResponse> variantes, List<Foto> imagenes) {}
 @Transactional(readOnly=true)
 public Detalle detalle(Long id) {
  ProductoResponse p=servicio.obtener(id);
  return new Detalle(p, productos.findByModeloIgnoreCaseAndCategoriaIdAndActivoTrueOrderByIdAsc(p.modelo(),p.categoriaId()).stream().map(servicio::toResponse).toList(), listar(id));
 }
 @Transactional(readOnly=true)
 public List<Foto> listar(Long id) { servicio.obtener(id);return imagenes.findByProductoIdOrderByIdAsc(id).stream().map(i->foto(id,i)).toList(); }
 private Foto foto(Long id,ProductoImagen imagen){return new Foto(imagen.getId(),"/api/v1/productos/"+id+"/galeria/"+imagen.getId());}
 @Transactional(readOnly=true)
 public List<ProductoResponse> variantes(Long id) {
  ProductoResponse p=servicio.obtener(id);
  return productos.findByModeloIgnoreCaseAndCategoriaIdOrderByIdAsc(p.modelo(),p.categoriaId()).stream()
    .filter(v->v.getActivo() || servicio.puedeGestionar()).map(servicio::toResponse).toList();
 }
 public Foto agregar(Long id, MultipartFile file, Long varianteId) {
  return agregar(servicio.resolverColorImagen(id,varianteId),file);
 }
 public Foto agregar(Long id, MultipartFile file) {
  var producto=productos.findByIdForUpdate(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  if(imagenes.countByProductoId(id)>=6)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Máximo seis imágenes adicionales por color.");
  if(file.isEmpty()||file.getSize()>5L*1024*1024)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La imagen debe pesar hasta 5 MB.");
  try {
   byte[] bytes=file.getBytes();String tipo=tipo(bytes);
   if(tipo==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Selecciona una imagen JPG, PNG o WebP válida.");
   if(!tipo.equals("image/webp") && javax.imageio.ImageIO.read(new ByteArrayInputStream(bytes))==null)throw new IOException();
   String nombre=UUID.randomUUID()+(tipo.equals("image/png")?".png":tipo.equals("image/webp")?".webp":".jpg");
   Files.createDirectories(directorio);Files.write(directorio.resolve(nombre),bytes);
   TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){@Override public void afterCompletion(int status){if(status!=STATUS_COMMITTED)borrarArchivo(nombre);}});
   ProductoImagen imagen=new ProductoImagen();imagen.setProducto(producto);imagen.setArchivo(nombre);imagen.setContentType(tipo);
   return foto(id,imagenes.save(imagen));
  }catch(IOException ex){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"No se pudo leer o guardar la imagen.");}
 }
 @Transactional(readOnly=true)
 public ProductoService.ImagenProducto leer(Long productoId,Long imagenId) {
  servicio.obtener(productoId);
  ProductoImagen imagen=buscar(productoId,imagenId);
  try{return new ProductoService.ImagenProducto(Files.readAllBytes(directorio.resolve(Path.of(imagen.getArchivo()).getFileName())),MediaType.parseMediaType(imagen.getContentType()));}
  catch(IOException ex){throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Imagen no disponible.");}
 }
 public void eliminar(Long productoId,Long imagenId){
  ProductoImagen imagen=buscar(productoId,imagenId);String archivo=imagen.getArchivo();imagenes.delete(imagen);
  TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){@Override public void afterCommit(){borrarArchivo(archivo);}});
 }
 private ProductoImagen buscar(Long productoId,Long imagenId){return imagenes.findByIdAndProductoId(imagenId,productoId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 private void borrarArchivo(String nombre){try{Files.deleteIfExists(directorio.resolve(Path.of(nombre).getFileName()));}catch(IOException ignored){}}
 private String tipo(byte[] b){
  if(b.length>=8 && b[0]==(byte)0x89 && b[1]==0x50 && b[2]==0x4e && b[3]==0x47 && b[4]==13 && b[5]==10 && b[6]==26 && b[7]==10)return "image/png";
  if(b.length>=4 && b[0]==(byte)0xff && b[1]==(byte)0xd8 && b[2]==(byte)0xff)return "image/jpeg";
  if(b.length>=20 && b[0]=='R' && b[1]=='I' && b[2]=='F' && b[3]=='F' && b[8]=='W' && b[9]=='E' && b[10]=='B' && b[11]=='P' && b[12]=='V' && b[13]=='P' && b[14]=='8' && (b[15]==' '||b[15]=='L'||b[15]=='X'))return "image/webp";
  return null;
 }
}
