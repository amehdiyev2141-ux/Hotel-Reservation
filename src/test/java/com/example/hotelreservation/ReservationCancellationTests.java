package com.example.hotelreservation;

import com.example.hotelreservation.controller.ReservationController;
import com.example.hotelreservation.dto.ReservationRequest;
import com.example.hotelreservation.dto.ReservationResponse;
import com.example.hotelreservation.entity.Guest;
import com.example.hotelreservation.entity.ReservationEntity;
import com.example.hotelreservation.entity.ReservationStatus;
import com.example.hotelreservation.entity.RoomEntity;
import com.example.hotelreservation.exception.GlobalExceptionHandler;
import com.example.hotelreservation.repository.GuestRepository;
import com.example.hotelreservation.repository.ReservationRepository;
import com.example.hotelreservation.repository.RoomRepository;
import com.example.hotelreservation.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReservationCancellationTests {

    @Test
    void activeReservationIsCancelledAndSaved() {
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ReservationEntity reservation = createReservation(1L, ReservationStatus.ACTIVE);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        ReservationService service = createService(reservationRepository, mock(RoomRepository.class), mock(GuestRepository.class));

        ReservationResponse response = service.cancelReservation(1L);

        assertEquals(ReservationStatus.CANCELLED, response.status());
        verify(reservationRepository).save(reservation);
    }

    @Test
    void cancelledReservationCannotBeCancelledAgain() {
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ReservationEntity reservation = createReservation(1L, ReservationStatus.CANCELLED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationService service = createService(reservationRepository, mock(RoomRepository.class), mock(GuestRepository.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.cancelReservation(1L));

        assertEquals("Reservation is already cancelled", exception.getMessage());
    }

    @Test
    void missingReservationReturnsNotFound() throws Exception {
        ReservationService service = mock(ReservationService.class);
        when(service.cancelReservation(99L)).thenThrow(new RuntimeException("Reservation not found"));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ReservationController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(patch("/api/reservations/99/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Reservation not found"))
                .andExpect(jsonPath("$.path").value("/api/reservations/99/cancel"));
    }

    @Test
    void cancelledReservationDoesNotPreventRebookingSameDates() {
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        GuestRepository guestRepository = mock(GuestRepository.class);
        RoomEntity room = new RoomEntity();
        room.setId(3L);
        room.setNumber(12);
        room.setPricePerNight(new BigDecimal("80.00"));
        Guest guest = new Guest();
        guest.setId(4L);
        guest.setFullName("Test Guest");
        when(roomRepository.findById(3L)).thenReturn(Optional.of(room));
        when(guestRepository.findById(4L)).thenReturn(Optional.of(guest));
        when(reservationRepository.existsOverlappingReservation(
                3L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3), ReservationStatus.ACTIVE
        )).thenReturn(false);
        when(reservationRepository.save(any(ReservationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReservationService service = createService(reservationRepository, roomRepository, guestRepository);
        ReservationResponse response = service.createReservation(new ReservationRequest(
                3L, 4L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3)
        ));

        assertEquals(ReservationStatus.ACTIVE, response.status());
        verify(reservationRepository).existsOverlappingReservation(
                3L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3), ReservationStatus.ACTIVE
        );
    }

    private ReservationService createService(
            ReservationRepository reservationRepository,
            RoomRepository roomRepository,
            GuestRepository guestRepository
    ) {
        return new ReservationService(reservationRepository, roomRepository, guestRepository);
    }

    private ReservationEntity createReservation(Long id, ReservationStatus status) {
        RoomEntity room = new RoomEntity();
        room.setId(3L);
        room.setNumber(12);
        Guest guest = new Guest();
        guest.setId(4L);
        guest.setFullName("Test Guest");
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(id);
        reservation.setRoom(room);
        reservation.setGuest(guest);
        reservation.setCheckIn(LocalDate.of(2026, 12, 1));
        reservation.setCheckOut(LocalDate.of(2026, 12, 3));
        reservation.setTotalPrice(new BigDecimal("160.00"));
        reservation.setStatus(status);
        return reservation;
    }
}
