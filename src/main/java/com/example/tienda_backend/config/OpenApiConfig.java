package com.example.tienda_backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "API Tienda Stokdark",
        version = "v1",
        description = "Backend REST de la tienda: productos, categorias, usuarios, boletas y autenticacion JWT"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Pega el token que devuelve /api/v1/auth/login (sin la palabra Bearer)")
public class OpenApiConfig {
}