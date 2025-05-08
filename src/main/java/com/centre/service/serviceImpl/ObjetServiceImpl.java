package com.centre.service.serviceImpl;

import com.centre.service.model.Objet;
import com.centre.service.repository.ObjetRepository;
import com.centre.service.service.ObjetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ObjetServiceImpl implements ObjetService {

    private static final Logger log = LoggerFactory.getLogger(ObjetServiceImpl.class);

    @Autowired
    private ObjetRepository objetRepository;

    @Override
    public ResponseEntity<?> getAllObjets() {
        try {
            List<Objet> objets = objetRepository.findByArchiverFalse();
            return new ResponseEntity<>(objets, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while retrieving all objects: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getObjetById(Long id) {
        try {
            Optional<Objet> objet = objetRepository.findByIdAndArchiverFalse(id);
            if (objet.isPresent()) {
                return new ResponseEntity<>(objet.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("{\"message\":\"Object not found or archived\"}", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error while retrieving object by ID: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> createObjet(Objet objet) {
        try {
            if (objet.getName() == null || objet.getName().trim().isEmpty()) {
                log.error("Object name is required");
                return new ResponseEntity<>("{\"message\":\"Object name is required\"}", HttpStatus.BAD_REQUEST);
            }
            objet.setArchiver(false);
            Objet savedObjet = objetRepository.save(objet);
            return new ResponseEntity<>(savedObjet, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error while creating object: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateObjet(Long id, Objet objet) {
        try {
            Optional<Objet> existingObjet = objetRepository.findByIdAndArchiverFalse(id);
            if (existingObjet.isEmpty()) {
                log.error("Object with ID {} not found or archived", id);
                return new ResponseEntity<>("{\"message\":\"Object not found or archived\"}", HttpStatus.NOT_FOUND);
            }
            Objet currentObjet = existingObjet.get();
            if (objet.getName() != null && !objet.getName().trim().isEmpty()) {
                currentObjet.setName(objet.getName());
            } else {
                log.error("Object name is required for update");
                return new ResponseEntity<>("{\"message\":\"Object name is required\"}", HttpStatus.BAD_REQUEST);
            }
            currentObjet.setArchiver(false);
            Objet updatedObjet = objetRepository.save(currentObjet);
            return new ResponseEntity<>(updatedObjet, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while updating object: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> archiveObjet(Long id) {
        try {
            Optional<Objet> objetOpt = objetRepository.findByIdAndArchiverFalse(id);
            if (objetOpt.isEmpty()) {
                log.error("Object with ID {} not found or already archived", id);
                return new ResponseEntity<>("{\"message\":\"Object not found or already archived\"}",
                        HttpStatus.NOT_FOUND);
            }
            Objet objet = objetOpt.get();
            long requeteCount = objetRepository.countRequetesByObjetId(id);
            if (requeteCount > 0) {
                log.error("Cannot archive object with ID {} as it is associated with {} non-archived requetes", id,
                        requeteCount);
                return new ResponseEntity<>(
                        "{\"message\":\"Cannot archive object as it is associated with non-archived requetes\"}",
                        HttpStatus.BAD_REQUEST);
            }
            objet.setArchiver(true);
            objetRepository.save(objet);
            return new ResponseEntity<>("{\"message\":\"Object archived successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while archiving object: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> getAllArchivedObjets() {
        try {
            List<Objet> objets = objetRepository.findByArchiverTrue();
            return new ResponseEntity<>(objets, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while retrieving archived objects: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> unarchiveObjet(Long id) {
        try {
            Optional<Objet> objetOpt = objetRepository.findById(id);
            if (objetOpt.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Object not found\"}", HttpStatus.NOT_FOUND);
            }
            Objet objet = objetOpt.get();
            if (!objet.isArchiver()) {
                return new ResponseEntity<>("{\"message\":\"Object is not archived\"}", HttpStatus.BAD_REQUEST);
            }
            // Check if produit is not archived
            if (objet.getProduit().isArchiver()) {
                return new ResponseEntity<>("{\"message\":\"Cannot unarchive object: Produit is archived\"}",
                        HttpStatus.BAD_REQUEST);
            }
            objet.setArchiver(false);
            objetRepository.save(objet);
            return new ResponseEntity<>("{\"message\":\"Object unarchived successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while unarchiving object: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}