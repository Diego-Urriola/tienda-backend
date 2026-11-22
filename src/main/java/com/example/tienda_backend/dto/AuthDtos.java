package com.example.tienda_backend.dto;

import com.example.tienda_backend.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank(message = "el correo es obligatorio") @Email(message = "correo invalido") String email,
            @NotBlank(message = "la contrasena es obligatoria") String password) {
    }

    public record RegistroRequest(
            @NotBlank(message = "el nombre es obligatorio") @Size(max = 100) String nombre,
            @NotBlank(message = "el correo es obligatorio") @Email(message = "correo invalido") @Size(max = 150) String email,
            @NotBlank(message = "la contrasena es obligatoria")
            @Size(min = 6, max = 100, message = "la contrasena debe tener al menos 6 caracteres") String password) {
    }

    public record AuthResponse(String token, Long id, String nombre, String email, String rol) {

        public static AuthResponse de(String token, Usuario u) {
            return new AuthResponse(token, u.getId(), u.getNombre(), u.getEmail(), u.getRol().name());
        }
    }
}