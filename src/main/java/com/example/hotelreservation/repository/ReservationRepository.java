package com.example.hotelreservation.repository;

import com.example.hotelreservation.entity.ReservationEntity;
import com.example.hotelreservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    @Query("""
            SELECT COUNT(r) > 0
            FROM ReservationEntity r
            WHERE r.room.id = :roomId
            AND r.status = :status
            AND r.checkIn < :checkOut
            AND r.checkOut > :checkIn
            """)
    boolean existsOverlappingReservation(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("status") ReservationStatus status
    );
}
