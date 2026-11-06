package com.example.hotelreservation.controller;

import com.example.hotelreservation.service.RoomSeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/admin")
@RequiredArgsConstructor
public class AdminRoomSeedController {

    private static final String TOKEN_HEADER = "X-Admin-Seed-Token";
    private static final int MINIMUM_TOKEN_LENGTH = 32;

    private final RoomSeedService roomSeedService;

    @Value("${admin.seed.enabled:false}")
    private boolean seedEnabled;

    @Value("${admin.seed.token:}")
    private String configuredToken;

    @PostMapping("/seed-rooms")
    public ResponseEntity<Map<String, String>> seedRooms(
            @RequestHeader(name = TOKEN_HEADER, required = false) String suppliedToken
    ) {
        if (!seedEnabled) {
            return ResponseEntity.notFound().build();
        }

        if (!matchesConfiguredToken(suppliedToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "A valid admin seed token is required"));
        }

        try {
            roomSeedService.seedRooms();
            return ResponseEntity.ok(Map.of("message", "Sample room seed script executed successfully"));
        } catch (RuntimeException exception) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Sample room seed operation failed"));
        }
    }

    private boolean matchesConfiguredToken(String suppliedToken) {
        if (configuredToken == null || configuredToken.length() < MINIMUM_TOKEN_LENGTH || suppliedToken == null) {
            return false;
        }

        return MessageDigest.isEqual(
                configuredToken.getBytes(StandardCharsets.UTF_8),
                suppliedToken.getBytes(StandardCharsets.UTF_8)
        );
    }
}
