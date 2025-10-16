package ch.afdanny.technicalexercise.clientcontractapi.controller;

import ch.afdanny.technicalexercise.clientcontractapi.dto.request.ContractRequest;
import ch.afdanny.technicalexercise.clientcontractapi.dto.response.ContractResponse;
import ch.afdanny.technicalexercise.clientcontractapi.dto.request.ContractUpdateRequest;
import ch.afdanny.technicalexercise.clientcontractapi.mapper.ContractMapper;
import ch.afdanny.technicalexercise.clientcontractapi.model.Contract;
import ch.afdanny.technicalexercise.clientcontractapi.service.ContractService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/contracts")
public class ContractController {

    private final ContractService contractService;
    private final ContractMapper mapper;

    public ContractController(ContractService contractService, ContractMapper mapper) {
        this.contractService = contractService;
        this.mapper = mapper;
    }

    /**
     * Create a contract
     */
    @PostMapping
    public ResponseEntity<ContractResponse> create(@Valid @RequestBody ContractRequest request) {
        Contract contract = contractService.create(
                request.clientId(),
                request.startDate(),
                request.endDate(),
                request.costAmount()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(contract));
    }

    /**
     * Update a contract (only cost amount)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ContractResponse> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ContractUpdateRequest request
    ) {
        Contract updated = contractService.update(
                id,
                request.costAmount()
        );
        return ResponseEntity.ok(mapper.toResponse(updated));
    }
}