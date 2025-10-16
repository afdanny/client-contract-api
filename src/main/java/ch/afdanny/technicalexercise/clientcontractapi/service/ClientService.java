package ch.afdanny.technicalexercise.clientcontractapi.service;

import ch.afdanny.technicalexercise.clientcontractapi.exception.ConflictException;
import ch.afdanny.technicalexercise.clientcontractapi.exception.NotFoundException;
import ch.afdanny.technicalexercise.clientcontractapi.model.*;
import ch.afdanny.technicalexercise.clientcontractapi.repository.ClientRepository;
import ch.afdanny.technicalexercise.clientcontractapi.repository.ContractRepository;
import ch.afdanny.technicalexercise.clientcontractapi.repository.CompanyClientRepository;
import ch.afdanny.technicalexercise.clientcontractapi.repository.PersonClientRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final PersonClientRepository personClientRepository;
    private final CompanyClientRepository companyClientRepository;
    private final ContractRepository contractRepository;

    /**
     * Create a Person client.
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
            return personClientRepository.saveAndFlush(person);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Email already exists or constraint violated", e);
        }
    }

    /**
     * Create a Company client.
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
            return companyClientRepository.saveAndFlush(company);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Email or company identifier already exists / constraint violated", e);
        }
    }

    /**
     * Load an ACTIVE client (ignores soft-deleted ones).
     */
    @Transactional
    public Client readActive(UUID id) {
        return clientRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("Client not found or deleted"));
    }

    /**
     * Update limited fields of a client: name, email, phone.
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

    /**
     * Soft-delete a client and close all its ACTIVE contracts by setting their endDate to today.
     */
    @Transactional
    public void deleteClient(UUID id) {
        Client client = clientRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("Client not found or already deleted"));

        LocalDate today = LocalDate.now();
        contractRepository.closeActiveContracts(id, today);

        client.markAsDeleted();
        clientRepository.save(client);
    }
}