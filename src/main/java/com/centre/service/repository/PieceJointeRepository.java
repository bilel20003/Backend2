package com.centre.service.repository;

import com.centre.service.model.PieceJointe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PieceJointeRepository extends JpaRepository<PieceJointe, Long> {
    Optional<PieceJointe> findByIdAndArchiverFalse(Long id);

    // Nouvelle méthode pour récupérer une pièce jointe par ID
    Optional<PieceJointe> findPieceJointeById(Long id);
}