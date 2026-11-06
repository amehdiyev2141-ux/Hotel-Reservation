package com.example.hotelreservation;

import com.example.hotelreservation.service.RoomSeedService;
import com.example.hotelreservation.controller.AdminRoomSeedController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminRoomSeedController.class)
@TestPropertySource(properties = {
        "admin.seed.enabled=true",
        "admin.seed.token=0123456789abcdef0123456789abcdef"
})
class AdminRoomSeedControllerTests {

    private static final String TOKEN_HEADER = "X-Admin-Seed-Token";
    private static final String SEED_ENDPOINT = "/api/internal/admin/seed-rooms";
    private static final String VALID_TOKEN = "0123456789abcdef0123456789abcdef";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomSeedService roomSeedService;

    @Test
    void missingTokenReturnsUnauthorizedAndDoesNotRunSeed() throws Exception {
        mockMvc.perform(post(SEED_ENDPOINT))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("A valid admin seed token is required"));

        verifyNoInteractions(roomSeedService);
    }

    @Test
    void incorrectTokenReturnsUnauthorizedAndDoesNotRunSeed() throws Exception {
        mockMvc.perform(post(SEED_ENDPOINT).header(TOKEN_HEADER, "incorrect-token"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(roomSeedService);
    }

    @Test
    void validTokenExecutesSeedAndReturnsSuccess() throws Exception {
        mockMvc.perform(post(SEED_ENDPOINT).header(TOKEN_HEADER, VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sample room seed script executed successfully"));

        verify(roomSeedService).seedRooms();
    }
}
