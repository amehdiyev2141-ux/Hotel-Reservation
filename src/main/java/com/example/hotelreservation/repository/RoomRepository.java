package com.example.hotelreservation.repository;

import com.example.hotelreservation.entity.RoomEntity;
import com.example.hotelreservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<RoomEntity, Long> {

    Iterable<Long> id(Long id);

    @Query("""
            SELECT DISTINCT room
            FROM RoomEntity room
            WHERE NOT EXISTS (
                SELECT reservation.id
                FROM ReservationEntity reservation
                WHERE reservation.room.id = room.id
                AND reservation.status = :status
                AND reservation.checkIn < :checkOut
                AND reservation.checkOut > :checkIn
            )
            """)
    List<RoomEntity> findAvailableRooms(
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("status") ReservationStatus status
    );
}
