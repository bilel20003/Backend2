package com.centre.service.repository;

import com.centre.service.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {
    Optional<Produit> findByNomAndArchiverFalse(String nom);

    @Query("SELECT COUNT(u) FROM UserInfo u WHERE u.produit.id = :produitId AND u.archiver = false")
    long countUsersByProduitId(@Param("produitId") Long produitId);

    Optional<Produit> findByIdAndArchiverFalse(Long id);

    List<Produit> findByArchiverFalse();

    List<Produit> findByArchiverTrue();

    Optional<Produit> findById(Long id);
}