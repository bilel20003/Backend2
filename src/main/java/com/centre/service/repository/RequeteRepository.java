package com.centre.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.centre.service.model.Requete;

public interface RequeteRepository extends JpaRepository<Requete, Long> {
	
}
