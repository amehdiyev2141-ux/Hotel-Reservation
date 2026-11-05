package com.example.hotelreservation.repository;

import com.example.hotelreservation.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {
}
