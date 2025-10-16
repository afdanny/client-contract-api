package ch.afdanny.technicalexercise.clientcontractapi.repository;

import ch.afdanny.technicalexercise.clientcontractapi.model.PersonClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PersonClientRepository extends JpaRepository<PersonClient, UUID> {
}