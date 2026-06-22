package com.example.ridebooking.controller;

import com.example.ridebooking.dto.RideCreateRequest;
import com.example.ridebooking.dto.RideResponse;
import com.example.ridebooking.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    @PostMapping
    public RideResponse createRide(@Valid @RequestBody RideCreateRequest request, Authentication authentication) {
        return rideService.createRide(authentication.getName(), request);
    }

    @GetMapping("/{id}")
    public RideResponse getRide(@PathVariable Long id, Authentication authentication) {
        return rideService.getRide(id, authentication.getName());
    }

    @DeleteMapping("/{id}")
    public void cancelRide(@PathVariable Long id, Authentication authentication) {
        rideService.cancelRide(id, authentication.getName());
    }

    @GetMapping("/driver/rides/available")
    public List<RideResponse> availableRides() {
        return rideService.getAvailableRides();
    }

    @PostMapping("/driver/rides/{rideId}/accept")
    public RideResponse acceptRide(@PathVariable Long rideId, Authentication authentication) {
        return rideService.acceptRide(rideId, authentication.getName());
    }

    @PostMapping("/driver/rides/{rideId}/start")
    public RideResponse startRide(@PathVariable Long rideId, Authentication authentication) {
        return rideService.startRide(rideId, authentication.getName());
    }

    @PostMapping("/driver/rides/{rideId}/complete")
    public RideResponse completeRide(@PathVariable Long rideId, Authentication authentication) {
        return rideService.completeRide(rideId, authentication.getName());
    }
}
