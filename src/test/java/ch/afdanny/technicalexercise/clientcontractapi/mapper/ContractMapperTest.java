package ch.afdanny.technicalexercise.clientcontractapi.mapper;

import ch.afdanny.technicalexercise.clientcontractapi.dto.response.ContractResponse;
import ch.afdanny.technicalexercise.clientcontractapi.model.Client;
import ch.afdanny.technicalexercise.clientcontractapi.model.Contract;
import ch.afdanny.technicalexercise.clientcontractapi.model.PersonClient;
import ch.afdanny.technicalexercise.clientcontractapi.model.enums.ClientType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ContractMapper}.
 * Verifies correct mapping between Contract entity and ContractResponse DTO.
 */
class ContractMapperTest {

    private ContractMapper mapper;

    @BeforeEach
    void setup() {
        mapper = Mappers.getMapper(ContractMapper.class);
    }

    @Test
    @DisplayName("toResponse maps all fields correctly")
    void toResponse_mapsFieldsCorrectly() {
        UUID clientId = UUID.randomUUID();
        UUID contractId = UUID.randomUUID();

        Client client = PersonClient.builder()
                .id(clientId)
                .name("Alice")
                .email("alice@test.ch")
                .phone("+41798716795")
                .birthdate(LocalDate.of(1990, 1, 1))
                .type(ClientType.PERSON)
                .build();

        Contract contract = Contract.builder()
                .id(contractId)
                .client(client)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .costAmount(new BigDecimal("123.45"))
                .lastUpdateDate(LocalDate.of(2025, 10, 17))
                .build();

        ContractResponse dto = mapper.toResponse(contract);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(contractId);
        assertThat(dto.clientId()).isEqualTo(clientId);
        assertThat(dto.startDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(dto.endDate()).isEqualTo(LocalDate.of(2025, 12, 31));
        assertThat(dto.costAmount()).isEqualByComparingTo("123.45");
    }

    @Test
    @DisplayName("Mapper returns null when input is null")
    void toResponse_handlesNullSafely() {
        assertThat(mapper.toResponse(null)).isNull();
    }
}