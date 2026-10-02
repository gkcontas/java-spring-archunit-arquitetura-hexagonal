package com.gkcontas.hexagonal.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gkcontas.hexagonal.adapter.out.persistence.memory.InMemoryReservationRepository;
import com.gkcontas.hexagonal.application.port.out.ReservationRepository;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The web adapter exercised against the in-memory outbound adapter.
 *
 * <p>No database and no container, because the HTTP layer has nothing to prove about
 * persistence — that is the contract test's job. Being able to split the two is a direct
 * consequence of the port being an interface the application owns.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("memory")
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReservationRepository reservationRepository;

    /** Far enough ahead that the request stays in the future however long the suite runs. */
    private static final Instant START = Instant.now().plus(Duration.ofDays(30))
            .truncatedTo(java.time.temporal.ChronoUnit.HOURS);

    @BeforeEach
    void clear() {
        ((InMemoryReservationRepository) reservationRepository).clear();
    }

    private String body(String room, Instant start, Duration duration, int attendees) {
        return """
                {"roomId":"%s","organizer":"ana","start":"%s","end":"%s","attendees":%d}
                """.formatted(room, start, start.plus(duration), attendees);
    }

    @Test
    void shouldCreateAReservation() throws Exception {
        mockMvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(body("AQUARIUM", START, Duration.ofHours(1), 4)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomId").value("AQUARIUM"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void shouldAnswer409WhenTheRoomIsTaken() throws Exception {
        mockMvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(body("ATRIUM", START, Duration.ofHours(2), 4)))
                .andExpect(status().isCreated());

        // The domain says "unavailable"; turning that into 409 is this adapter's decision.
        mockMvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(body("ATRIUM", START.plus(Duration.ofHours(1)), Duration.ofHours(1), 4)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Room unavailable"));
    }

    @Test
    void shouldAnswer400WhenTheRoomIsTooSmall() throws Exception {
        mockMvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(body("PHONE_BOOTH", START, Duration.ofHours(1), 10)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(
                        org.hamcrest.Matchers.containsString("the room holds 2")));
    }

    @Test
    void shouldAnswer404ForAnUnknownRoom() throws Exception {
        mockMvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(body("BASEMENT", START, Duration.ofHours(1), 2)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectAPastStartBeforeReachingTheUseCase() throws Exception {
        // Caught by Bean Validation on the request record: a malformed request is the web
        // adapter's problem, and answering it does not need the domain.
        mockMvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(body("AQUARIUM", Instant.parse("2020-01-01T10:00:00Z"),
                                Duration.ofHours(1), 2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(
                        org.hamcrest.Matchers.containsString("start must be in the future")));
    }

    @Test
    void shouldCancelAReservation() throws Exception {
        String created = mockMvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(body("BOARDROOM", START, Duration.ofHours(1), 4)))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(delete("/reservations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldListTheAgendaOfARoomForADay() throws Exception {
        mockMvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(body("AQUARIUM", START, Duration.ofHours(1), 4)))
                .andExpect(status().isCreated());

        String day = START.atZone(java.time.ZoneOffset.UTC).toLocalDate().toString();
        mockMvc.perform(get("/reservations").param("roomId", "AQUARIUM").param("date", day))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldListTheRooms() throws Exception {
        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].id").value("AQUARIUM"));
    }
}
