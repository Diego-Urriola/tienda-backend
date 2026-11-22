package com.example.tienda_backend.service;

import com.example.tienda_backend.dto.UsuarioDtos.UsuarioRequest;
import com.example.tienda_backend.dto.UsuarioDtos.UsuarioResponse;
import com.example.tienda_backend.dto.UsuarioDtos.UsuarioUpdateRequest;
import com.example.tienda_backend.exception.ConflictoException;
import com.example.tienda_backend.exception.RecursoNoEncontradoException;
import com.example.tienda_backend.model.Usuario;
import com.example.tienda_backend.repository.BoletaRepository;
import com.example.tienda_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BoletaRepository boletaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.de(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest req) {
        String email = req.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflictoException("hay una cuenta con ese correo");
        }
        Usuario usuario = Usuario.builder()
                .nombre(req.nombre().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .rol(req.rol())
                .build();
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest req) {
        Usuario usuario = buscar(id);
        usuario.setNombre(req.nombre().trim());
        usuario.setRol(req.rol());
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = buscar(id);
        if (boletaRepository.existsByUsuarioId(id)) {
            throw new ConflictoException("No se puede eliminar: el usuario tiene boletas asociadas");
        }
        usuarioRepository.delete(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario " + id + " no encontrado"));
    }
}