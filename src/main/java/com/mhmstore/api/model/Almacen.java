package com.mhmstore.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "almacenes")
public class Almacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del almacén es obligatorio")
    @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @NotBlank(message = "La ubicación del almacén es obligatoria")
    @Size(max = 150, message = "La ubicación no puede superar los 150 caracteres")
    @Column(nullable = false, length = 150)
    private String ubicacion;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    @Column(length = 255)
    private String descripcion;

    public Almacen() {
    }

    public Almacen(String nombre, String ubicacion, String descripcion) {
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}