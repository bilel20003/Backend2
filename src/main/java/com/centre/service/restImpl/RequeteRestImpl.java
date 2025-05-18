package com.centre.service.restImpl;

import com.centre.service.model.Requete;
import com.centre.service.model.UserInfo;
import com.centre.service.rest.RequeteRest;
import com.centre.service.service.RequeteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class RequeteRestImpl implements RequeteRest {

    private static final Logger log = LoggerFactory.getLogger(RequeteRestImpl.class);

    @Autowired
    private RequeteService requeteService;

    @Override
    public ResponseEntity<?> addRequete(Requete requete, MultipartFile[] files) {
        try {
            log.info("Received request to add requete with files: {}, files count: {}", requete,
                    files != null ? files.length : 0);
            return requeteService.addRequete(requete, files);
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
    public ResponseEntity<?> getRequetesByClient(Long clientId) {
        try {
            return requeteService.getRequetesByClient(clientId);
        } catch (Exception e) {
            log.error("Error retrieving requetes for client {}: {}", clientId, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Error retrieving requetes for client\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getRequetesByGuichetier(Long guichetierId) {
        try {
            return requeteService.getRequetesByGuichetier(guichetierId);
        } catch (Exception e) {
            log.error("Error retrieving requetes for guichetier {}: {}", guichetierId, e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Error retrieving requetes for guichetier\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getRequetesByTechnicien(Long technicienId) {
        try {
            return requeteService.getRequetesByTechnicien(technicienId);
        } catch (Exception e) {
            log.error("Error retrieving requetes for technicien {}: {}", technicienId, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Error retrieving requetes for technicien\"}",
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
    public ResponseEntity<?> updateRequete(Long id, Requete requete) {
        try {
            return requeteService.updateRequete(id, requete);
        } catch (Exception e) {
            log.error("Error updating requete with id {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Error updating requete\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> archiveRequete(Long id) {
        try {
            return requeteService.archiveRequete(id);
        } catch (Exception e) {
            log.error("Error archiving requete with id {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Error archiving requete\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> downloadPieceJointe(Long pieceJointeId) {
        try {
            return requeteService.downloadPieceJointe(pieceJointeId);
        } catch (Exception e) {
            log.error("Error downloading piece jointe with id {}: {}", pieceJointeId, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Error downloading piece jointe\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getPieceJointeParId(Long id) {
        try {
            log.info("Received request to get piece jointe with ID: {}", id);
            return requeteService.getPieceJointeParId(id);
        } catch (Exception e) {
            log.error("Error retrieving piece jointe with ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Error retrieving piece jointe\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}