package ch.afdanny.technicalexercise.clientcontractapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contract")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    private LocalDate startDate;

    private LocalDate endDate;

    @Positive
    @Column(nullable = false)
    private BigDecimal costAmount;

    @Column(nullable = false)
    private LocalDate lastUpdateDate;

    @PrePersist
    public void onCreate() {
        if (startDate == null) startDate = LocalDate.now();
        lastUpdateDate = LocalDate.now();
    }

    public void setCostAmount(BigDecimal newAmount) {
        this.costAmount = newAmount;
        this.lastUpdateDate = LocalDate.now();
    }
}