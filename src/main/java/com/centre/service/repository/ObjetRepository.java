package com.centre.service.repository;

import com.centre.service.model.Objet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObjetRepository extends JpaRepository<Objet, Long> {
}