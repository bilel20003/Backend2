package com.centre.service.repository;

import com.centre.service.model.Rdv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RdvRepository extends JpaRepository<Rdv, Long> {

    List<Rdv> findByArchiverFalse();

    Optional<Rdv> findByIdAndArchiverFalse(Long id);

    @Query("SELECT COUNT(r) FROM Rdv r WHERE r.technicien.id = :technicienId AND r.archiver = false")
    long countRdvsByTechnicienId(@Param("technicienId") Long technicienId);

    @Query("SELECT COUNT(r) FROM Rdv r WHERE r.client.id = :clientId AND r.status = 'EN_ATTENTE' AND r.archiver = false")
    long countPendingRdvsByClientId(@Param("clientId") Long clientId);
}