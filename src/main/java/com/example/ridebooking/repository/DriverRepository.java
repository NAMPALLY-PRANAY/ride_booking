package com.example.ridebooking.repository;

import com.example.ridebooking.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByEmail(String email);
    List<Driver> findByAvailableTrue();
}
