package ch.afdanny.technicalexercise.clientcontractapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;

@Entity
public class Client {
    @Id
    private UUID id;
}