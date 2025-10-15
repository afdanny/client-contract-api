package ch.afdanny.technicalexercise.clientcontractapi.model;

import ch.afdanny.technicalexercise.clientcontractapi.model.enums.ClientType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "company_client")
@Getter @Setter
@NoArgsConstructor
@SuperBuilder
public class CompanyClient extends Client {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z]{3}-\\d{3}$", message = "Invalid company identifier format (e.g. AAA-123)")
    @Column(name = "company_identifier", nullable = false, unique = true)
    private String companyIdentifier;

    @PrePersist
    private void setTypeOnCreate() { setType(ClientType.COMPANY); }
}