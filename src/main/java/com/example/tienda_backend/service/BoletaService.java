package com.example.tienda_backend.service;

import com.example.tienda_backend.dto.BoletaDtos.BoletaRequest;
import com.example.tienda_backend.dto.BoletaDtos.BoletaResponse;
import com.example.tienda_backend.dto.BoletaDtos.ItemRequest;
import com.example.tienda_backend.exception.ConflictoException;
import com.example.tienda_backend.exception.RecursoNoEncontradoException;
import com.example.tienda_backend.model.Boleta;
import com.example.tienda_backend.model.DetalleBoleta;
import com.example.tienda_backend.model.EstadoBoleta;
import com.example.tienda_backend.model.Producto;
import com.example.tienda_backend.model.Usuario;
import com.example.tienda_backend.repository.BoletaRepository;
import com.example.tienda_backend.repository.ProductoRepository;
import com.example.tienda_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class BoletaService {

    public static final int LIMITE_PAGO = 100_000;

    private final BoletaRepository boletaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public BoletaResponse crear(String emailUsuario, BoletaRequest req) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (ItemRequest item : req.items()) {
            cantidades.merge(item.productoId(), item.cantidad(), Integer::sum);
        }

        Boleta boleta = Boleta.builder()
                .usuario(usuario)
                .nombre(req.nombre().trim())
                .apellidos(req.apellidos().trim())
                .correo(req.correo().trim())
                .calle(req.calle().trim())
                .departamento(req.departamento())
                .region(req.region())
                .comuna(req.comuna())
                .indicaciones(req.indicaciones())
                .build();

        int total = 0;
        for (Map.Entry<Long, Integer> linea : cantidades.entrySet()) {
            Producto producto = productoRepository.buscarParaActualizar(linea.getKey())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto " + linea.getKey() + " no encontrado"));
            int cantidad = linea.getValue();
            if (producto.getStock() < cantidad) {
                throw new ConflictoException("Stock insuficiente para " + producto.getNombre()
                        + " (disponible: " + producto.getStock() + ")");
            }
            total += producto.getPrecio() * cantidad;
            boleta.getDetalles().add(DetalleBoleta.builder()
                    .boleta(boleta)
                    .producto(producto)
                    .nombre(producto.getNombre())
                    .imagen(producto.getImagen())
                    .precio(producto.getPrecio())
                    .cantidad(cantidad)
                    .build());
        }

        boleta.setTotal(total);
        boolean pagado = total <= LIMITE_PAGO;
        boleta.setEstado(pagado ? EstadoBoleta.PAGADO : EstadoBoleta.RECHAZADO);

        if (pagado) {
            for (DetalleBoleta detalle : boleta.getDetalles()) {
                Producto p = detalle.getProducto();
                p.setStock(p.getStock() - detalle.getCantidad());
            }
        }

        int numero = generarNumero();
        boleta.setNumero(numero);
        boleta.setCodigo("ORDER" + numero);

        return BoletaResponse.de(boletaRepository.save(boleta));
    }

    @Transactional(readOnly = true)
    public List<BoletaResponse> listar() {
        return boletaRepository.findAllByOrderByFechaDesc().stream().map(BoletaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public BoletaResponse obtener(Long id) {
        return boletaRepository.findById(id)
                .map(BoletaResponse::de)
                .orElseThrow(() -> new RecursoNoEncontradoException("Boleta " + id + " no encontrada"));
    }

    private int generarNumero() {
        int numero;
        do {
            numero = ThreadLocalRandom.current().nextInt(10_000_000, 100_000_000);
        } while (boletaRepository.existsByNumero(numero));
        return numero;
    }
}