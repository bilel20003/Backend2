package com.centre.service.repository;

import com.centre.service.model.Rdv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RdvRepository extends JpaRepository<Rdv, Long> {
    // Vous pouvez ajouter des méthodes personnalisées ici si nécessaire
}