package com.example.hotelreservation.service;

import com.example.hotelreservation.dto.ReservationRequest;
import com.example.hotelreservation.dto.ReservationResponse;
import com.example.hotelreservation.entity.Guest;
import com.example.hotelreservation.entity.ReservationEntity;
import com.example.hotelreservation.entity.ReservationStatus;
import com.example.hotelreservation.entity.RoomEntity;
import com.example.hotelreservation.repository.GuestRepository;
import com.example.hotelreservation.repository.ReservationRepository;
import com.example.hotelreservation.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ReservationResponse findById(Long id) {
        ReservationEntity reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        return toResponse(reservation);
    }

    public ReservationResponse createReservation(ReservationRequest request) {

        if (!request.checkOut().isAfter(request.checkIn())) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }
        if (request.checkIn().isBefore(LocalDate.now())) {
            throw new RuntimeException("Check-in date cannot be in the past");
        }

        RoomEntity room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        Guest guest = guestRepository.findById(request.guestId())
                .orElseThrow(() -> new RuntimeException("Guest not found"));

        boolean roomIsBooked = reservationRepository.existsOverlappingReservation(
                room.getId(),
                request.checkIn(),
                request.checkOut(),
                ReservationStatus.ACTIVE
        );

        if (roomIsBooked) {
            throw new RuntimeException("Room is already booked for these dates");
        }

        long nights = ChronoUnit.DAYS.between(request.checkIn(), request.checkOut());
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        ReservationEntity reservation = new ReservationEntity();
        reservation.setRoom(room);
        reservation.setGuest(guest);
        reservation.setCheckIn(request.checkIn());
        reservation.setCheckOut(request.checkOut());
        reservation.setTotalPrice(totalPrice);
        reservation.setStatus(ReservationStatus.ACTIVE);

        return toResponse(reservationRepository.save(reservation));
    }

    public ReservationResponse cancelReservation(Long id) {
        ReservationEntity reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new RuntimeException("Reservation is already cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return toResponse(reservationRepository.save(reservation));
    }

    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new RuntimeException("Reservation not found");
        }
        reservationRepository.deleteById(id);
    }

    private ReservationResponse toResponse(ReservationEntity reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getRoom().getId(),
                reservation.getRoom().getNumber(),
                reservation.getGuest().getId(),
                reservation.getGuest().getFullName(),
                reservation.getCheckIn(),
                reservation.getCheckOut(),
                reservation.getTotalPrice(),
                reservation.getStatus()
        );
    }
}
