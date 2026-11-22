package com.example.tienda_backend.exception;

import java.time.Instant;

public record ApiError(String timestamp, int status, String message) {

    public static ApiError de(int status, String message) {
        return new ApiError(Instant.now().toString(), status, message);
    }
}