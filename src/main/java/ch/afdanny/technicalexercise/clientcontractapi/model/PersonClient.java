package ch.afdanny.technicalexercise.clientcontractapi.model;

import ch.afdanny.technicalexercise.clientcontractapi.model.enums.ClientType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "person_client")
@Getter @Setter
@NoArgsConstructor
@SuperBuilder
public class PersonClient extends Client {

    @Past(message = "Birthdate must be in the past")
    @NotNull(message = "Birthdate is required")
    @Column(nullable = false)
    private LocalDate birthdate;

    @PrePersist
    private void setTypeOnCreate() { setType(ClientType.PERSON); }
}