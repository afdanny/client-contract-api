package ch.afdanny.technicalexercise.clientcontractapi.model;

import ch.afdanny.technicalexercise.clientcontractapi.model.enums.ClientType;
import ch.afdanny.technicalexercise.clientcontractapi.validation.Phone;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "client")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Email @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @Phone
    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientType type;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<Contract> contracts = new ArrayList<>();

    @Column(name = "deleted_at")
    private Instant deletedAt; // soft delete

    public boolean isDeleted() { return deletedAt != null; }

    public void markAsDeleted() { this.deletedAt = Instant.now(); }
}