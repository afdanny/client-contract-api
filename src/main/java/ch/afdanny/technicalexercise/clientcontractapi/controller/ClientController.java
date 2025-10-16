package ch.afdanny.technicalexercise.clientcontractapi.controller;

import ch.afdanny.technicalexercise.clientcontractapi.dto.request.*;
import ch.afdanny.technicalexercise.clientcontractapi.dto.response.*;
import ch.afdanny.technicalexercise.clientcontractapi.mapper.ClientMapper;
import ch.afdanny.technicalexercise.clientcontractapi.model.*;
import ch.afdanny.technicalexercise.clientcontractapi.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/clients") // ⇐ versioning
public class ClientController {

    private final ClientService service;
    private final ClientMapper mapper;

    // --- CREATE (201 + Location)
    @PostMapping("/person")
    public ResponseEntity<PersonClientResponse> createPerson(
            @Valid @RequestBody CreatePersonClientRequest req,
            UriComponentsBuilder uriBuilder) {

        var created = service.createPerson(req.name(), req.email(), req.phone(), req.birthdate());
        var body = mapper.toPersonResponse(created);
        var location = uriBuilder.path("/v1/clients/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @PostMapping("/company")
    public ResponseEntity<CompanyClientResponse> createCompany(
            @Valid @RequestBody CreateCompanyClientRequest req,
            UriComponentsBuilder uriBuilder) {

        var created = service.createCompany(req.name(), req.email(), req.phone(), req.companyIdentifier());
        var body = mapper.toCompanyResponse(created);
        var location = uriBuilder.path("/v1/clients/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    // --- READ by id (polymorphique)
    @GetMapping("/{id}")
    public ResponseEntity<? extends ClientResponse> getById(@PathVariable UUID id) {
        var c = service.readActive(id);
        if (c instanceof PersonClient p) return ResponseEntity.ok(mapper.toPersonResponse(p));
        if (c instanceof CompanyClient co) return ResponseEntity.ok(mapper.toCompanyResponse(co));
        throw new IllegalStateException("Unknown client subtype: " + c.getClass());
    }

    // --- UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<? extends ClientResponse> update(@PathVariable UUID id,
                                                           @Valid @RequestBody UpdateClientRequest req) {
        var updated = service.updateContactInfo(id, req.name(), req.email(), req.phone());
        if (updated instanceof PersonClient p) return ResponseEntity.ok(mapper.toPersonResponse(p));
        if (updated instanceof CompanyClient co) return ResponseEntity.ok(mapper.toCompanyResponse(co));
        throw new IllegalStateException("Unknown client subtype: " + updated.getClass());
    }

    // --- DELETE (204)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    // --- LIST (pagination + Content-Range + 200/206)
    @GetMapping
    public ResponseEntity<List<? extends ClientResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageable = PageRequest.of(Math.max(0, page), Math.min(200, size));
        var result = service.listActive(pageable);

        // map polymorphique
        List<? extends ClientResponse> body = result.getContent().stream()
                .map(c -> (c instanceof PersonClient p)
                        ? mapper.toPersonResponse(p)
                        : mapper.toCompanyResponse((CompanyClient) c))
                .toList();

        // Content-Range: items start-end/total
        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = start + body.size() - 1;
        var headers = new HttpHeaders();
        headers.add("Content-Range", "items " + (body.isEmpty() ? 0 : start) + "-" + (body.isEmpty() ? 0 : end) + "/" + result.getTotalElements());

        // 206 si partiel, sinon 200
        var status = (result.hasNext() || result.hasPrevious()) ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;
        return new ResponseEntity<>(body, headers, status);
    }
}