package com.centre.service.service;

import com.centre.service.model.Requete;
import com.centre.service.model.UserInfo;
import org.springframework.http.ResponseEntity;

public interface RequeteService {

    ResponseEntity<?> addRequete(Requete requete);

    ResponseEntity<?> getAllRequetes();

    ResponseEntity<?> getRequetesByClient(Long clientId);

    ResponseEntity<?> getRequetesByGuichetier(Long guichetierId);

    ResponseEntity<?> getRequetesByTechnicien(Long technicienId);

    UserInfo findGuichetierWithLeastRequests();

    ResponseEntity<?> updateRequete(Long id, Requete requete);

    ResponseEntity<?> archiveRequete(Long id);
}