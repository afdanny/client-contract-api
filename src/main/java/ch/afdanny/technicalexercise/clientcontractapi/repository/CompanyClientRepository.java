package ch.afdanny.technicalexercise.clientcontractapi.repository;

import ch.afdanny.technicalexercise.clientcontractapi.model.CompanyClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CompanyClientRepository extends JpaRepository<CompanyClient, UUID> {
    // Inherits all CRUD methods from JpaRepository
}