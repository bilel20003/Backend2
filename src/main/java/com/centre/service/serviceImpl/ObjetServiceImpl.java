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
            List<Objet> objets = objetRepository.findAll();
            return new ResponseEntity<>(objets, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while retrieving all objects: {}", e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getObjetById(Long id) {
        try {
            Optional<Objet> objet = objetRepository.findById(id);
            if (objet.isPresent()) {
                return new ResponseEntity<>(objet.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("{\"message\":\"Object not found\"}", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error while retrieving object by ID: {}", e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> createObjet(Objet objet) {
        try {
            // Validation des données de l'objet
            if (objet.getName() == null || objet.getName().isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Object name is required\"}", HttpStatus.BAD_REQUEST);
            }
            // Enregistrement de l'objet
            Objet savedObjet = objetRepository.save(objet);
            return new ResponseEntity<>(savedObjet, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error while creating object: {}", e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateObjet(Long id, Objet objet) {
        try {
            if (!objetRepository.existsById(id)) {
                return new ResponseEntity<>("{\"message\":\"Object not found\"}", HttpStatus.NOT_FOUND);
            }
            objet.setId(id);
            Objet updatedObjet = objetRepository.save(objet);
            return new ResponseEntity<>(updatedObjet, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while updating object: {}", e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> deleteObjet(Long id) {
        try {
            if (!objetRepository.existsById(id)) {
                return new ResponseEntity<>("{\"message\":\"Object not found\"}", HttpStatus.NOT_FOUND);
            }
            objetRepository.deleteById(id);
            return new ResponseEntity<>("{\"message\":\"Object deleted successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while deleting object: {}", e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}