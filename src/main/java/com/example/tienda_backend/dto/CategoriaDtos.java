package com.example.tienda_backend.dto;

import com.example.tienda_backend.model.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CategoriaDtos {

    private CategoriaDtos() {
    }

    public record CategoriaRequest(
            @NotBlank(message = "el nombre es obligatorio") @Size(max = 100) String nombre) {
    }

    public record CategoriaResponse(Long id, String nombre) {

        public static CategoriaResponse de(Categoria c) {
            return new CategoriaResponse(c.getId(), c.getNombre());
        }
    }
}