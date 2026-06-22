package com.example.ridebooking.controller;

import com.example.ridebooking.dto.AuthRequest;
import com.example.ridebooking.dto.AuthResponse;
import com.example.ridebooking.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/customer/register")
    public AuthResponse registerCustomer(@Valid @RequestBody AuthRequest request) {
        return authService.registerCustomer(request);
    }

    @PostMapping("/customer/login")
    public AuthResponse loginCustomer(@Valid @RequestBody AuthRequest request) {
        return authService.loginCustomer(request);
    }

    @PostMapping("/driver/register")
    public AuthResponse registerDriver(@Valid @RequestBody AuthRequest request) {
        return authService.registerDriver(request);
    }

    @PostMapping("/driver/login")
    public AuthResponse loginDriver(@Valid @RequestBody AuthRequest request) {
        return authService.loginDriver(request);
    }
}
