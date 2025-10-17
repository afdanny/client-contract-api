package ch.afdanny.technicalexercise.clientcontractapi.mapper;

import ch.afdanny.technicalexercise.clientcontractapi.dto.response.CompanyClientResponse;
import ch.afdanny.technicalexercise.clientcontractapi.dto.response.PersonClientResponse;
import ch.afdanny.technicalexercise.clientcontractapi.model.CompanyClient;
import ch.afdanny.technicalexercise.clientcontractapi.model.PersonClient;
import ch.afdanny.technicalexercise.clientcontractapi.model.enums.ClientType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ClientMapper}.
 * Ensures correct mapping between PersonClient / CompanyClient entities and their DTOs.
 */
class ClientMapperTest {

    private ClientMapper mapper;

    @BeforeEach
    void setup() {
        mapper = Mappers.getMapper(ClientMapper.class);
    }

    @Test
    @DisplayName("toPersonResponse maps all fields correctly")
    void toPersonResponse_mapsFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        PersonClient entity = PersonClient.builder()
                .id(id)
                .name("Alice")
                .email("alice@test.ch")
                .phone("+41798716795")
                .birthdate(LocalDate.of(1990, 1, 1))
                .type(ClientType.PERSON)
                .build();

        PersonClientResponse dto = mapper.toPersonResponse(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.id).isEqualTo(id);
        assertThat(dto.name).isEqualTo("Alice");
        assertThat(dto.email).isEqualTo("alice@test.ch");
        assertThat(dto.phone).isEqualTo("+41798716795");
        assertThat(dto.birthdate).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(dto.type).isEqualTo("person");
    }

    @Test
    @DisplayName("toCompanyResponse maps all fields correctly")
    void toCompanyResponse_mapsFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        CompanyClient entity = CompanyClient.builder()
                .id(id)
                .name("Vaudoise SA")
                .email("contact@vaudoise.ch")
                .phone("+41791234567")
                .companyIdentifier("vau-123")
                .type(ClientType.COMPANY)
                .build();

        CompanyClientResponse dto = mapper.toCompanyResponse(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.id).isEqualTo(id);
        assertThat(dto.name).isEqualTo("Vaudoise SA");
        assertThat(dto.email).isEqualTo("contact@vaudoise.ch");
        assertThat(dto.phone).isEqualTo("+41791234567");
        assertThat(dto.companyIdentifier).isEqualTo("vau-123");
        assertThat(dto.type).isEqualTo("company");
    }


    @Test
    @DisplayName("Mapper returns null when input is null")
    void mapper_handlesNullSafely() {
        assertThat(mapper.toPersonResponse(null)).isNull();
        assertThat(mapper.toCompanyResponse(null)).isNull();
    }
}