package com.example.tienda_backend.service;

import com.example.tienda_backend.dto.AuthDtos.AuthResponse;
import com.example.tienda_backend.dto.AuthDtos.LoginRequest;
import com.example.tienda_backend.dto.AuthDtos.RegistroRequest;
import com.example.tienda_backend.exception.ConflictoException;
import com.example.tienda_backend.exception.RecursoNoEncontradoException;
import com.example.tienda_backend.model.Rol;
import com.example.tienda_backend.model.Usuario;
import com.example.tienda_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse registrar(RegistroRequest req) {
        String email = normalizar(req.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflictoException("hay una cuenta con ese correo");
        }
        Usuario usuario = Usuario.builder()
                .nombre(req.nombre().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .rol(Rol.CLIENTE)
                .build();
        usuario = usuarioRepository.save(usuario);
        return AuthResponse.de(jwtService.generarToken(usuario), usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByEmail(normalizar(req.email()))
                .orElseThrow(() -> new BadCredentialsException("Correo o contraseña incorrectos"));
        if (!passwordEncoder.matches(req.password(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Correo o contraseña incorrectos");
        }
        return AuthResponse.de(jwtService.generarToken(usuario), usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse refrescar(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return AuthResponse.de(jwtService.generarToken(usuario), usuario);
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase();
    }
}