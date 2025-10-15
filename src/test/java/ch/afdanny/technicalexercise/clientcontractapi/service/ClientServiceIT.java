package ch.afdanny.technicalexercise.clientcontractapi.service;

import ch.afdanny.technicalexercise.clientcontractapi.model.*;
import ch.afdanny.technicalexercise.clientcontractapi.model.enums.*;
import ch.afdanny.technicalexercise.clientcontractapi.repository.ContractRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Full integration test for ClientService.
 *
 * Validates:
 *  - Client creation (Person / Company)
 *  - Update rules (only name/email/phone)
 *  - Soft delete logic (client.deletedAt not null)
 *  - Closing of all active contracts upon delete
 */
@SpringBootTest(
        properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.liquibase.enabled=false"
        }
)
@Import(TestcontainersConfiguration.class)
class ClientServiceIT {

    @Autowired
    private ClientService service;

    @Autowired
    private ContractRepository contractRepository;

    // --------------------------------------------------------------------
    // CREATE
    // --------------------------------------------------------------------

    @Test
    void createPerson_and_createCompany_shouldPersistEntities() {
        var person = service.createPerson("Alice", "alice@test.ch", "+41 79 000 00 00", LocalDate.of(1990, 1, 1));
        var company = service.createCompany("Vaudoise SA", "info@vaudoise.ch", "+41 21 000 00 00", "AAA-123");

        assertThat(person.getId()).isNotNull();
        assertThat(company.getId()).isNotNull();
        assertThat(person.getType()).isEqualTo(ClientType.PERSON);
        assertThat(company.getType()).isEqualTo(ClientType.COMPANY);
    }

    // --------------------------------------------------------------------
    // UPDATE
    // --------------------------------------------------------------------

    @Test
    void updateContactInfo_shouldOnlyAffectNameEmailPhone() {
        var person = service.createPerson("Bob", "bob@test.ch", "+41 79 111 11 11", LocalDate.of(1985, 6, 6));

        var updated = service.updateContactInfo(person.getId(), "Bob Updated", "bob.updated@test.ch", "+41 79 999 99 99");

        assertThat(updated.getName()).isEqualTo("Bob Updated");
        assertThat(updated.getEmail()).isEqualTo("bob.updated@test.ch");
        assertThat(updated.getPhone()).isEqualTo("+41 79 999 99 99");

        // birthdate must remain unchanged (not updatable)
        assertThat(((PersonClient) updated).getBirthdate()).isEqualTo(LocalDate.of(1985, 6, 6));
    }

    @Test
    void updateContactInfo_shouldThrowConflict_whenEmailAlreadyExists() {
        var p1 = service.createPerson("A", "dup@test.ch", "+41 79 111 11 11", LocalDate.of(1990, 1, 1));
        var p2 = service.createPerson("B", "unique@test.ch", "+41 79 222 22 22", LocalDate.of(1991, 1, 1));

        assertThatThrownBy(() ->
                service.updateContactInfo(p2.getId(), "B", "dup@test.ch", "+41 79 222 22 22"))
                .isInstanceOf(ClientService.ConflictException.class);
    }

    // --------------------------------------------------------------------
    // DELETE (Soft delete + contract closure)
    // --------------------------------------------------------------------

    @Test
    void deleteClient_shouldSoftDeleteClient_andCloseActiveContracts() {
        // given: a person with 2 active contracts
        var person = service.createPerson("Charlie", "charlie@test.ch", "+41 79 333 33 33", LocalDate.of(1980, 3, 3));

        contractRepository.save(Contract.builder()
                .client(person)
                .startDate(LocalDate.now().minusDays(10))
                .costAmount(new BigDecimal("100")).build());

        contractRepository.save(Contract.builder()
                .client(person)
                .startDate(LocalDate.now().minusDays(20))
                .costAmount(new BigDecimal("200")).build());

        var today = LocalDate.now();

        // when
        service.deleteClient(person.getId());

        // then: client should no longer be readable (soft-deleted)
        assertThatThrownBy(() -> service.readActive(person.getId()))
                .isInstanceOf(ClientService.NotFoundException.class);

        // and: all contracts must have endDate = today
        var contracts = contractRepository.findAll();
        assertThat(contracts)
                .allMatch(c -> today.equals(c.getEndDate()))
                .hasSize(2);
    }

    @Test
    void deleteClient_shouldThrowNotFound_whenAlreadyDeleted() {
        var person = service.createPerson("Deleted", "deleted@test.ch", "+41 79 444 44 44", LocalDate.of(1995, 5, 5));

        service.deleteClient(person.getId());

        assertThatThrownBy(() -> service.deleteClient(person.getId()))
                .isInstanceOf(ClientService.NotFoundException.class);
    }
}