package com.example.tienda_backend.config;

import com.example.tienda_backend.model.Categoria;
import com.example.tienda_backend.model.Producto;
import com.example.tienda_backend.model.Rol;
import com.example.tienda_backend.model.Usuario;
import com.example.tienda_backend.repository.CategoriaRepository;
import com.example.tienda_backend.repository.ProductoRepository;
import com.example.tienda_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            crearUsuario("Administrador", "admin@tienda.cl", "Admin123!", Rol.ADMIN);
            crearUsuario("Vendedor", "vendedor@tienda.cl", "Vendedor123!", Rol.VENDEDOR);
            crearUsuario("Cliente", "cliente@tienda.cl", "Cliente123!", Rol.CLIENTE);
        }

        if (categoriaRepository.count() == 0 && productoRepository.count() == 0) {
            Categoria gorros = crearCategoria("Gorros");
            Categoria poleras = crearCategoria("Poleras");
            Categoria polerones = crearCategoria("Polerones");
            Categoria zapatillas = crearCategoria("Zapatillas");

            crearProducto("Gorro Blokeet", 19990, "/Imagenes/gorro.jpg", "Gorro negro con logo.", gorros);
            crearProducto("Polera cafe Blokeet", 19990, "/Imagenes/poleracafe.jpg", "Polera café de algodón.", poleras);
            crearProducto("Polera beige Blokeet", 19990, "/Imagenes/poleraclara.jpg", "Polera beige con logo.", poleras);
            crearProducto("Polera Negra Blokeet", 19990, "/Imagenes/poleranegralogo.jpg", "Polera negra con logo.", poleras);
            crearProducto("Poleron beige Blokeet", 39990, "/Imagenes/poleronclarologo.jpg", "Polerón beige con capucha.", polerones);
            crearProducto("Poleron gris Blokeet", 39990, "/Imagenes/poleronfgrislogo.jpg", "Polerón gris con capucha.", polerones);
            crearProducto("Poleron negro Blokeet", 39990, "/Imagenes/poleronnegrologo.jpg", "Polerón negro con capucha.", polerones);
            crearProducto("Zapatillas negro Blokeet", 49990, "/Imagenes/tillas.jpg", "Zapatillas negras.", zapatillas);
            crearProducto("Zapatillas verdes Blokeet", 49990, "/Imagenes/tillasverdes.jpg", "Zapatillas verdes.", zapatillas);
        }
    }

    private Categoria crearCategoria(String nombre) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        return categoriaRepository.save(categoria);
    }

    private void crearUsuario(String nombre, String email, String password, Rol rol) {
        usuarioRepository.save(Usuario.builder()
                .nombre(nombre)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .rol(rol)
                .build());
    }

    private void crearProducto(String nombre, int precio, String imagen, String descripcion, Categoria categoria) {
        productoRepository.save(Producto.builder()
                .nombre(nombre)
                .precio(precio)
                .imagen(imagen)
                .descripcion(descripcion)
                .stock(20)
                .categoria(categoria)
                .build());
    }
}