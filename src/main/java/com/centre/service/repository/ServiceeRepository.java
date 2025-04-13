package com.centre.service.repository;

import com.centre.service.model.Servicee;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceeRepository extends JpaRepository<Servicee, Long> {
    // Vous pouvez ajouter des méthodes spécifiques si nécessaire
    Optional<Servicee> findByNomService(String nomService);
}