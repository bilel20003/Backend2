package com.centre.service.restImpl;

import com.centre.service.model.EtatRequete;
import com.centre.service.model.Requete;
import com.centre.service.model.UserInfo;
import com.centre.service.rest.RequeteRest;
import com.centre.service.service.RequeteService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequeteRestImpl implements RequeteRest {

    private static final Logger log = LoggerFactory.getLogger(RequeteRestImpl.class);

    @Autowired
    private RequeteService requeteService;

    @Override
    public ResponseEntity<?> addRequete(@RequestBody Requete requete) {
        try {
            return requeteService.addRequete(requete);
        } catch (Exception e) {
            log.error("Error adding requete: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Error adding requete\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllRequetes() {
        try {
            return requeteService.getAllRequetes();
        } catch (Exception e) {
            log.error("Error retrieving all requetes: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Error retrieving all requetes\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getRequetesByClient(@PathVariable Long clientId) {
        try {
            return requeteService.getRequetesByClient(clientId);
        } catch (Exception e) {
            log.error("Error retrieving requetes for client {}: {}", clientId, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Error retrieving requetes for client\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getRequetesByGuichetier(@PathVariable Long guichetierId) {
        try {
            return requeteService.getRequetesByGuichetier(guichetierId);
        } catch (Exception e) {
            log.error("Error retrieving requetes for guichetier {}: {}", guichetierId, e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Error retrieving requetes for guichetier\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getGuichetierWithLeastRequests() {
        try {
            UserInfo guichetier = requeteService.findGuichetierWithLeastRequests();
            return new ResponseEntity<>(guichetier, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving guichetier with least requests: {}", e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Error retrieving guichetier\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateRequete(@PathVariable Long id, @RequestBody Requete requete) {
        try {
            return requeteService.updateRequete(id, requete);
        } catch (Exception e) {
            log.error("Error updating requete with id {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Error updating requete\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> deleteRequete(@PathVariable Long id) {
        try {
            return requeteService.deleteRequete(id);
        } catch (Exception e) {
            log.error("Error deleting requete with id {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Error deleting requete\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}