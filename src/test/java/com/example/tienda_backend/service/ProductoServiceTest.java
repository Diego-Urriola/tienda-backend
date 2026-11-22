package com.example.tienda_backend.service;

import com.example.tienda_backend.dto.ProductoDtos.ProductoRequest;
import com.example.tienda_backend.dto.ProductoDtos.ProductoResponse;
import com.example.tienda_backend.exception.RecursoNoEncontradoException;
import com.example.tienda_backend.model.Categoria;
import com.example.tienda_backend.model.Producto;
import com.example.tienda_backend.repository.CategoriaRepository;
import com.example.tienda_backend.repository.DetalleBoletaRepository;
import com.example.tienda_backend.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private DetalleBoletaRepository detalleBoletaRepository;

    @InjectMocks
    private ProductoService productoService;

    private final Categoria gorros = new Categoria(1L, "Gorros");

    @Test
    void crear_guardaProductoConSuCategoria() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(gorros));
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        ProductoResponse r = productoService.crear(
                new ProductoRequest("  Gorro Blokeet ", 19990, "/Imagenes/gorro.jpg", "Gorro negro", 5, 1L));

        assertEquals("Gorro Blokeet", r.nombre());
        assertEquals("Gorro Blokeet", r.alt());
        assertEquals("Gorros", r.categoriaNombre());
        assertEquals(5, r.stock());
    }

    @Test
    void crear_conCategoriaInexistente_lanzaNoEncontrado() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.crear(
                new ProductoRequest("Gorro", 1000, "/x.jpg", null, 1, 99L)));

        verify(productoRepository, never()).save(any());
    }

    @Test
    void obtener_inexistente_lanzaNoEncontrado() {
        when(productoRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.obtener(5L));
    }

    @Test
    void eliminar_desvinculaElHistorialAntesDeBorrar() {
        Producto p = Producto.builder().id(7L).nombre("Gorro").precio(1000).imagen("/x.jpg")
                .stock(1).categoria(gorros).build();
        when(productoRepository.findById(7L)).thenReturn(Optional.of(p));

        productoService.eliminar(7L);

        verify(detalleBoletaRepository).desvincularProducto(7L);
        verify(productoRepository).delete(p);
    }
}