package ch.afdanny.technicalexercise.clientcontractapi.repository;

import ch.afdanny.technicalexercise.clientcontractapi.model.PersonClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ClientRepository using a real PostgreSQL container.
 *
 * Verifies:
 *  - findAllActive() only returns clients not soft-deleted
 *  - findActiveById() ignores deleted clients
 *  - markAsDeleted() correctly sets deletedAt field
 */
@DataJpaTest(
        properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.liquibase.enabled=false"
        }
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ClientRepositoryTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PersonClientRepository personClientRepository;

    @Test
    void findAllActive_returnsOnlyClientsNotSoftDeleted() {
        // given
        var activeClient = personClientRepository.save(PersonClient.builder()
                .name("Alice Active")
                .email("alice@test.ch")
                .phone("+41790000001")
                .birthdate(LocalDate.of(1990, 1, 1))
                .build());

        var deletedClient = personClientRepository.save(PersonClient.builder()
                .name("Bob Deleted")
                .email("bob@test.ch")
                .phone("+41790000002")
                .birthdate(LocalDate.of(1991, 1, 1))
                .build());

        deletedClient.markAsDeleted();
        personClientRepository.save(deletedClient);

        // when
        var activeClients = clientRepository.findAllActive();

        // then
        assertThat(activeClients)
                .extracting("id")
                .contains(activeClient.getId())
                .doesNotContain(deletedClient.getId());
    }

    @Test
    void findActiveById_returnsOnlyNonDeletedClients() {
        // given
        var client = personClientRepository.save(PersonClient.builder()
                .name("Charlie")
                .email("charlie@test.ch")
                .phone("+41790000003")
                .birthdate(LocalDate.of(1992, 1, 1))
                .build());

        client.markAsDeleted();
        personClientRepository.save(client);

        // when
        var result = clientRepository.findActiveById(client.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void markAsDeleted_updatesDeletedAtField() {
        // given
        var client = personClientRepository.save(PersonClient.builder()
                .name("David")
                .email("david@test.ch")
                .phone("+41790000004")
                .birthdate(LocalDate.of(1985, 5, 5))
                .build());

        // when
        int updated = clientRepository.markAsDeleted(client.getId(), Instant.now());

        // then
        assertThat(updated).isEqualTo(1);

        var reloaded = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(reloaded.getDeletedAt()).isNotNull();
    }
}