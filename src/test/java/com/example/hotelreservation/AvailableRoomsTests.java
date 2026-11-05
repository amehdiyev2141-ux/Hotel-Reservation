package com.example.hotelreservation;

import com.example.hotelreservation.controller.RoomController;
import com.example.hotelreservation.entity.Guest;
import com.example.hotelreservation.entity.ReservationEntity;
import com.example.hotelreservation.entity.ReservationStatus;
import com.example.hotelreservation.entity.RoomEntity;
import com.example.hotelreservation.entity.RoomType;
import com.example.hotelreservation.exception.GlobalExceptionHandler;
import com.example.hotelreservation.repository.GuestRepository;
import com.example.hotelreservation.repository.ReservationRepository;
import com.example.hotelreservation.repository.RoomRepository;
import com.example.hotelreservation.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class AvailableRoomsTests {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Test
    void roomWithoutReservationsIsAvailable() {
        RoomEntity room = saveRoom();

        List<RoomEntity> availableRooms = roomService.getAvailableRooms(checkIn(), checkOut());

        assertTrue(availableRooms.stream().anyMatch(item -> item.getId().equals(room.getId())));
    }

    @Test
    void activeReservationMakesRoomUnavailableForOverlappingDates() {
        RoomEntity room = saveRoom();
        saveReservation(room, ReservationStatus.ACTIVE, checkIn(), checkOut());

        List<RoomEntity> availableRooms = roomService.getAvailableRooms(checkIn(), checkOut());

        assertTrue(availableRooms.stream().noneMatch(item -> item.getId().equals(room.getId())));
    }

    @Test
    void cancelledReservationDoesNotMakeRoomUnavailable() {
        RoomEntity room = saveRoom();
        saveReservation(room, ReservationStatus.CANCELLED, checkIn(), checkOut());

        List<RoomEntity> availableRooms = roomService.getAvailableRooms(checkIn(), checkOut());

        assertTrue(availableRooms.stream().anyMatch(item -> item.getId().equals(room.getId())));
    }

    @Test
    void roomIsAvailableWhenReservationDatesDoNotOverlap() {
        RoomEntity room = saveRoom();
        saveReservation(room, ReservationStatus.ACTIVE, checkOut(), checkOut().plusDays(3));

        List<RoomEntity> availableRooms = roomService.getAvailableRooms(checkIn(), checkOut());

        assertTrue(availableRooms.stream().anyMatch(item -> item.getId().equals(room.getId())));
    }

    @Test
    void checkOutMustBeAfterCheckIn() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> roomService.getAvailableRooms(checkIn(), checkIn()));

        assertEquals("Check-out date must be after check-in date", exception.getMessage());
    }

    @Test
    void pastDatesAreRejected() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> roomService.getAvailableRooms(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)));

        assertEquals("Check-in date cannot be in the past", exception.getMessage());
    }

    @Test
    void invalidDateRangeReturnsBadRequestResponse() throws Exception {
        RoomService service = mock(RoomService.class);
        LocalDate start = checkIn();
        LocalDate end = checkOut();
        when(service.getAvailableRooms(end, start))
                .thenThrow(new RuntimeException("Check-out date must be after check-in date"));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new RoomController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/rooms/available")
                        .param("checkIn", end.toString())
                        .param("checkOut", start.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Check-out date must be after check-in date"))
                .andExpect(jsonPath("$.path").value("/api/rooms/available"));
    }

    private RoomEntity saveRoom() {
        RoomEntity room = new RoomEntity();
        room.setNumber(120);
        room.setType(RoomType.SINGLE);
        room.setPricePerNight(new BigDecimal("100.00"));
        return roomRepository.saveAndFlush(room);
    }

    private void saveReservation(RoomEntity room, ReservationStatus status, LocalDate from, LocalDate to) {
        Guest guest = new Guest();
        guest.setFullName("Available Room Test Guest");
        guest.setEmail("available-room-test@example.com");
        guest.setPhoneNumber("+10000000000");
        guest = guestRepository.saveAndFlush(guest);

        ReservationEntity reservation = new ReservationEntity();
        reservation.setRoom(room);
        reservation.setGuest(guest);
        reservation.setCheckIn(from);
        reservation.setCheckOut(to);
        reservation.setTotalPrice(new BigDecimal("100.00"));
        reservation.setStatus(status);
        reservationRepository.saveAndFlush(reservation);
    }

    private LocalDate checkIn() {
        return LocalDate.now().plusDays(30);
    }

    private LocalDate checkOut() {
        return checkIn().plusDays(4);
    }
}
