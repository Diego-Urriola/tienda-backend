package com.example.tienda_backend.controller;

import com.example.tienda_backend.dto.BoletaDtos.BoletaRequest;
import com.example.tienda_backend.dto.BoletaDtos.BoletaResponse;
import com.example.tienda_backend.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/boletas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Boletas (ordenes)", description = "Ver: ADMIN y VENDEDOR. Comprar: CLIENTE y ADMIN")
public class BoletaController {

    private final BoletaService boletaService;

    @GetMapping
    @Operation(summary = "Listar boletas (ADMIN, VENDEDOR)")
    public List<BoletaResponse> listar() {
        return boletaService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una boleta (ADMIN, VENDEDOR)")
    public BoletaResponse obtener(@PathVariable Long id) {
        return boletaService.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Finalizar compra (CLIENTE, ADMIN)",
            description = "Responde 201 tanto si el pago queda PAGADO como RECHAZADO; revisa el campo estado.")
    public ResponseEntity<BoletaResponse> crear(Authentication authentication, @Valid @RequestBody BoletaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boletaService.crear(authentication.getName(), req));
    }
}