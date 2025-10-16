package ch.afdanny.technicalexercise.clientcontractapi.repository;

import ch.afdanny.technicalexercise.clientcontractapi.model.Contract;
import ch.afdanny.technicalexercise.clientcontractapi.model.PersonClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ContractRepository using a real PostgreSQL container.
 *
 * Verifies:
 *  - closeActiveContracts(): sets endDate = today for active contracts
 *    (active = endDate IS NULL OR endDate > today)
 *  - findActiveContractsByClient(): returns only active contracts
 */
@DataJpaTest(
        properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.liquibase.enabled=false"
        }
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ContractRepositoryTest {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private PersonClientRepository personClientRepository;

    private PersonClient newPerson(String name, String email) {
        return personClientRepository.save(PersonClient.builder()
                .name(name)
                .email(email)
                .phone("+41790000000")
                .birthdate(LocalDate.of(1990, 1, 1))
                .build());
    }

    private Contract newContract(PersonClient client, LocalDate start, LocalDate end, String cost) {
        return contractRepository.save(Contract.builder()
                .client(client)
                .startDate(start)
                .endDate(end) // may be null
                .costAmount(new BigDecimal(cost))
                .build());
    }

    @Test
    void closeActiveContracts_setsEndDateToToday_forActiveOnes_only() {
        // given
        var client = newPerson("Alice", "alice@test.ch");
        var today  = LocalDate.now();

        // Active #1: no endDate (null)
        var activeNullEnd = newContract(client, today.minusDays(10), null, "100");

        // Active #2: endDate in the future (> today)
        var activeFutureEnd = newContract(client, today.minusDays(20), today.plusDays(5), "200");

        // NOT active #1: already ended yesterday (< today)
        var endedYesterday = newContract(client, today.minusDays(30), today.minusDays(1), "50");

        // NOT active #2: already ends today (== today) → treated as not active by our rule
        var endsToday = newContract(client, today.minusDays(15), today, "75");

        // when
        int updated = contractRepository.closeActiveContracts(client.getId(), today);

        // then
        assertThat(updated).isEqualTo(2); // only the two "active" should be updated

        // reload and assert
        var reloadedNullEnd   = contractRepository.findById(activeNullEnd.getId()).orElseThrow();
        var reloadedFutureEnd = contractRepository.findById(activeFutureEnd.getId()).orElseThrow();
        var reloadedEnded     = contractRepository.findById(endedYesterday.getId()).orElseThrow();
        var reloadedEndsToday = contractRepository.findById(endsToday.getId()).orElseThrow();

        // Active ones now closed today
        assertThat(reloadedNullEnd.getEndDate()).isEqualTo(today);
        assertThat(reloadedFutureEnd.getEndDate()).isEqualTo(today);

        // Non-active ones unchanged
        assertThat(reloadedEnded.getEndDate()).isEqualTo(today.minusDays(1));
        assertThat(reloadedEndsToday.getEndDate()).isEqualTo(today);
    }

    @Test
    void findActiveContractsByClient_returnsOnlyContractsWithNullOrFutureEndDate() {
        // given
        var client = newPerson("Bob", "bob@test.ch");
        var today  = LocalDate.now();

        var active1 = newContract(client, today.minusDays(7), null, "120");                 // active (null)
        var active2 = newContract(client, today.minusDays(14), today.plusDays(3), "180");   // active (future)
        var ended1  = newContract(client, today.minusDays(21), today.minusDays(1), "60");   // not active
        var ended2  = newContract(client, today.minusDays(10), today, "90");                // not active (== today)

        // when
        var actives = contractRepository.findActiveContractsByClient(client.getId(), today);

        // then
        assertThat(actives)
                .extracting(Contract::getId)
                .containsExactlyInAnyOrder(active1.getId(), active2.getId())
                .doesNotContain(ended1.getId(), ended2.getId());
    }
}