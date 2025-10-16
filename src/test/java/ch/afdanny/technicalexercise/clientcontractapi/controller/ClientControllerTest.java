package ch.afdanny.technicalexercise.clientcontractapi.controller;

import ch.afdanny.technicalexercise.clientcontractapi.dto.request.CreateCompanyClientRequest;
import ch.afdanny.technicalexercise.clientcontractapi.dto.request.CreatePersonClientRequest;
import ch.afdanny.technicalexercise.clientcontractapi.dto.request.UpdateClientRequest;
import ch.afdanny.technicalexercise.clientcontractapi.exception.GlobalExceptionHandler;
import ch.afdanny.technicalexercise.clientcontractapi.mapper.ClientMapperImpl;
import ch.afdanny.technicalexercise.clientcontractapi.mapper.ContractMapperImpl;
import ch.afdanny.technicalexercise.clientcontractapi.model.Client;
import ch.afdanny.technicalexercise.clientcontractapi.model.CompanyClient;
import ch.afdanny.technicalexercise.clientcontractapi.model.Contract;
import ch.afdanny.technicalexercise.clientcontractapi.model.PersonClient;
import ch.afdanny.technicalexercise.clientcontractapi.service.ClientService;
import ch.afdanny.technicalexercise.clientcontractapi.service.ContractService;
import ch.afdanny.technicalexercise.clientcontractapi.exception.ConflictException;
import ch.afdanny.technicalexercise.clientcontractapi.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web MVC slice test for ClientController.
 * - Mocks ClientService & ContractService
 * - Uses real MapStruct impls (ClientMapperImpl, ContractMapperImpl)
 * - Verifies 201/200/204 success, and 400/404/409 error mappings
 */
@WebMvcTest(controllers = ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ ClientMapperImpl.class, ContractMapperImpl.class, GlobalExceptionHandler.class })
class ClientControllerTest {

    private static final String BASE = "/v1/clients";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean ClientService clientService;
    @MockitoBean ContractService contractService;

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------
    private static PersonClient samplePerson(UUID id) {
        return PersonClient.builder()
                .id(id)
                .name("Alice")
                .email("alice@test.ch")
                .phone("+41 79 000 00 00")
                .birthdate(LocalDate.of(1990, 1, 1))
                .build();
    }

    private static CompanyClient sampleCompany(UUID id) {
        return CompanyClient.builder()
                .id(id)
                .name("Acme SA")
                .email("contact@acme.ch")
                .phone("+41 21 555 00 00")
                .companyIdentifier("aaa-123")
                .build();
    }

    private static Contract sampleContract(UUID id, UUID clientId) {
        Client client = samplePerson(clientId);
        return Contract.builder()
                .id(id)
                .client(client)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(null)
                .costAmount(new BigDecimal("123.45"))
                .lastUpdateDate(Instant.parse("2025-01-01T12:00:00Z"))
                .build();
    }

