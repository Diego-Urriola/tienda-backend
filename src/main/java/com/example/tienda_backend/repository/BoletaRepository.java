package com.example.tienda_backend.repository;

import com.example.tienda_backend.model.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    List<Boleta> findAllByOrderByFechaDesc();

    boolean existsByNumero(Integer numero);

    boolean existsByUsuarioId(Long usuarioId);
}