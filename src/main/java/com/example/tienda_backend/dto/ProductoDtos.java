package com.example.tienda_backend.dto;

import com.example.tienda_backend.model.Producto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class ProductoDtos {

    private ProductoDtos() {
    }

    public record ProductoRequest(
            @NotBlank(message = "el nombre es obligatorio") @Size(max = 150) String nombre,
            @NotNull(message = "el precio es obligatorio") @Positive(message = "el precio debe ser mayor a 0") Integer precio,
            @NotBlank(message = "la imagen es obligatoria") String imagen,
            @Size(max = 500) String descripcion,
            @NotNull(message = "el stock es obligatorio") @Min(value = 0, message = "el stock no puede ser negativo") Integer stock,
            @NotNull(message = "la categoria es obligatoria") Long categoriaId) {
    }

    public record ProductoResponse(Long id, String nombre, Integer precio, String imagen, String alt,
                                   String descripcion, Integer stock, Long categoriaId, String categoriaNombre) {

        public static ProductoResponse de(Producto p) {
            return new ProductoResponse(p.getId(), p.getNombre(), p.getPrecio(), p.getImagen(), p.getNombre(),
                    p.getDescripcion(), p.getStock(), p.getCategoria().getId(), p.getCategoria().getNombre());
        }
    }
}