package com.centre.service.repository;

import com.centre.service.model.Requete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RequeteRepository extends JpaRepository<Requete, Long> {

    List<Requete> findByClientIdAndArchiverFalse(Long clientId);

    List<Requete> findByGuichetierIdAndArchiverFalse(Long guichetierId);

    List<Requete> findByTechnicienIdAndArchiverFalse(Long technicienId);

    @Query("SELECT COUNT(r) FROM Requete r WHERE r.guichetier.id = :guichetierId AND (r.etat = 'NOUVEAU' OR r.etat = 'EN_COURS_DE_TRAITEMENT') AND r.archiver = false")
    long countActiveRequetesForGuichetier(@Param("guichetierId") Long guichetierId);

    Optional<Requete> findByIdAndArchiverFalse(Long id);

    List<Requete> findByArchiverFalse();
}