    // ---------------------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("POST /v1/clients/person -> 201 Created + Location")
    void createPerson_created() throws Exception {
        var created = samplePerson(UUID.randomUUID());
        var req = new CreatePersonClientRequest(
                "Alice", "alice@test.ch", "+41790000000", LocalDate.of(1990,1,1));

        given(clientService.createPerson(eq("Alice"), eq("alice@test.ch"), eq("+41790000000"), eq(LocalDate.of(1990,1,1))))
                .willReturn(created);

        mvc.perform(post(BASE + "/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/v1/clients/" + created.getId())))
                .andExpect(jsonPath("$.id", is(created.getId().toString())))
                .andExpect(jsonPath("$.name", is("Alice")))
                .andExpect(jsonPath("$.type", is("person")));
    }

    @Test
    @DisplayName("POST /v1/clients/company -> 201 Created + Location")
    void createCompany_created() throws Exception {
        var created = sampleCompany(UUID.randomUUID());
        var req = new CreateCompanyClientRequest(
                "Acme SA", "contact@acme.ch", "+41215550000", "aaa-123");

        given(clientService.createCompany(eq("Acme SA"), eq("contact@acme.ch"), eq("+41215550000"), eq("aaa-123")))
                .willReturn(created);

        mvc.perform(post(BASE + "/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/v1/clients/" + created.getId())))
                .andExpect(jsonPath("$.id", is(created.getId().toString())))
                .andExpect(jsonPath("$.companyIdentifier", is("aaa-123")))
                .andExpect(jsonPath("$.type", is("company")));
    }

    @Test
    @DisplayName("POST /v1/clients/person -> 409 Conflict si email déjà utilisé")
    void createPerson_conflict() throws Exception {
        var req = new CreatePersonClientRequest(
                "Alice", "dup@test.ch", "+41791111111", LocalDate.of(1990,1,1));

        given(clientService.createPerson(anyString(), anyString(), anyString(), any(LocalDate.class)))
                .willThrow(new ConflictException("Email already exists", null));

        mvc.perform(post(BASE + "/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Email")))
                .andExpect(jsonPath("$.status", is(409)));
    }

    // ---------------------------------------------------------------------
    // READ by id (polymorphique)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("GET /v1/clients/{id} -> 200 Person")
    void getById_person_ok() throws Exception {
        var id = UUID.randomUUID();
        var person = samplePerson(id);
        given(clientService.readActive(eq(id))).willReturn(person);

        mvc.perform(get(BASE + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.type", is("person")))
                .andExpect(jsonPath("$.birthdate", is("1990-01-01")));
    }

    @Test
    @DisplayName("GET /v1/clients/{id} -> 200 Company")
    void getById_company_ok() throws Exception {
        var id = UUID.randomUUID();
        var company = sampleCompany(id);
        given(clientService.readActive(eq(id))).willReturn(company);

        mvc.perform(get(BASE + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.type", is("company")))
                .andExpect(jsonPath("$.companyIdentifier", is("aaa-123")));
    }

    @Test
    @DisplayName("GET /v1/clients/{id} -> 404 NotFound")
    void getById_notFound() throws Exception {
        var id = UUID.randomUUID();
        given(clientService.readActive(eq(id))).willThrow(new NotFoundException("Client not found"));

        mvc.perform(get(BASE + "/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Client not found")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    // ---------------------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("PUT /v1/clients/{id} -> 200 OK (person)")
    void update_ok_person() throws Exception {
        var id = UUID.randomUUID();
        var updated = samplePerson(id);
        updated.setName("Alice Updated");
        updated.setEmail("alice.updated@test.ch");
        updated.setPhone("+41790000001");

        var req = new UpdateClientRequest("Alice Updated", "alice.updated@test.ch", "+41790000001");

        given(clientService.updateContactInfo(eq(id), eq("Alice Updated"), eq("alice.updated@test.ch"), eq("+41790000001")))
                .willReturn(updated);

        mvc.perform(put(BASE + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.name", is("Alice Updated")))
                .andExpect(jsonPath("$.type", is("person")));
    }

    @Test
    @DisplayName("PUT /v1/clients/{id} -> 409 Conflict (email unique)")
    void update_conflict() throws Exception {
        var id = UUID.randomUUID();
        var req = new UpdateClientRequest("Bob", "dup@test.ch", "+41791111111");

        given(clientService.updateContactInfo(eq(id), anyString(), anyString(), anyString()))
                .willThrow(new ConflictException("Email already exists", null));

        mvc.perform(put(BASE + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    // ---------------------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /v1/clients/{id} -> 204 No Content")
    void delete_noContent() throws Exception {
        var id = UUID.randomUUID();
        mvc.perform(delete(BASE + "/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /v1/clients/{id} -> 404 NotFound")
    void delete_notFound() throws Exception {
        var id = UUID.randomUUID();

        willThrow(new NotFoundException("Client not found or already deleted"))
                .given(clientService)
                .deleteClient(eq(id));

        mvc.perform(delete(BASE + "/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    // ---------------------------------------------------------------------
    // GET active contracts for a client (+ filtre updatedSince)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("GET /v1/clients/{id}/contracts/active -> 200 OK (sans filtre)")
    void getActiveContracts_noFilter() throws Exception {
        var clientId = UUID.randomUUID();
        var ct1 = sampleContract(UUID.randomUUID(), clientId);
        var ct2 = sampleContract(UUID.randomUUID(), clientId);

        given(clientService.readActive(eq(clientId))).willReturn(samplePerson(clientId));
        given(contractService.listActiveByClient(eq(clientId))).willReturn(List.of(ct1, ct2));

        mvc.perform(get(BASE + "/" + clientId + "/contracts/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].clientId", is(clientId.toString())))
                .andExpect(jsonPath("$[0].startDate", is("2024-01-01")))
                .andExpect(jsonPath("$[0].costAmount", is(123.45)));
    }

    @Test
    @DisplayName("GET /v1/clients/{id}/contracts/active?updatedSince=... -> 200 OK (avec filtre)")
    void getActiveContracts_withUpdatedSince() throws Exception {
        var clientId = UUID.randomUUID();
        var since = LocalDate.parse("2025-10-01");
        var ct = sampleContract(UUID.randomUUID(), clientId);

        given(clientService.readActive(eq(clientId))).willReturn(samplePerson(clientId));
        given(contractService.listActiveByClientSince(eq(clientId), eq(since))).willReturn(List.of(ct));

        mvc.perform(get(BASE + "/" + clientId + "/contracts/active")
                        .param("updatedSince", "2025-10-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].clientId", is(clientId.toString())));
    }

    @Test
    @DisplayName("GET /v1/clients/{id}/contracts/active -> 404 si client inexistant")
    void getActiveContracts_clientNotFound() throws Exception {
        var clientId = UUID.randomUUID();
        given(clientService.readActive(eq(clientId))).willThrow(new NotFoundException("Client not found"));

        mvc.perform(get(BASE + "/" + clientId + "/contracts/active"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}