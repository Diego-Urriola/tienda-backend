package com.example.tienda_backend.dto;

import com.example.tienda_backend.model.Rol;
import com.example.tienda_backend.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class UsuarioDtos {

    private UsuarioDtos() {
    }

    public record UsuarioRequest(
            @NotBlank(message = "el nombre es obligatorio") @Size(max = 100) String nombre,
            @NotBlank(message = "el correo es obligatorio") @Email(message = "correo invalido") @Size(max = 150) String email,
            @NotBlank(message = "la contrasena es obligatoria")
            @Size(min = 6, max = 100, message = "la contrasena debe tener al menos 6 caracteres") String password,
            @NotNull(message = "el rol es obligatorio") Rol rol) {
    }

    public record UsuarioUpdateRequest(
            @NotBlank(message = "el nombre es obligatorio") @Size(max = 100) String nombre,
            @NotNull(message = "el rol es obligatorio") Rol rol) {
    }

    public record UsuarioResponse(Long id, String nombre, String email, String rol, LocalDateTime creado) {

        public static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.getId(), u.getNombre(), u.getEmail(), u.getRol().name(), u.getCreado());
        }
    }
}