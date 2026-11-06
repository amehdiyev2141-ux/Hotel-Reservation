package com.example.hotelreservation;

import com.example.hotelreservation.dto.ReservationResponse;
import com.example.hotelreservation.entity.Guest;
import com.example.hotelreservation.entity.ReservationStatus;
import com.example.hotelreservation.entity.RoomEntity;
import com.example.hotelreservation.entity.RoomType;
import com.example.hotelreservation.service.GuestService;
import com.example.hotelreservation.service.ReservationService;
import com.example.hotelreservation.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class ControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private GuestService guestService;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void getRoomsReturnsAllRooms() throws Exception {
        when(roomService.getAllRooms()).thenReturn(List.of(room(1L)));

        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].number").value(101))
                .andExpect(jsonPath("$[0].type").value("SINGLE"))
                .andExpect(jsonPath("$[0].pricePerNight").value(100.0));
    }

    @Test
    void postRoomCreatesRoom() throws Exception {
        when(roomService.createRoom(org.mockito.ArgumentMatchers.any(RoomEntity.class)))
                .thenReturn(room(1L));

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"number":101,"type":"SINGLE","pricePerNight":100.00}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.number").value(101))
                .andExpect(jsonPath("$.pricePerNight").value(100.0));
    }

    @Test
    void getRoomByIdReturnsRoom() throws Exception {
        when(roomService.getRoomById(1L)).thenReturn(room(1L));

        mockMvc.perform(get("/api/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.number").value(101));
    }

    @Test
    void getMissingRoomReturnsNotFoundErrorBody() throws Exception {
        when(roomService.getRoomById(99L)).thenThrow(new RuntimeException("Room not found"));

        mockMvc.perform(get("/api/rooms/99"))
                .andExpect(status().isNotFound())
                .andExpect(errorBody(404, "Not Found", "Room not found", "/api/rooms/99"));
    }

    @Test
    void deleteRoomReturnsSuccess() throws Exception {
        mockMvc.perform(delete("/api/rooms/1"))
                .andExpect(status().isOk());

        verify(roomService).deleteRoom(1L);
    }

    @Test
    void deletingMissingRoomReturnsNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Room not found"))
                .when(roomService).deleteRoom(99L);

        mockMvc.perform(delete("/api/rooms/99"))
                .andExpect(status().isNotFound())
                .andExpect(errorBody(404, "Not Found", "Room not found", "/api/rooms/99"));
    }

    @Test
    void postRoomWithInvalidFieldsReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"number":null,"type":null,"pricePerNight":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/rooms"));
    }

    @Test
    void getAvailableRoomsReturnsAvailableRooms() throws Exception {
        LocalDate checkIn = futureCheckIn();
        LocalDate checkOut = checkIn.plusDays(3);
        when(roomService.getAvailableRooms(checkIn, checkOut)).thenReturn(List.of(room(1L)));

        mockMvc.perform(get("/api/rooms/available")
                        .param("checkIn", checkIn.toString())
                        .param("checkOut", checkOut.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].number").value(101));
    }

    @Test
    void getAvailableRoomsWithInvalidDatesReturnsBadRequest() throws Exception {
        LocalDate checkIn = futureCheckIn();
        LocalDate checkOut = checkIn;
        when(roomService.getAvailableRooms(checkIn, checkOut))
                .thenThrow(new RuntimeException("Check-out date must be after check-in date"));

        mockMvc.perform(get("/api/rooms/available")
                        .param("checkIn", checkIn.toString())
                        .param("checkOut", checkOut.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(errorBody(400, "Bad Request", "Check-out date must be after check-in date",
                        "/api/rooms/available"));
    }

    @Test
    void getGuestsReturnsAllGuests() throws Exception {
        when(guestService.getAllGuests()).thenReturn(List.of(guest(1L)));

        mockMvc.perform(get("/api/guests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Aysel Mammadova"))
                .andExpect(jsonPath("$[0].email").value("aysel@example.com"))
                .andExpect(jsonPath("$[0].phoneNumber").value("+994501112233"));
    }

    @Test
    void postGuestCreatesGuest() throws Exception {
        when(guestService.createGuest(org.mockito.ArgumentMatchers.any(Guest.class))).thenReturn(guest(1L));

        mockMvc.perform(post("/api/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Aysel Mammadova","email":"aysel@example.com","phoneNumber":"+994501112233"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Aysel Mammadova"))
                .andExpect(jsonPath("$.email").value("aysel@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+994501112233"));
    }

    @Test
    void getGuestByIdReturnsGuest() throws Exception {
        when(guestService.findById(1L)).thenReturn(guest(1L));

        mockMvc.perform(get("/api/guests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Aysel Mammadova"))
                .andExpect(jsonPath("$.email").value("aysel@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+994501112233"));
    }

    @Test
    void getMissingGuestReturnsNotFoundErrorBody() throws Exception {
        when(guestService.findById(99L)).thenThrow(new RuntimeException("Guest not found"));

        mockMvc.perform(get("/api/guests/99"))
                .andExpect(status().isNotFound())
                .andExpect(errorBody(404, "Not Found", "Guest not found", "/api/guests/99"));
    }

    @Test
    void deleteGuestReturnsSuccess() throws Exception {
        mockMvc.perform(delete("/api/guests/1"))
                .andExpect(status().isOk());

        verify(guestService).deleteGuest(1L);
    }

    @Test
    void deletingMissingGuestReturnsNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Guest not found"))
                .when(guestService).deleteGuest(99L);

        mockMvc.perform(delete("/api/guests/99"))
                .andExpect(status().isNotFound())
                .andExpect(errorBody(404, "Not Found", "Guest not found", "/api/guests/99"));
    }

    @Test
    void postGuestWithInvalidEmailReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Aysel Mammadova","email":"invalid-email","phoneNumber":"+994501112233"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Email must be valid")));
    }

    @Test
    void postGuestWithBlankRequiredFieldsReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":" ","email":"","phoneNumber":" "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void postReservationCreatesReservationResponse() throws Exception {
        LocalDate checkIn = futureCheckIn();
        LocalDate checkOut = checkIn.plusDays(2);
        when(reservationService.createReservation(org.mockito.ArgumentMatchers.any()))
                .thenReturn(reservationResponse(5L, ReservationStatus.ACTIVE, checkIn, checkOut));

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roomId":1,"guestId":2,"checkIn":"%s","checkOut":"%s"}
                                """.formatted(checkIn, checkOut)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.roomId").value(1))
                .andExpect(jsonPath("$.roomNumber").value(101))
                .andExpect(jsonPath("$.guestId").value(2))
                .andExpect(jsonPath("$.guestName").value("Aysel Mammadova"))
                .andExpect(jsonPath("$.checkIn").value(checkIn.toString()))
                .andExpect(jsonPath("$.checkOut").value(checkOut.toString()))
                .andExpect(jsonPath("$.totalPrice").value(200.0))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getReservationsReturnsReservationResponseList() throws Exception {
        LocalDate checkIn = futureCheckIn();
        LocalDate checkOut = checkIn.plusDays(2);
        when(reservationService.getAllReservations())
                .thenReturn(List.of(reservationResponse(5L, ReservationStatus.ACTIVE, checkIn, checkOut)));

        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].roomId").value(1))
                .andExpect(jsonPath("$[0].roomNumber").value(101))
                .andExpect(jsonPath("$[0].guestId").value(2))
                .andExpect(jsonPath("$[0].guestName").value("Aysel Mammadova"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void getReservationByIdReturnsReservationResponse() throws Exception {
        LocalDate checkIn = futureCheckIn();
        LocalDate checkOut = checkIn.plusDays(2);
        when(reservationService.findById(5L))
                .thenReturn(reservationResponse(5L, ReservationStatus.ACTIVE, checkIn, checkOut));

        mockMvc.perform(get("/api/reservations/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.roomId").value(1))
                .andExpect(jsonPath("$.guestName").value("Aysel Mammadova"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getMissingReservationReturnsNotFoundErrorBody() throws Exception {
        when(reservationService.findById(99L)).thenThrow(new RuntimeException("Reservation not found"));

        mockMvc.perform(get("/api/reservations/99"))
                .andExpect(status().isNotFound())
                .andExpect(errorBody(404, "Not Found", "Reservation not found", "/api/reservations/99"));
    }

    @Test
    void deletingMissingReservationReturnsNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Reservation not found"))
                .when(reservationService).deleteReservation(99L);

        mockMvc.perform(delete("/api/reservations/99"))
                .andExpect(status().isNotFound())
                .andExpect(errorBody(404, "Not Found", "Reservation not found", "/api/reservations/99"));
    }

    @Test
    void overlappingActiveReservationReturnsConflict() throws Exception {
        LocalDate checkIn = futureCheckIn();
        LocalDate checkOut = checkIn.plusDays(2);
        when(reservationService.createReservation(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("Room is already booked for these dates"));

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roomId":1,"guestId":2,"checkIn":"%s","checkOut":"%s"}
                                """.formatted(checkIn, checkOut)))
                .andExpect(status().isConflict())
                .andExpect(errorBody(409, "Conflict", "Room is already booked for these dates",
                        "/api/reservations"));
    }

    @Test
    void invalidReservationDatesReturnBadRequest() throws Exception {
        LocalDate checkIn = futureCheckIn();
        when(reservationService.createReservation(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("Check-out date must be after check-in date"));

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roomId":1,"guestId":2,"checkIn":"%s","checkOut":"%s"}
                                """.formatted(checkIn, checkIn)))
                .andExpect(status().isBadRequest())
                .andExpect(errorBody(400, "Bad Request", "Check-out date must be after check-in date",
                        "/api/reservations"));
    }

    @Test
    void pastReservationCheckInReturnsBadRequest() throws Exception {
        LocalDate checkIn = LocalDate.now().minusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(1);
        when(reservationService.createReservation(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("Check-in date cannot be in the past"));

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roomId":1,"guestId":2,"checkIn":"%s","checkOut":"%s"}
                                """.formatted(checkIn, checkOut)))
                .andExpect(status().isBadRequest())
                .andExpect(errorBody(400, "Bad Request", "Check-in date cannot be in the past",
                        "/api/reservations"));
    }

    @Test
    void cancelReservationReturnsCancelledResponse() throws Exception {
        LocalDate checkIn = futureCheckIn();
        LocalDate checkOut = checkIn.plusDays(2);
        when(reservationService.cancelReservation(5L))
                .thenReturn(reservationResponse(5L, ReservationStatus.CANCELLED, checkIn, checkOut));

        mockMvc.perform(patch("/api/reservations/5/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.roomId").value(1))
                .andExpect(jsonPath("$.guestName").value("Aysel Mammadova"));
    }

    @Test
    void cancellingReservationTwiceReturnsConflict() throws Exception {
        when(reservationService.cancelReservation(5L))
                .thenThrow(new RuntimeException("Reservation is already cancelled"));

        mockMvc.perform(patch("/api/reservations/5/cancel"))
                .andExpect(status().isConflict())
                .andExpect(errorBody(409, "Conflict", "Reservation is already cancelled",
                        "/api/reservations/5/cancel"));
    }

    @Test
    void cancelledReservationCanBeFollowedByAvailableRoomRequest() throws Exception {
        LocalDate checkIn = futureCheckIn();
        LocalDate checkOut = checkIn.plusDays(2);
        when(reservationService.cancelReservation(5L))
                .thenReturn(reservationResponse(5L, ReservationStatus.CANCELLED, checkIn, checkOut));
        when(roomService.getAvailableRooms(checkIn, checkOut)).thenReturn(List.of(room(1L)));

        mockMvc.perform(patch("/api/reservations/5/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/rooms/available")
                        .param("checkIn", checkIn.toString())
                        .param("checkOut", checkOut.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    private RoomEntity room(Long id) {
        RoomEntity room = new RoomEntity();
        room.setId(id);
        room.setNumber(101);
        room.setType(RoomType.SINGLE);
        room.setPricePerNight(new BigDecimal("100.00"));
        return room;
    }

    private Guest guest(Long id) {
        Guest guest = new Guest();
        guest.setId(id);
        guest.setFullName("Aysel Mammadova");
        guest.setEmail("aysel@example.com");
        guest.setPhoneNumber("+994501112233");
        return guest;
    }

    private ReservationResponse reservationResponse(
            Long id,
            ReservationStatus status,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        return new ReservationResponse(
                id, 1L, 101, 2L, "Aysel Mammadova", checkIn, checkOut,
                new BigDecimal("200.00"), status
        );
    }

    private LocalDate futureCheckIn() {
        return LocalDate.now().plusDays(10);
    }

    private ResultMatcher errorBody(int status, String error, String message, String path) {
        return result -> {
            jsonPath("$.timestamp").exists().match(result);
            jsonPath("$.status").value(status).match(result);
            jsonPath("$.error").value(error).match(result);
            jsonPath("$.message").value(message).match(result);
            jsonPath("$.path").value(path).match(result);
        };
    }
}
