package ch.afdanny.technicalexercise.clientcontractapi.controller;

import ch.afdanny.technicalexercise.clientcontractapi.dto.request.*;
import ch.afdanny.technicalexercise.clientcontractapi.dto.response.*;
import ch.afdanny.technicalexercise.clientcontractapi.mapper.ClientMapper;
import ch.afdanny.technicalexercise.clientcontractapi.mapper.ContractMapper;
import ch.afdanny.technicalexercise.clientcontractapi.model.*;
import ch.afdanny.technicalexercise.clientcontractapi.service.ClientService;
import ch.afdanny.technicalexercise.clientcontractapi.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/clients") // ⇐ versioning
public class ClientController {

    private final ClientService clientService;
    private final ContractService contractService;
    private final ContractMapper contractMapper;
    private final ClientMapper mapper;

    /**
     * Create a person client
     */
    @PostMapping("/person")
    public ResponseEntity<PersonClientResponse> createPerson(
            @Valid @RequestBody CreatePersonClientRequest req,
            UriComponentsBuilder uriBuilder) {
        var created = clientService.createPerson(req.name(), req.email(), req.phone(), req.birthdate());
        var body = mapper.toPersonResponse(created);
        var location = uriBuilder.path("/v1/clients/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    /**
     * Create a company client
     */
    @PostMapping("/company")
    public ResponseEntity<CompanyClientResponse> createCompany(
            @Valid @RequestBody CreateCompanyClientRequest req,
            UriComponentsBuilder uriBuilder) {
        var created = clientService.createCompany(req.name(), req.email(), req.phone(), req.companyIdentifier());
        var body = mapper.toCompanyResponse(created);
        var location = uriBuilder.path("/v1/clients/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    /**
     * Read a client
     */
    @GetMapping("/{id}")
    public ResponseEntity<? extends ClientResponse> getById(@PathVariable UUID id) {
        var c = clientService.readActive(id);
        if (c instanceof PersonClient p) return ResponseEntity.ok(mapper.toPersonResponse(p));
        if (c instanceof CompanyClient co) return ResponseEntity.ok(mapper.toCompanyResponse(co));
        throw new IllegalStateException("Unknown client subtype: " + c.getClass());
    }

    /**
     * Update a client
     */
    @PutMapping("/{id}")
    public ResponseEntity<? extends ClientResponse> update(@PathVariable UUID id,
                                                           @Valid @RequestBody UpdateClientRequest req) {
        var updated = clientService.updateContactInfo(id, req.name(), req.email(), req.phone());
        if (updated instanceof PersonClient p) return ResponseEntity.ok(mapper.toPersonResponse(p));
        if (updated instanceof CompanyClient co) return ResponseEntity.ok(mapper.toCompanyResponse(co));
        throw new IllegalStateException("Unknown client subtype: " + updated.getClass());
    }

    /**
     * Delete a client
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all active contracts for a given client.
     */
    @GetMapping("/{id}/contracts/active")
    public ResponseEntity<List<ContractResponse>> getActiveContractsForClient(
            @PathVariable UUID id,
            @RequestParam(required = false) LocalDate updatedSince
    ) {
        clientService.readActive(id);

        List<Contract> contracts = (updatedSince != null)
                ? contractService.listActiveByClientSince(id, updatedSince)
                : contractService.listActiveByClient(id);

        List<ContractResponse> response = contracts.stream()
                .map(contractMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }


    /**
     * A very performant endpoint that returns the sum of all the cost amounts
     * of the active contracts (current date < endDate or endDate is null)
     * for one client.
     */
    @GetMapping("/{id}/contracts/active/sum")
    public ResponseEntity<ActiveContractsSumResponse> getActiveContractsSum(@PathVariable UUID id) {
        clientService.readActive(id);

        var total = contractService.sumActiveCost(id);
        var response = new ActiveContractsSumResponse(id, total);

        return ResponseEntity.ok(response);
    }
}