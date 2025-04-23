package com.centre.service.repository;

import com.centre.service.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {
    Optional<Produit> findByNom(String nom);

    @Query("SELECT COUNT(u) FROM UserInfo u WHERE u.produit.id = :produitId")
    long countUsersByProduitId(@Param("produitId") Long produitId);
}