package com.centre.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.centre.service.model.EtatRequete;
import com.centre.service.model.Requete;

public interface RequeteRepository extends JpaRepository<Requete, Long> {

    List<Requete> findByClientId(Long clientId); // Nouvelle méthode pour récupérer les requêtes par clien

    @Query("SELECT COUNT(r) FROM Requete r WHERE r.guichetier.id = :guichetierId AND (r.etat = 'NOUVEAU' OR r.etat = 'EN_COURS_DE_TRAITEMENT')")
    long countActiveRequetesForGuichetier(@Param("guichetierId") Long guichetierId);

}
