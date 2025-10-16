package ch.afdanny.technicalexercise.clientcontractapi.controller;

import ch.afdanny.technicalexercise.clientcontractapi.dto.request.ContractRequest;
import ch.afdanny.technicalexercise.clientcontractapi.dto.request.ContractUpdateRequest;
import ch.afdanny.technicalexercise.clientcontractapi.exception.GlobalExceptionHandler;
import ch.afdanny.technicalexercise.clientcontractapi.mapper.ContractMapperImpl;
import ch.afdanny.technicalexercise.clientcontractapi.model.Client;
import ch.afdanny.technicalexercise.clientcontractapi.model.Contract;
import ch.afdanny.technicalexercise.clientcontractapi.model.PersonClient;
import ch.afdanny.technicalexercise.clientcontractapi.service.ContractService;
import ch.afdanny.technicalexercise.clientcontractapi.exception.BadRequestException;
import ch.afdanny.technicalexercise.clientcontractapi.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ContractController.class)
@Import({ ContractMapperImpl.class, GlobalExceptionHandler.class })
class ContractControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ContractService contractService;

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------
    private static Contract sampleContract(UUID id, UUID clientId) {
        Client client = PersonClient.builder()
                .id(clientId)
                .name("Alice")
                .email("alice@test.ch")
                .phone("+41790000000")
                .birthdate(LocalDate.of(1990, 1, 1))
                .build();

        return Contract.builder()
                .id(id)
                .client(client)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(null)
                .costAmount(new BigDecimal("123.45"))
                .lastUpdateDate(LocalDate.parse("2025-01-01"))
                .build();
    }

    // ---------------------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("POST /v1/contracts -> 201 Created")
    void create_returns201() throws Exception {
        UUID clientId = UUID.randomUUID();
        Contract created = sampleContract(UUID.randomUUID(), clientId);

        var req = new ContractRequest(
                clientId,
                LocalDate.of(2024, 1, 1),
                null,
                new BigDecimal("123.45")
        );

        given(contractService.create(eq(clientId), eq(req.startDate()), isNull(), eq(new BigDecimal("123.45"))))
                .willReturn(created);

        mvc.perform(post("/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(created.getId().toString())))
                .andExpect(jsonPath("$.clientId", is(clientId.toString())))
                .andExpect(jsonPath("$.startDate", is("2024-01-01")))
                .andExpect(jsonPath("$.costAmount", is(123.45)));
    }

    @Test
    @DisplayName("POST /v1/contracts with null startDate -> 201 Created (service defaults to today)")
    void create_withNullStartDate_returns201() throws Exception {     UUID clientId = UUID.randomUUID();
        Contract created = sampleContract(UUID.randomUUID(), clientId);
        LocalDate today = LocalDate.now();
        created.setStartDate(today);

        var req = new ContractRequest(
                clientId,
                null,
                null,
                new BigDecimal("123.45")
        );

        given(contractService.create(eq(clientId), isNull(), isNull(), eq(new BigDecimal("123.45"))))
                .willReturn(created);

        mvc.perform(post("/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(created.getId().toString())))
                .andExpect(jsonPath("$.clientId", is(clientId.toString())))
                .andExpect(jsonPath("$.startDate", is(today.toString())))
                .andExpect(jsonPath("$.costAmount", is(123.45)));
    }

    @Test
    @DisplayName("POST /v1/contracts -> 404 when client not found")
    void create_returns404_whenClientMissing() throws Exception {
        UUID clientId = UUID.randomUUID();
        var req = new ContractRequest(
                clientId,
                LocalDate.of(2024, 1, 1),
                null,
                new BigDecimal("10.00")
        );

        given(contractService.create(eq(clientId), any(), any(), any()))
                .willThrow(new NotFoundException("Client not found or deleted"));

        mvc.perform(post("/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Client not found")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("POST /v1/contracts -> 400 when endDate < startDate (domain rule)")
    void create_returns400_whenEndBeforeStart() throws Exception {
        UUID clientId = UUID.randomUUID();
        var req = new ContractRequest(
                clientId,
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 1, 1),
                new BigDecimal("10.00")
        );

        given(contractService.create(eq(clientId), any(), any(), any()))
                .willThrow(new BadRequestException("endDate must be greater than or equal to startDate"));

        mvc.perform(post("/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("endDate")))
                .andExpect(jsonPath("$.status", is(400)));
    }

    // ---------------------------------------------------------------------
    // UPDATE (only costAmount)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("PUT /v1/contracts/{id} -> 200 OK (update only costAmount)")
    void update_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        Contract updated = sampleContract(id, clientId);
        updated.setCostAmount(new BigDecimal("200.00"));
        updated.setLastUpdateDate(LocalDate.now());

        var req = new ContractUpdateRequest(new BigDecimal("200.00"));

        given(contractService.update(eq(id), eq(new BigDecimal("200.00"))))
                .willReturn(updated);

        mvc.perform(put("/v1/contracts/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.clientId", is(clientId.toString())))
                .andExpect(jsonPath("$.costAmount", is(200.00)));
    }

    @Test
    @DisplayName("PUT /v1/contracts/{id} -> 400 when costAmount invalid (Bean Validation)")
    void update_returns400_whenInvalidCost() throws Exception {
        UUID id = UUID.randomUUID();
        // costAmount négatif → rejeté par @Positive avant d'appeler le service
        var req = new ContractUpdateRequest(new BigDecimal("-0.01"));

        mvc.perform(put("/v1/contracts/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /v1/contracts/{id} -> 404 when contract not found")
    void update_returns404_whenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ContractUpdateRequest(new BigDecimal("10.00"));

        given(contractService.update(eq(id), any()))
                .willThrow(new NotFoundException("Contract not found"));

        mvc.perform(put("/v1/contracts/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("Contract not found")));
    }
}