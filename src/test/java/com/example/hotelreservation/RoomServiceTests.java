package com.example.hotelreservation;

import com.example.hotelreservation.entity.ReservationStatus;
import com.example.hotelreservation.entity.RoomEntity;
import com.example.hotelreservation.entity.RoomType;
import com.example.hotelreservation.repository.RoomRepository;
import com.example.hotelreservation.service.RoomService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoomServiceTests {

    private final RoomRepository roomRepository = mock(RoomRepository.class);
    private final RoomService roomService = new RoomService(roomRepository);

    @Test
    void returnsAllRooms() {
        List<RoomEntity> rooms = List.of(createRoom(1L), createRoom(2L));
        when(roomRepository.findAll()).thenReturn(rooms);

        assertSame(rooms, roomService.getAllRooms());
    }

    @Test
    void returnsRoomById() {
        RoomEntity room = createRoom(1L);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertSame(room, roomService.getRoomById(1L));
    }

    @Test
    void throwsRoomNotFoundWhenIdDoesNotExist() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> roomService.getRoomById(99L));

        assertEquals("Room not found", exception.getMessage());
    }

    @Test
    void createsRoom() {
        RoomEntity room = createRoom(null);
        RoomEntity savedRoom = createRoom(1L);
        when(roomRepository.save(room)).thenReturn(savedRoom);

        assertSame(savedRoom, roomService.createRoom(room));
        verify(roomRepository).save(room);
    }

    @Test
    void deletesRoomById() {
        roomService.deleteRoom(1L);

        verify(roomRepository).deleteById(1L);
    }

    @Test
    void propagatesRoomNotFoundWhenRepositoryReportsMissingRoomDuringDelete() {
        doThrow(new RuntimeException("Room not found")).when(roomRepository).deleteById(99L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> roomService.deleteRoom(99L));

        assertEquals("Room not found", exception.getMessage());
    }

    @Test
    void returnsRoomsAvailableForRequestedDates() {
        LocalDate checkIn = futureCheckIn();
        LocalDate checkOut = checkIn.plusDays(3);
        List<RoomEntity> availableRooms = List.of(createRoom(1L), createRoom(2L));
        when(roomRepository.findAvailableRooms(checkIn, checkOut, ReservationStatus.ACTIVE))
                .thenReturn(availableRooms);

        assertSame(availableRooms, roomService.getAvailableRooms(checkIn, checkOut));
        verify(roomRepository).findAvailableRooms(checkIn, checkOut, ReservationStatus.ACTIVE);
    }

    @Test
    void excludesRoomBookedByAnActiveReservation() {
        LocalDate checkIn = futureCheckIn();
        LocalDate checkOut = checkIn.plusDays(3);
        RoomEntity availableRoom = createRoom(2L);
        when(roomRepository.findAvailableRooms(checkIn, checkOut, ReservationStatus.ACTIVE))
                .thenReturn(List.of(availableRoom));

        List<RoomEntity> result = roomService.getAvailableRooms(checkIn, checkOut);

        assertEquals(List.of(availableRoom), result);
        verify(roomRepository).findAvailableRooms(checkIn, checkOut, ReservationStatus.ACTIVE);
    }

    @Test
    void cancelledReservationDoesNotPreventRoomFromBeingAvailable() {
        LocalDate checkIn = futureCheckIn();
        LocalDate checkOut = checkIn.plusDays(3);
        RoomEntity room = createRoom(1L);
        when(roomRepository.findAvailableRooms(checkIn, checkOut, ReservationStatus.ACTIVE))
                .thenReturn(List.of(room));

        List<RoomEntity> result = roomService.getAvailableRooms(checkIn, checkOut);

        assertEquals(List.of(room), result);
        verify(roomRepository).findAvailableRooms(checkIn, checkOut, ReservationStatus.ACTIVE);
    }

    @Test
    void rejectsCheckOutThatIsNotAfterCheckIn() {
        LocalDate checkIn = futureCheckIn();

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> roomService.getAvailableRooms(checkIn, checkIn));

        assertEquals("Check-out date must be after check-in date", exception.getMessage());
    }

    @Test
    void rejectsAvailabilitySearchForPastDates() {
        LocalDate checkIn = LocalDate.now().minusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(1);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> roomService.getAvailableRooms(checkIn, checkOut));

        assertEquals("Check-in date cannot be in the past", exception.getMessage());
    }

    private RoomEntity createRoom(Long id) {
        RoomEntity room = new RoomEntity();
        room.setId(id);
        room.setNumber(101);
        room.setType(RoomType.SINGLE);
        room.setPricePerNight(new BigDecimal("100.00"));
        return room;
    }

    private LocalDate futureCheckIn() {
        return LocalDate.now().plusDays(10);
    }
}
