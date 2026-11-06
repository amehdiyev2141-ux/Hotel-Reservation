package com.example.hotelreservation;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomSeedSqlTests {

    @Test
    void seedScriptIsIdempotentAndDoesNotModifyExistingRooms() throws IOException {
        String script = new ClassPathResource("seed-sample-rooms.sql")
                .getContentAsString(StandardCharsets.UTF_8);

        assertTrue(script.contains("BEGIN;"));
        assertTrue(script.contains("LOCK TABLE room_entity IN EXCLUSIVE MODE;"));
        assertTrue(script.contains("WHERE NOT EXISTS"));
        assertTrue(script.contains("existing.number = sample.number"));
        assertTrue(script.contains("(102, 'SINGLE', 95.00::numeric)"));
        assertTrue(script.contains("(103, 'DOUBLE', 120.00::numeric)"));
        assertTrue(script.contains("(104, 'SUITE', 200.00::numeric)"));
        assertTrue(script.contains("(105, 'SINGLE', 100.00::numeric)"));
        assertFalse(script.contains("101"));
        assertFalse(containsSql(script, "UPDATE\\s+room_entity"));
        assertFalse(containsSql(script, "DELETE\\s+FROM\\s+room_entity"));
        assertTrue(script.contains("COMMIT;"));
    }

    private boolean containsSql(String script, String expression) {
        return Pattern.compile("\\b" + expression + "\\b", Pattern.CASE_INSENSITIVE)
                .matcher(script)
                .find();
    }
}
