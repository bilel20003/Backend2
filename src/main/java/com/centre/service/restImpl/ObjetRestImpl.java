package com.centre.service.restImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.centre.service.model.Objet;
import com.centre.service.rest.ObjetRest;
import com.centre.service.service.ObjetService;

@RestController
public class ObjetRestImpl implements ObjetRest {

    private static final Logger log = LoggerFactory.getLogger(ObjetRestImpl.class);

    @Autowired
    private ObjetService objetService;

    @Override
    public ResponseEntity<?> createObjet(Objet objet) {
        try {
            return objetService.createObjet(objet);
        } catch (Exception ex) {
            log.error("Error in createObjet: {}", ex);
        }
        return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getAllObjets() {
        try {
            return objetService.getAllObjets();
        } catch (Exception ex) {
            log.error("Error in getAllObjets: {}", ex);
        }
        return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getObjetById(Long id) {
        try {
            return objetService.getObjetById(id);
        } catch (Exception ex) {
            log.error("Error in getObjetById: {}", ex);
        }
        return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> updateObjet(Long id, Objet objet) {
        try {
            return objetService.updateObjet(id, objet);
        } catch (Exception ex) {
            log.error("Error in updateObjet: {}", ex);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> deleteObjet(Long id) {
        try {
            return objetService.deleteObjet(id);
        } catch (Exception ex) {
            log.error("Error in deleteObjet: {}", ex);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}