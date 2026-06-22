package com.example.ridebooking.service;

import com.example.ridebooking.dto.RideCreateRequest;
import com.example.ridebooking.dto.RideResponse;
import com.example.ridebooking.entity.*;
import com.example.ridebooking.repository.CustomerRepository;
import com.example.ridebooking.repository.DriverRepository;
import com.example.ridebooking.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final CustomerRepository customerRepository;
    private final DriverRepository driverRepository;

    @Transactional
    public RideResponse createRide(String customerEmail, RideCreateRequest request) {
        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        Ride ride = Ride.builder()
                .pickupLocation(request.pickupLocation())
                .dropLocation(request.dropLocation())
                .fare(request.fare())
                .status(RideStatus.REQUESTED)
                .customer(customer)
                .build();
        ride = rideRepository.save(ride);
        return toResponse(ride);
    }

    @Transactional(readOnly = true)
    public RideResponse getRide(Long id, String customerEmail) {
        Ride ride = rideRepository.findByIdAndCustomer_Email(id, customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));
        return toResponse(ride);
    }

    @Transactional
    public void cancelRide(Long id, String customerEmail) {
        Ride ride = rideRepository.findByIdAndCustomer_Email(id, customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));
        ensureMutable(ride);
        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Completed ride cannot be cancelled");
        }
        ride.setStatus(RideStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public List<RideResponse> getAvailableRides() {
        return rideRepository.findByStatusAndDriverIsNull(RideStatus.REQUESTED).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RideResponse acceptRide(Long rideId, String driverEmail) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));
        if (ride.getStatus() != RideStatus.REQUESTED || ride.getDriver() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ride is not available for acceptance");
        }

        Driver driver = driverRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));
        if (!driver.isAvailable()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Driver is not available");
        }

        ride.setDriver(driver);
        ride.setStatus(RideStatus.ASSIGNED);
        driver.setAvailable(false);
        return toResponse(ride);
    }

    @Transactional
    public RideResponse startRide(Long rideId, String driverEmail) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));
        validateDriverOwnership(ride, driverEmail);
        ensureMutable(ride);
        if (ride.getStatus() != RideStatus.ASSIGNED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ride cannot be started now");
        }
        ride.setStatus(RideStatus.IN_PROGRESS);
        return toResponse(ride);
    }

    @Transactional
    public RideResponse completeRide(Long rideId, String driverEmail) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));
        validateDriverOwnership(ride, driverEmail);
        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ride cannot be completed now");
        }
        ride.setStatus(RideStatus.COMPLETED);
        ride.getDriver().setAvailable(true);
        return toResponse(ride);
    }

    private void validateDriverOwnership(Ride ride, String driverEmail) {
        if (ride.getDriver() == null || !ride.getDriver().getEmail().equals(driverEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This ride is not assigned to this driver");
        }
    }

    private void ensureMutable(Ride ride) {
        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Completed rides cannot be modified");
        }
    }

    private RideResponse toResponse(Ride ride) {
        return new RideResponse(
                ride.getId(),
                ride.getPickupLocation(),
                ride.getDropLocation(),
                ride.getFare(),
                ride.getStatus(),
                ride.getCustomer().getName(),
                ride.getDriver() == null ? null : ride.getDriver().getName()
        );
    }
}
