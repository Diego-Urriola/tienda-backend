package com.example.tienda_backend.repository;

import com.example.tienda_backend.model.DetalleBoleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DetalleBoletaRepository extends JpaRepository<DetalleBoleta, Long> {

    
    @Modifying(clearAutomatically = true)
    @Query("update DetalleBoleta d set d.producto = null where d.producto.id = :productoId")
    void desvincularProducto(@Param("productoId") Long productoId);
}