package com.example.tienda_backend.service;

import com.example.tienda_backend.model.Rol;
import com.example.tienda_backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRETO = "secreto-de-prueba-con-mas-de-32-caracteres-ok";

    private final Usuario admin = Usuario.builder().id(1L).nombre("Admin").email("admin@tienda.cl")
            .passwordHash("x").rol(Rol.ADMIN).build();

    @Test
    void tokenGenerado_sePuedeLeer_yTraeCorreoYRol() {
        JwtService jwt = new JwtService(SECRETO, 60_000);

        Claims claims = jwt.leerToken(jwt.generarToken(admin));

        assertEquals("admin@tienda.cl", claims.getSubject());
        assertEquals("ADMIN", claims.get("rol", String.class));
    }

    @Test
    void tokenManipulado_esRechazado() {
        JwtService jwt = new JwtService(SECRETO, 60_000);
        String token = jwt.generarToken(admin);

        assertThrows(JwtException.class, () -> jwt.leerToken(token + "x"));
    }

    @Test
    void tokenFirmadoConOtroSecreto_esRechazado() {
        JwtService emisor = new JwtService("otro-secreto-distinto-de-mas-de-32-caracteres", 60_000);
        JwtService receptor = new JwtService(SECRETO, 60_000);

        assertThrows(JwtException.class, () -> receptor.leerToken(emisor.generarToken(admin)));
    }

    @Test
    void tokenVencido_esRechazado() {
        JwtService jwt = new JwtService(SECRETO, -1_000);

        assertThrows(JwtException.class, () -> jwt.leerToken(jwt.generarToken(admin)));
    }
}