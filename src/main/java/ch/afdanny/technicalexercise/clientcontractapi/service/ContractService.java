package ch.afdanny.technicalexercise.clientcontractapi.service;

import ch.afdanny.technicalexercise.clientcontractapi.exception.BadRequestException;
import ch.afdanny.technicalexercise.clientcontractapi.exception.ConflictException;
import ch.afdanny.technicalexercise.clientcontractapi.exception.NotFoundException;
import ch.afdanny.technicalexercise.clientcontractapi.model.Client;
import ch.afdanny.technicalexercise.clientcontractapi.model.Contract;
import ch.afdanny.technicalexercise.clientcontractapi.repository.ClientRepository;
import ch.afdanny.technicalexercise.clientcontractapi.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final ClientRepository clientRepository;

    /**
     * Create a new contract for an ACTIVE client.
     */
    @Transactional
    public Contract create(UUID clientId, LocalDate startDate, LocalDate endDate, BigDecimal costAmount) {
        Client client = clientRepository.findActiveById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found or deleted"));

        validateDateRange(startDate, endDate);

        Contract c = Contract.builder()
                .client(client)
                .startDate(startDate)
                .endDate(endDate) // null => active/open-ended
                .costAmount(costAmount)
                .lastUpdateDate(Instant.now())
                .build();

        try {
            return contractRepository.save(c);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Constraint violation while creating contract", e);
        }
    }

    /**
     * Load a contract by id (regardless of client deletion status).
     */
    @Transactional(readOnly = true)
    public Contract getById(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contract not found"));
    }

    @Transactional(readOnly = true)
    public List<Contract> listActiveByClient(UUID clientId) {
        LocalDate today = LocalDate.now();
        return contractRepository.findActiveContractsByClient(clientId, today);
    }

    @Transactional(readOnly = true)
    public List<Contract> listActiveByClientSince(UUID clientId, Instant updatedSince) {
        LocalDate today = LocalDate.now();
        return contractRepository.findActiveContractsByClientUpdatedSince(clientId, today, updatedSince);
    }

    /**
     * Update mutable fields:
     *  - costAmount
     */
    @Transactional
    public Contract update(UUID id, BigDecimal newCostAmount) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contract not found"));


        if (newCostAmount != null) {
            contract.setCostAmount(newCostAmount);
        }

        contract.setLastUpdateDate(Instant.now());
        try {
            return contractRepository.saveAndFlush(contract);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Constraint violation while updating contract", e);
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal sumActiveCost(UUID clientId) {
        return contractRepository.sumActiveCostByClient(clientId, LocalDate.now());
    }

    private static void validateDateRange(LocalDate start, LocalDate end) {
        if (end != null && end.isBefore(start)) {
            throw new BadRequestException("endDate must be greater than or equal to startDate");
        }
    }
}