package com.mhmstore.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MHM STORE API",
                version = "1.0",
                description = "API REST para la gestión del inventario de la tienda de mochilas MHM STORE."
        )
)
public class OpenApiConfig {
}