package com.example.hotelreservation.dto;

import com.example.hotelreservation.entity.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        Long roomId,
        Integer roomNumber,
        Long guestId,
        String guestName,
        LocalDate checkIn,
        LocalDate checkOut,
        BigDecimal totalPrice,
        ReservationStatus status
) {
}
