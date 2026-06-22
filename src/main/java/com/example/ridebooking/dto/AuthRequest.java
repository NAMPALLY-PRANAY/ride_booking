package com.example.ridebooking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        String name,
        @Email @NotBlank String email,
        @NotBlank String password,
        String phoneNumber,
        String vehicleNumber,
        Boolean available
) {
}
