package ch.afdanny.technicalexercise.clientcontractapi.repository;

import ch.afdanny.technicalexercise.clientcontractapi.model.Contract;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {

    /**
     * Returns all active contracts for a given client.
     * A contract is considered active when its endDate is null or greater than the current date.
     */
    @Query("""
        SELECT ct FROM Contract ct
        WHERE ct.client.id = :clientId
          AND (ct.endDate IS NULL OR ct.endDate > :today)
    """)
    List<Contract> findActiveContractsByClient(@Param("clientId") UUID clientId,
                                               @Param("today") LocalDate today);

    /**
     * Sets the endDate of all active contracts of a given client to today's date.
     * This is used when a client is soft-deleted.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Contract ct
           SET ct.endDate = :today
         WHERE ct.client.id = :clientId
           AND (ct.endDate IS NULL OR ct.endDate > :today)
    """)
    int closeActiveContracts(@Param("clientId") UUID clientId,
                             @Param("today") LocalDate today);
}