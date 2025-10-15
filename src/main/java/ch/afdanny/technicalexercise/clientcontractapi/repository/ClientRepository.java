package ch.afdanny.technicalexercise.clientcontractapi.repository;

import ch.afdanny.technicalexercise.clientcontractapi.model.Client;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    /**
     * Returns all active clients (not soft-deleted).
     */
    @Query("SELECT c FROM Client c WHERE c.deletedAt IS NULL")
    List<Client> findAllActive();

    /**
     * Returns one active client by id (ignores soft-deleted ones).
     */
    @Query("SELECT c FROM Client c WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Client> findActiveById(@Param("id") UUID id);

    /**
     * Soft-deletes a client by updating the deletedAt field.
     * Can be used for batch operations instead of loading the entity.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE Client c
       SET c.deletedAt = :now
     WHERE c.id = :id
       AND c.deletedAt IS NULL
""")
    int markAsDeleted(@Param("id") UUID id, @Param("now") Instant now);
}