package com.example.ridebooking.dto;

import com.example.ridebooking.entity.RideStatus;

public record RideResponse(
        Long id,
        String pickupLocation,
        String dropLocation,
        Double fare,
        RideStatus status,
        String customerName,
        String driverName
) {
}
