package com.example.tienda_backend.service;

import com.example.tienda_backend.dto.BoletaDtos.BoletaRequest;
import com.example.tienda_backend.dto.BoletaDtos.BoletaResponse;
import com.example.tienda_backend.dto.BoletaDtos.ItemRequest;
import com.example.tienda_backend.exception.ConflictoException;
import com.example.tienda_backend.exception.RecursoNoEncontradoException;
import com.example.tienda_backend.model.Boleta;
import com.example.tienda_backend.model.Categoria;
import com.example.tienda_backend.model.Producto;
import com.example.tienda_backend.model.Rol;
import com.example.tienda_backend.model.Usuario;
import com.example.tienda_backend.repository.BoletaRepository;
import com.example.tienda_backend.repository.ProductoRepository;
import com.example.tienda_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoletaServiceTest {

    @Mock
    private BoletaRepository boletaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private BoletaService boletaService;

    private Usuario cliente;
    private Producto polera;

    @BeforeEach
    void preparar() {
        cliente = Usuario.builder().id(1L).nombre("Cliente").email("cliente@tienda.cl")
                .passwordHash("hash").rol(Rol.CLIENTE).build();
        Categoria categoria = new Categoria(1L, "Poleras");
        polera = Producto.builder().id(10L).nombre("Polera Negra").precio(19990).imagen("/Imagenes/p.jpg")
                .stock(10).categoria(categoria).build();
    }

    private BoletaRequest pedido(ItemRequest... items) {
        return new BoletaRequest("Ana", "Perez", "ana@correo.cl", "Calle 1", null,
                "Region Metropolitana de Santiago", "Maipu", null, List.of(items));
    }

    @Test
    void compraValida_recalculaTotal_quedaPagada_yDescuentaStock() {
        when(usuarioRepository.findByEmail("cliente@tienda.cl")).thenReturn(Optional.of(cliente));
        when(productoRepository.buscarParaActualizar(10L)).thenReturn(Optional.of(polera));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(i -> i.getArgument(0));

        BoletaResponse r = boletaService.crear("cliente@tienda.cl", pedido(new ItemRequest(10L, 3)));

        assertEquals("PAGADO", r.estado());
        assertEquals(19990 * 3, r.total());
        assertEquals(7, polera.getStock());
        assertEquals(1, r.items().size());
        assertEquals("ORDER" + r.numero(), r.codigo());
    }

    @Test
    void productoRepetido_sumaCantidades() {
        when(usuarioRepository.findByEmail("cliente@tienda.cl")).thenReturn(Optional.of(cliente));
        when(productoRepository.buscarParaActualizar(10L)).thenReturn(Optional.of(polera));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(i -> i.getArgument(0));

        BoletaResponse r = boletaService.crear("cliente@tienda.cl",
                pedido(new ItemRequest(10L, 1), new ItemRequest(10L, 2)));

        assertEquals(1, r.items().size());
        assertEquals(3, r.items().get(0).cantidad());
        assertEquals(7, polera.getStock());
    }

    @Test
    void totalMayorAlLimite_quedaRechazada_yNoDescuentaStock() {
        polera.setPrecio(60000);
        when(usuarioRepository.findByEmail("cliente@tienda.cl")).thenReturn(Optional.of(cliente));
        when(productoRepository.buscarParaActualizar(10L)).thenReturn(Optional.of(polera));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(i -> i.getArgument(0));

        BoletaResponse r = boletaService.crear("cliente@tienda.cl", pedido(new ItemRequest(10L, 2)));

        assertEquals("RECHAZADO", r.estado());
        assertEquals(120000, r.total());
        assertEquals(10, polera.getStock());
    }

    @Test
    void stockInsuficiente_lanzaConflicto_yNoGuarda() {
        when(usuarioRepository.findByEmail("cliente@tienda.cl")).thenReturn(Optional.of(cliente));
        when(productoRepository.buscarParaActualizar(10L)).thenReturn(Optional.of(polera));

        assertThrows(ConflictoException.class,
                () -> boletaService.crear("cliente@tienda.cl", pedido(new ItemRequest(10L, 11))));

        verify(boletaRepository, never()).save(any());
        assertEquals(10, polera.getStock());
    }

    @Test
    void productoInexistente_lanzaNoEncontrado() {
        when(usuarioRepository.findByEmail("cliente@tienda.cl")).thenReturn(Optional.of(cliente));
        when(productoRepository.buscarParaActualizar(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> boletaService.crear("cliente@tienda.cl", pedido(new ItemRequest(99L, 1))));
    }

    @Test
    void usuarioInexistente_lanzaNoEncontrado() {
        when(usuarioRepository.findByEmail("nadie@tienda.cl")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> boletaService.crear("nadie@tienda.cl", pedido(new ItemRequest(10L, 1))));
    }
}