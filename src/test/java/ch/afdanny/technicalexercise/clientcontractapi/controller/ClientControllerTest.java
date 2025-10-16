package ch.afdanny.technicalexercise.clientcontractapi.controller;

import ch.afdanny.technicalexercise.clientcontractapi.dto.request.CreatePersonClientRequest;
import ch.afdanny.technicalexercise.clientcontractapi.dto.response.PersonClientResponse;
import ch.afdanny.technicalexercise.clientcontractapi.mapper.ClientMapper;
import ch.afdanny.technicalexercise.clientcontractapi.model.enums.ClientType;
import ch.afdanny.technicalexercise.clientcontractapi.model.PersonClient;
import ch.afdanny.technicalexercise.clientcontractapi.service.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration-style controller test verifying endpoint behaviour for /v1/clients/person.
 * Uses @WebMvcTest to slice the web layer and mocks the service + mapper dependencies.
 */
@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @MockBean
    private ClientService service;

    @MockBean
    private ClientMapper mapper;

    @Test
    void createPerson_returns201_andLocation() throws Exception {
        // given
        var req = new CreatePersonClientRequest(
                "Alice",
                "alice@test.ch",
                "+41 79 000 00 00",
                LocalDate.of(1990, 1, 1)
        );

        var entity = PersonClient.builder()
                .name("Alice")
                .email("alice@test.ch")
                .phone("+41 79 000 00 00")
                .birthdate(LocalDate.of(1990, 1, 1))
                .build();
        entity.setId(UUID.randomUUID());
        entity.setType(ClientType.PERSON);

        var resp = new PersonClientResponse();
        resp.id = entity.getId();
        resp.name = "Alice";
        resp.email = "alice@test.ch";
        resp.phone = "+41 79 000 00 00";
        resp.type = "person";
        resp.birthdate = LocalDate.of(1990, 1, 1);

        // when
        Mockito.when(service.createPerson(any(), any(), any(), any())).thenReturn(entity);
        Mockito.when(mapper.toPersonResponse(entity)).thenReturn(resp);

        // then
        mvc.perform(post("/v1/clients/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        Matchers.endsWith("/v1/clients/" + entity.getId())))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@test.ch"))
                .andExpect(jsonPath("$.phone").value("+41 79 000 00 00"))
                .andExpect(jsonPath("$.type").value("person"))
                .andExpect(jsonPath("$.birthdate").value("1990-01-01"));
    }
}