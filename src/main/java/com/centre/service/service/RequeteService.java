package com.centre.service.service;

import com.centre.service.model.Requete;
import org.springframework.http.ResponseEntity;

public interface RequeteService {

    ResponseEntity<?> addRequete(Requete requete);

    ResponseEntity<?> getAllRequetes();

    ResponseEntity<?> getRequetesByClient(Long clientId);

    ResponseEntity<?> updateRequete(Long id, Requete requete);

    ResponseEntity<?> deleteRequete(Long id);
}