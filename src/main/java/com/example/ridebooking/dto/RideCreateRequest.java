package com.example.ridebooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RideCreateRequest(
        @NotBlank String pickupLocation,
        @NotBlank String dropLocation,
        @NotNull Double fare
) {
}
