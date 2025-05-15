package com.centre.service.repository;

import com.centre.service.model.Objet;
import com.centre.service.model.ObjetType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObjetRepository extends JpaRepository<Objet, Long> {

    Optional<Objet> findByIdAndArchiverFalse(Long id);

    List<Objet> findByArchiverFalse();

    @Query("SELECT COUNT(r) FROM Requete r WHERE r.objet.id = :objetId AND r.archiver = false")
    long countRequetesByObjetId(@Param("objetId") Long objetId);

    List<Objet> findByArchiverTrue();

    Optional<Objet> findById(Long id);

    // New method to get objects by product and type
    List<Objet> findByProduitIdAndArchiverFalseAndType(Long produitId, ObjetType type);

    // New method to get all objects for a product (for rendezvous and requetes)
    List<Objet> findByProduitIdAndArchiverFalse(Long produitId);
}