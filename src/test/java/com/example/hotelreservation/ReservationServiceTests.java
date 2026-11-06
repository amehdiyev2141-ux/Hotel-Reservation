package com.example.hotelreservation;

import com.example.hotelreservation.dto.ReservationRequest;
import com.example.hotelreservation.dto.ReservationResponse;
import com.example.hotelreservation.entity.Guest;
import com.example.hotelreservation.entity.ReservationEntity;
import com.example.hotelreservation.entity.ReservationStatus;
import com.example.hotelreservation.entity.RoomEntity;
import com.example.hotelreservation.repository.GuestRepository;
import com.example.hotelreservation.repository.ReservationRepository;
import com.example.hotelreservation.repository.RoomRepository;
import com.example.hotelreservation.service.ReservationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationServiceTests {

    private final ReservationRepository reservationRepository = mock(ReservationRepository.class);
    private final RoomRepository roomRepository = mock(RoomRepository.class);
    private final GuestRepository guestRepository = mock(GuestRepository.class);
    private final ReservationService reservationService = new ReservationService(
            reservationRepository, roomRepository, guestRepository
    );

    @Test
    void createsReservationAndCalculatesTotalPrice() {
        RoomEntity room = createRoom();
        Guest guest = createGuest();
        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(guestRepository.findById(20L)).thenReturn(Optional.of(guest));
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> {
            ReservationEntity saved = invocation.getArgument(0);
            saved.setId(30L);
            return saved;
        });

        ReservationResponse response = reservationService.createReservation(validRequest(checkIn(), checkOut()));

        assertEquals(30L, response.id());
        assertEquals(10L, response.roomId());
        assertEquals(101, response.roomNumber());
        assertEquals(20L, response.guestId());
        assertEquals("Test Guest", response.guestName());
        assertEquals(new BigDecimal("375.00"), response.totalPrice());
        assertEquals(ReservationStatus.ACTIVE, response.status());
        verify(reservationRepository).existsOverlappingReservation(
                10L, checkIn(), checkOut(), ReservationStatus.ACTIVE
        );
        verify(reservationRepository).save(any(ReservationEntity.class));
    }

    @Test
    void throwsRoomNotFoundWhenRoomDoesNotExist() {
        when(roomRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.createReservation(validRequest(checkIn(), checkOut())));

        assertEquals("Room not found", exception.getMessage());
        verify(guestRepository, never()).findById(20L);
        verify(reservationRepository, never()).save(any(ReservationEntity.class));
    }

    @Test
    void throwsGuestNotFoundWhenGuestDoesNotExist() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(createRoom()));
        when(guestRepository.findById(20L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.createReservation(validRequest(checkIn(), checkOut())));

        assertEquals("Guest not found", exception.getMessage());
        verify(reservationRepository, never()).save(any(ReservationEntity.class));
    }

    @Test
    void rejectsCheckOutBeforeCheckIn() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.createReservation(validRequest(checkIn(), checkIn().minusDays(1))));

        assertEquals("Check-out date must be after check-in date", exception.getMessage());
        verify(roomRepository, never()).findById(10L);
    }

    @Test
    void rejectsCheckOutEqualToCheckIn() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.createReservation(validRequest(checkIn(), checkIn())));

        assertEquals("Check-out date must be after check-in date", exception.getMessage());
        verify(roomRepository, never()).findById(10L);
    }

    @Test
    void rejectsReservationWithCheckInInThePast() {
        LocalDate pastCheckIn = LocalDate.now().minusDays(1);
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.createReservation(validRequest(pastCheckIn, LocalDate.now().plusDays(1))));

        assertEquals("Check-in date cannot be in the past", exception.getMessage());
        verify(roomRepository, never()).findById(10L);
    }

    @Test
    void rejectsReservationWhenRoomIsAlreadyBookedForActiveDates() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(createRoom()));
        when(guestRepository.findById(20L)).thenReturn(Optional.of(createGuest()));
        when(reservationRepository.existsOverlappingReservation(
                10L, checkIn(), checkOut(), ReservationStatus.ACTIVE
        )).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.createReservation(validRequest(checkIn(), checkOut())));

        assertEquals("Room is already booked for these dates", exception.getMessage());
        verify(reservationRepository, never()).save(any(ReservationEntity.class));
    }

    @Test
    void cancelledReservationDoesNotBlockNewReservation() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(createRoom()));
        when(guestRepository.findById(20L)).thenReturn(Optional.of(createGuest()));
        when(reservationRepository.existsOverlappingReservation(
                10L, checkIn(), checkOut(), ReservationStatus.ACTIVE
        )).thenReturn(false);
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReservationResponse response = reservationService.createReservation(validRequest(checkIn(), checkOut()));

        assertEquals(ReservationStatus.ACTIVE, response.status());
        verify(reservationRepository).existsOverlappingReservation(
                10L, checkIn(), checkOut(), ReservationStatus.ACTIVE
        );
        verify(reservationRepository).save(any(ReservationEntity.class));
    }

    @Test
    void cancelsReservationAndReturnsCancelledStatus() {
        ReservationEntity reservation = createReservation(30L, ReservationStatus.ACTIVE);
        when(reservationRepository.findById(30L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        ReservationResponse response = reservationService.cancelReservation(30L);

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        assertEquals(ReservationStatus.CANCELLED, response.status());
        verify(reservationRepository).save(reservation);
    }

    @Test
    void rejectsCancellingReservationThatIsAlreadyCancelled() {
        ReservationEntity reservation = createReservation(30L, ReservationStatus.CANCELLED);
        when(reservationRepository.findById(30L)).thenReturn(Optional.of(reservation));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.cancelReservation(30L));

        assertEquals("Reservation is already cancelled", exception.getMessage());
        verify(reservationRepository, never()).save(any(ReservationEntity.class));
    }

    @Test
    void throwsReservationNotFoundWhenCancellingUnknownId() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.cancelReservation(99L));

        assertEquals("Reservation not found", exception.getMessage());
        verify(reservationRepository, never()).save(any(ReservationEntity.class));
    }

    @Test
    void rejectsDeletingReservationThatDoesNotExist() {
        when(reservationRepository.existsById(99L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.deleteReservation(99L));

        assertEquals("Reservation not found", exception.getMessage());
        verify(reservationRepository, never()).deleteById(99L);
    }

    private ReservationRequest validRequest(LocalDate checkIn, LocalDate checkOut) {
        return new ReservationRequest(10L, 20L, checkIn, checkOut);
    }

    private RoomEntity createRoom() {
        RoomEntity room = new RoomEntity();
        room.setId(10L);
        room.setNumber(101);
        room.setPricePerNight(new BigDecimal("125.00"));
        return room;
    }

    private Guest createGuest() {
        Guest guest = new Guest();
        guest.setId(20L);
        guest.setFullName("Test Guest");
        return guest;
    }

    private ReservationEntity createReservation(Long id, ReservationStatus status) {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(id);
        reservation.setRoom(createRoom());
        reservation.setGuest(createGuest());
        reservation.setCheckIn(checkIn());
        reservation.setCheckOut(checkOut());
        reservation.setTotalPrice(new BigDecimal("375.00"));
        reservation.setStatus(status);
        return reservation;
    }

    private LocalDate checkIn() {
        return LocalDate.of(2027, 1, 10);
    }

    private LocalDate checkOut() {
        return LocalDate.of(2027, 1, 13);
    }
}
