package com.mhmstore.api.exception;

public class ProductoNotFoundException extends RuntimeException {

    public ProductoNotFoundException(Long id) {
        super("No existe el producto con id " + id);
    }
}