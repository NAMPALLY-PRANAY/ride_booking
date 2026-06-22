package com.example.ridebooking.repository;

import com.example.ridebooking.entity.Ride;
import com.example.ridebooking.entity.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByStatusAndDriverIsNull(RideStatus status);
    Optional<Ride> findByIdAndCustomer_Email(Long id, String email);
}
