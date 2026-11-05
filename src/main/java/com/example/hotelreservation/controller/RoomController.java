package com.example.hotelreservation.controller;

import com.example.hotelreservation.entity.RoomEntity;
import com.example.hotelreservation.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;


    @GetMapping
    public List<RoomEntity> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/available")
    public List<RoomEntity> getAvailableRooms(
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut
    ) {
        return roomService.getAvailableRooms(checkIn, checkOut);
    }

    @PostMapping
    public RoomEntity createRoom(@Valid @RequestBody RoomEntity room) {
        return roomService.createRoom(room);
    }

    @GetMapping("/{id}")
    public RoomEntity getRoomById(@PathVariable Long id) {
        return roomService.getRoomById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);

    }
}
