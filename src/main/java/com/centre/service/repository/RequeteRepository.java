package com.centre.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.centre.service.model.Requete;

public interface RequeteRepository extends JpaRepository<Requete, Long> {
    List<Requete> findByClientId(Long clientId); // Nouvelle méthode pour récupérer les requêtes par client
}
