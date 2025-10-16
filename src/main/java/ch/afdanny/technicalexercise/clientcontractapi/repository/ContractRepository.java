package ch.afdanny.technicalexercise.clientcontractapi.repository;

import ch.afdanny.technicalexercise.clientcontractapi.model.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {

    /**
     * Paginated contracts for a given client (no "active" filter).
     */
    @Query("""
           SELECT ct
             FROM Contract ct
            WHERE ct.client.id = :clientId
           """)
    Page<Contract> findByClientId(@Param("clientId") UUID clientId, Pageable pageable);

    /**
     * Returns all active contracts for a given client at a given date.
     * A contract is considered active when its endDate is null or greater than :today.
     */
    @Query("""
           SELECT ct
             FROM Contract ct
            WHERE ct.client.id = :clientId
              AND (ct.endDate IS NULL OR ct.endDate > :today)
           """)
    List<Contract> findActiveContractsByClient(@Param("clientId") UUID clientId,
                                               @Param("today") LocalDate today);

    /**
     * Closes all ACTIVE contracts (endDate IS NULL or > :today) for the given client
     * by setting endDate = :today and bumping lastUpdateDate to now.
     *
     * @return number of rows updated
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE Contract ct
              SET ct.endDate = :today,
                  ct.lastUpdateDate = CURRENT_TIMESTAMP
            WHERE ct.client.id = :clientId
              AND (ct.endDate IS NULL OR ct.endDate > :today)
           """)
    int closeActiveContracts(@Param("clientId") UUID clientId,
                             @Param("today") LocalDate today);


    @Query("""
            SELECT ct FROM Contract ct
            WHERE ct.client.id = :clientId
              AND (ct.endDate IS NULL OR ct.endDate > :today)
              AND ct.lastUpdateDate >= :updatedSince
            """)
    List<Contract> findActiveContractsByClientUpdatedSince(@Param("clientId") UUID clientId,
                                                           @Param("today") LocalDate today,
                                                           @Param("updatedSince") Instant updatedSince);

    @Query("""
            SELECT COALESCE(SUM(ct.costAmount), 0)
              FROM Contract ct
             WHERE ct.client.id = :clientId
               AND (ct.endDate IS NULL OR ct.endDate > :today)
            """)
    BigDecimal sumActiveCostByClient(@Param("clientId") UUID clientId,
                                     @Param("today") LocalDate today);
}