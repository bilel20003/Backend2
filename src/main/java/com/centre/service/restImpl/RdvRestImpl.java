package com.centre.service.restImpl;

import com.centre.service.model.Rdv;
import com.centre.service.rest.RdvRest;
import com.centre.service.service.RdvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RdvRestImpl implements RdvRest {

    private static final Logger log = LoggerFactory.getLogger(RdvRestImpl.class);

    @Autowired
    private RdvService rdvService;

    @Override
    public ResponseEntity<?> addRdv(Rdv rdv) {
        return rdvService.addRdv(rdv);
    }

    @Override
    public ResponseEntity<?> getAllRdvs() {
        return rdvService.getAllRdvs();
    }

    @Override
    public ResponseEntity<?> getRdvById(Long id) {
        return rdvService.getRdvById(id);
    }

    @Override
    public ResponseEntity<?> updateRdv(Long id, Rdv rdv) {
        return rdvService.updateRdv(id, rdv);
    }

    @Override
    public ResponseEntity<?> archiveRdv(Long id) {
        return rdvService.archiveRdv(id);
    }

    @Override
    public ResponseEntity<?> refuseRdv(Long id, Long technicienId, String noteRetour) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Utilisateur authentifié: {}, Rôles: {}",
                authentication.getName(), authentication.getAuthorities());
        log.info("Reçu refuseRdv - ID: {}, technicienId: {}, noteRetour: {}", id, technicienId, noteRetour);
        return rdvService.refuseRdv(id, technicienId, noteRetour);
    }

    @Override
    public ResponseEntity<?> getRdvsByClient(Long clientId) {
        return rdvService.getRdvsByClient(clientId);
    }

    @Override
    public ResponseEntity<?> getRdvsByTechnicien(Long technicienId) {
        return rdvService.getRdvsByTechnicien(technicienId);
    }
}