package com.centre.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.centre.service.model.Rdv;

@Repository
public interface RdvRepository extends JpaRepository<Rdv, Long> {
    
}
