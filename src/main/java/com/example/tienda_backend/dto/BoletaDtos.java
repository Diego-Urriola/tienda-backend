package com.example.tienda_backend.dto;

import com.example.tienda_backend.model.Boleta;
import com.example.tienda_backend.model.DetalleBoleta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class BoletaDtos {

    private BoletaDtos() {
    }

    public record ItemRequest(
            @NotNull(message = "el producto es obligatorio") Long productoId,
            @NotNull(message = "la cantidad es obligatoria") @Min(value = 1, message = "la cantidad minima es 1") Integer cantidad) {
    }

    public record BoletaRequest(
            @NotBlank(message = "el nombre es obligatorio") @Size(max = 100) String nombre,
            @NotBlank(message = "los apellidos son obligatorios") @Size(max = 100) String apellidos,
            @NotBlank(message = "el correo es obligatorio") @Email(message = "correo invalido") @Size(max = 150) String correo,
            @NotBlank(message = "la calle es obligatoria") @Size(max = 150) String calle,
            @Size(max = 50) String departamento,
            @NotBlank(message = "la region es obligatoria") @Size(max = 100) String region,
            @NotBlank(message = "la comuna es obligatoria") @Size(max = 100) String comuna,
            @Size(max = 500) String indicaciones,
            @NotEmpty(message = "la compra debe tener al menos un producto") @Valid List<ItemRequest> items) {
    }

    public record DetalleResponse(Long id, Long productoId, String nombre, String imagen, String alt,
                                  Integer precio, Integer cantidad, Integer subtotal) {

        public static DetalleResponse de(DetalleBoleta d) {
            Long productoId = d.getProducto() != null ? d.getProducto().getId() : null;
            return new DetalleResponse(d.getId(), productoId, d.getNombre(), d.getImagen(), d.getNombre(),
                    d.getPrecio(), d.getCantidad(), d.getPrecio() * d.getCantidad());
        }
    }

    public record BoletaResponse(Long id, Integer numero, String codigo, String estado, LocalDateTime fecha,
                                 Integer total, Long usuarioId, String nombre, String apellidos, String correo,
                                 String calle, String departamento, String region, String comuna,
                                 String indicaciones, List<DetalleResponse> items) {

        public static BoletaResponse de(Boleta b) {
            return new BoletaResponse(b.getId(), b.getNumero(), b.getCodigo(), b.getEstado().name(), b.getFecha(),
                    b.getTotal(), b.getUsuario().getId(), b.getNombre(), b.getApellidos(), b.getCorreo(),
                    b.getCalle(), b.getDepartamento(), b.getRegion(), b.getComuna(), b.getIndicaciones(),
                    b.getDetalles().stream().map(DetalleResponse::de).toList());
        }
    }
}