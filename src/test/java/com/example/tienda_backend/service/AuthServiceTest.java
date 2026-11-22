package com.example.tienda_backend.service;

import com.example.tienda_backend.dto.AuthDtos.AuthResponse;
import com.example.tienda_backend.dto.AuthDtos.LoginRequest;
import com.example.tienda_backend.dto.AuthDtos.RegistroRequest;
import com.example.tienda_backend.exception.ConflictoException;
import com.example.tienda_backend.model.Rol;
import com.example.tienda_backend.model.Usuario;
import com.example.tienda_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registrar_creaClienteConContrasenaEncriptada() {
        when(usuarioRepository.existsByEmail("ana@correo.cl")).thenReturn(false);
        when(passwordEncoder.encode("secreto1")).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("token-falso");

        AuthResponse r = authService.registrar(new RegistroRequest("Ana", " ANA@correo.cl ", "secreto1"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertEquals(Rol.CLIENTE, guardado.getRol());
        assertEquals("HASH", guardado.getPasswordHash());
        assertNotEquals("secreto1", guardado.getPasswordHash());
        assertEquals("CLIENTE", r.rol());
        assertEquals("token-falso", r.token());
    }

    @Test
    void registrar_conCorreoRepetido_lanzaConflicto() {
        when(usuarioRepository.existsByEmail("ana@correo.cl")).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> authService.registrar(new RegistroRequest("Ana", "ana@correo.cl", "secreto1")));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void login_correcto_devuelveTokenYRol() {
        Usuario u = Usuario.builder().id(2L).nombre("Vendedor").email("v@tienda.cl")
                .passwordHash("HASH").rol(Rol.VENDEDOR).build();
        when(usuarioRepository.findByEmail("v@tienda.cl")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("clave123", "HASH")).thenReturn(true);
        when(jwtService.generarToken(u)).thenReturn("tok");

        AuthResponse r = authService.login(new LoginRequest("v@tienda.cl", "clave123"));

        assertEquals("VENDEDOR", r.rol());
        assertEquals("tok", r.token());
    }

    @Test
    void login_conContrasenaIncorrecta_lanzaBadCredentials() {
        Usuario u = Usuario.builder().id(2L).nombre("Vendedor").email("v@tienda.cl")
                .passwordHash("HASH").rol(Rol.VENDEDOR).build();
        when(usuarioRepository.findByEmail("v@tienda.cl")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("mala", "HASH")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("v@tienda.cl", "mala")));
    }

    @Test
    void login_conCorreoInexistente_lanzaBadCredentials() {
        when(usuarioRepository.findByEmail("x@tienda.cl")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("x@tienda.cl", "clave123")));
    }
}
