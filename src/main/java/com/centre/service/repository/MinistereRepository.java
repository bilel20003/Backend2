package com.centre.service.repository;

import com.centre.service.model.Ministere;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MinistereRepository extends JpaRepository<Ministere, Long> {
    Optional<Ministere> findByNomMinistere(String nomMinistere); // Ajoutez cette ligne
}