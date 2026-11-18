package com.mhmstore.api.model;
import jakarta.persistence.*;
@Entity
@Table(name="producto_imagenes")
public class ProductoImagen {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="producto_id",nullable=false) private Producto producto;
 @Column(nullable=false,length=255) private String archivo;
 @Column(name="content_type",nullable=false,length=32) private String contentType;
 public Long getId(){return id;}
 public Producto getProducto(){return producto;}
 public void setProducto(Producto p){producto=p;}
 public String getArchivo(){return archivo;}
 public void setArchivo(String a){archivo=a;}
 public String getContentType(){return contentType;}
 public void setContentType(String t){contentType=t;}
}
