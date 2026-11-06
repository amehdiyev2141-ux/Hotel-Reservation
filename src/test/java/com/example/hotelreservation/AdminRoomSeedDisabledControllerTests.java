package com.example.hotelreservation;

import com.example.hotelreservation.service.RoomSeedService;
import com.example.hotelreservation.controller.AdminRoomSeedController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminRoomSeedController.class)
@TestPropertySource(properties = "admin.seed.enabled=false")
class AdminRoomSeedDisabledControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomSeedService roomSeedService;

    @Test
    void disabledSeedFeatureHidesEndpointAndNeverRunsScript() throws Exception {
        mockMvc.perform(post("/api/internal/admin/seed-rooms")
                        .header("X-Admin-Seed-Token", "any-token"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(roomSeedService);
    }
}
