package com.example.hotelreservation.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationRequest(
        @NotNull(message = "Room ID is required")
        Long roomId,
        @NotNull(message = "Guest ID is required")
        Long guestId,
        @NotNull(message = "Check-in date is required")
        LocalDate checkIn,
        @NotNull(message = "Check-out date is required")
        LocalDate checkOut
) {
}
