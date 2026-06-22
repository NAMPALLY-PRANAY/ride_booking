package com.example.ridebooking.service;

import com.example.ridebooking.dto.AuthRequest;
import com.example.ridebooking.dto.AuthResponse;
import com.example.ridebooking.entity.Customer;
import com.example.ridebooking.entity.Driver;
import com.example.ridebooking.repository.CustomerRepository;
import com.example.ridebooking.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse registerCustomer(AuthRequest request) {
        requireUniqueEmail(request.email());
        requireText(request.name(), "Customer name is required");
        requireText(request.phoneNumber(), "Customer phone number is required");

        Customer customer = Customer.builder()
                .name(request.name().trim())
                .email(request.email().trim().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber().trim())
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        return new AuthResponse(jwtService.generateToken(savedCustomer.getEmail(), "CUSTOMER"), "CUSTOMER", savedCustomer.getId(), savedCustomer.getName(), savedCustomer.getEmail());
    }

    public AuthResponse loginCustomer(AuthRequest request) {
        Customer customer = customerRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Customer not found"));

        if (!passwordEncoder.matches(request.password(), customer.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid customer credentials");
        }

        return new AuthResponse(jwtService.generateToken(customer.getEmail(), "CUSTOMER"), "CUSTOMER", customer.getId(), customer.getName(), customer.getEmail());
    }

    public AuthResponse registerDriver(AuthRequest request) {
        requireUniqueEmail(request.email());
        requireText(request.name(), "Driver name is required");
        requireText(request.vehicleNumber(), "Vehicle number is required");

        Driver driver = Driver.builder()
                .name(request.name().trim())
                .email(request.email().trim().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .vehicleNumber(request.vehicleNumber().trim())
                .available(request.available() == null || request.available())
                .build();

        Driver savedDriver = driverRepository.save(driver);
        return new AuthResponse(jwtService.generateToken(savedDriver.getEmail(), "DRIVER"), "DRIVER", savedDriver.getId(), savedDriver.getName(), savedDriver.getEmail());
    }

    public AuthResponse loginDriver(AuthRequest request) {
        Driver driver = driverRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Driver not found"));

        if (!passwordEncoder.matches(request.password(), driver.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid driver credentials");
        }

        return new AuthResponse(jwtService.generateToken(driver.getEmail(), "DRIVER"), "DRIVER", driver.getId(), driver.getName(), driver.getEmail());
    }

    private void requireUniqueEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (customerRepository.findByEmail(normalizedEmail).isPresent() || driverRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
