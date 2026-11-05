package com.example.hotelreservation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private RoomEntity room;

    @ManyToOne
    private Guest guest;

    private LocalDate checkIn;

    private LocalDate checkOut;

    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
}