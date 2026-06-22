package com.example.ridebooking.dto;

public record AuthResponse(
        String token,
        String role,
        Long id,
        String name,
        String email
) {
}
