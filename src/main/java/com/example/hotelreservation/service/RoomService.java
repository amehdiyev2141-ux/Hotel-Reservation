package com.example.hotelreservation.service;

import com.example.hotelreservation.entity.RoomEntity;
import com.example.hotelreservation.entity.ReservationStatus;
import com.example.hotelreservation.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;


    public List<RoomEntity> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<RoomEntity> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new RuntimeException("Check-in date cannot be in the past");
        }

        return roomRepository.findAvailableRooms(checkIn, checkOut, ReservationStatus.ACTIVE);
    }

    public RoomEntity getRoomById(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
    }

    public RoomEntity createRoom(RoomEntity room) {
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }
}
