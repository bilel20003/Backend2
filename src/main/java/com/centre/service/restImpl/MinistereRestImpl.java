package com.centre.service.restImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import com.centre.service.model.Ministere;
import com.centre.service.rest.MinistereRest;
import com.centre.service.service.MinistereService;

@RestController
public class MinistereRestImpl implements MinistereRest {

    private static final Logger log = LoggerFactory.getLogger(MinistereRestImpl.class);

    @Autowired
    private MinistereService ministereService;

    @Override
    public ResponseEntity<?> addNewMinistere(Ministere ministere) {
        try {
            return ministereService.addNewMinistere(ministere);
        } catch (Exception ex) {
            log.error("Error in addNewMinistere: {}", ex.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllMinisteres() {
        try {
            return ministereService.getAllMinisteres();
        } catch (Exception ex) {
            log.error("Error in getAllMinisteres: {}", ex.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateMinistere(Long id, Ministere ministere) {
        try {
            return ministereService.updateMinistere(id, ministere);
        } catch (Exception ex) {
            log.error("Error in updateMinistere: {}", ex.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> archiveMinistere(Long id) {
        try {
            return ministereService.archiveMinistere(id);
        } catch (Exception ex) {
            log.error("Error in archiveMinistere: {}", ex.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}