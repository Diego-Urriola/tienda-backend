package com.example.tienda_backend.service;

import com.example.tienda_backend.dto.ProductoDtos.ProductoRequest;
import com.example.tienda_backend.dto.ProductoDtos.ProductoResponse;
import com.example.tienda_backend.exception.RecursoNoEncontradoException;
import com.example.tienda_backend.model.Categoria;
import com.example.tienda_backend.model.Producto;
import com.example.tienda_backend.repository.CategoriaRepository;
import com.example.tienda_backend.repository.DetalleBoletaRepository;
import com.example.tienda_backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final DetalleBoletaRepository detalleBoletaRepository;

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(Long categoriaId) {
        List<Producto> productos = categoriaId == null
                ? productoRepository.findAll()
                : productoRepository.findByCategoriaId(categoriaId);
        return productos.stream().map(ProductoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return ProductoResponse.de(buscar(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest req) {
        Producto producto = new Producto();
        aplicar(producto, req);
        return ProductoResponse.de(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest req) {
        Producto producto = buscar(id);
        aplicar(producto, req);
        return ProductoResponse.de(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscar(id);
        detalleBoletaRepository.desvincularProducto(id);
        productoRepository.delete(producto);
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto " + id + " no encontrado"));
    }

    private void aplicar(Producto producto, ProductoRequest req) {
        Categoria categoria = categoriaRepository.findById(req.categoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria " + req.categoriaId() + " no encontrada"));
        producto.setNombre(req.nombre().trim());
        producto.setPrecio(req.precio());
        producto.setImagen(req.imagen().trim());
        producto.setDescripcion(req.descripcion());
        producto.setStock(req.stock());
        producto.setCategoria(categoria);
    }
}