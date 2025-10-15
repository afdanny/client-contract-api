package ch.afdanny.technicalexercise.clientcontractapi.service;

import ch.afdanny.technicalexercise.clientcontractapi.model.*;
import ch.afdanny.technicalexercise.clientcontractapi.repository.ClientRepository;
import ch.afdanny.technicalexercise.clientcontractapi.repository.ContractRepository;
import ch.afdanny.technicalexercise.clientcontractapi.repository.CompanyClientRepository;
import ch.afdanny.technicalexercise.clientcontractapi.repository.PersonClientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain service handling Client CRUD with the business rules required by the exercise:
 *  - Create a client (two types: Person, Company)
 *  - Read a client (return all its fields) – service returns the entity, the controller maps to DTO
 *  - Update a client (allow name/email/phone only; forbid birthdate and companyIdentifier changes)
 *  - Delete a client (soft delete + set endDate of all active contracts to today)
 *
 * Notes:
 *  - This layer purposefully returns entities; map to DTOs at the controller/facade level.
 *  - Exceptions are domain/HTTP-oriented (NotFound / Conflict) so controllers can translate to 404/409.
 */
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final PersonClientRepository personClientRepository;
    private final CompanyClientRepository companyClientRepository;
    private final ContractRepository contractRepository;

    // -- Exceptions (simple runtime types for brevity) ---------------------------------------------

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String msg) { super(msg); }
    }

    public static class ConflictException extends RuntimeException {
        public ConflictException(String msg, Throwable cause) { super(msg, cause); }
    }

    // -- Create ------------------------------------------------------------------------------------

    /**
     * Create a Person client.
     * Constraints enforced:
     *  - email unique (DB constraint → translated to 409 Conflict)
     *  - birthdate must be in the past (Bean Validation is expected at controller/DTO level)
     */
    @Transactional
    public PersonClient createPerson(String name, String email, String phone, java.time.LocalDate birthdate) {
        PersonClient person = PersonClient.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .birthdate(birthdate)
                .build();
        try {
            return personClientRepository.save(person);
        } catch (DataIntegrityViolationException e) {
            // Typically thrown for unique email violation or invalid constraint mapping
            throw new ConflictException("Email already exists or constraint violated", e);
        }
    }

    /**
     * Create a Company client.
     * Constraints enforced:
     *  - email unique (DB constraint → translated to 409 Conflict)
     *  - companyIdentifier format and uniqueness (Bean Validation + DB constraint)
     */
    @Transactional
    public CompanyClient createCompany(String name, String email, String phone, String companyIdentifier) {
        CompanyClient company = CompanyClient.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .companyIdentifier(companyIdentifier)
                .build();
        try {
            return companyClientRepository.save(company);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Email or company identifier already exists / constraint violated", e);
        }
    }

    // -- Read --------------------------------------------------------------------------------------

    /**
     * Load an ACTIVE client (ignores soft-deleted ones).
     * Returns the entity; controller maps to the proper DTO (Person/Company) using instanceof.
     */
    @Transactional
    public Client readActive(UUID id) {
        return clientRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("Client not found or deleted"));
    }

    // -- Update (partial) --------------------------------------------------------------------------

    /**
     * Update limited fields of a client: name, email, phone.
     * Business rule: birthdate (person) and companyIdentifier (company) MUST NOT be updated.
     * We simply do not touch those fields here.
     *
     * @return the updated entity (PersonClient or CompanyClient)
     */
    @Transactional
    public Client updateContactInfo(UUID id, String name, String email, String phone) {
        Client client = clientRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("Client not found or deleted"));

        client.setName(name);
        client.setEmail(email);
        client.setPhone(phone);

        try {
            return clientRepository.saveAndFlush(client); // <—
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Email already exists / constraint violated", e);
        }
    }

    // -- Delete (soft + contracts endDate) ---------------------------------------------------------

    /**
     * Soft-delete a client and close all its ACTIVE contracts by setting their endDate to today.
     * Rules:
     *  - If already deleted → 404 to keep idempotency semantics explicit for the test.
     *  - Active contract = (endDate IS NULL OR endDate > today).
     *  - We run both operations in a single transaction.
     */
    @Transactional
    public void deleteClient(UUID id) {
        Client client = clientRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("Client not found or already deleted"));

        // 1) Close all active contracts for this client
        LocalDate today = LocalDate.now();
        contractRepository.closeActiveContracts(id, today);

        // 2) Soft-delete the client
        client.markAsDeleted();
        clientRepository.save(client);
    }
}