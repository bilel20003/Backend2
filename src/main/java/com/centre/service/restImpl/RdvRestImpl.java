package com.centre.service.restImpl;

import com.centre.service.model.Rdv;
import com.centre.service.rest.RdvRest;
import com.centre.service.service.RdvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RdvRestImpl implements RdvRest {

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
    public ResponseEntity<?> refuseRdv(Long id, Long guichetierId) {
        return rdvService.refuseRdv(id, guichetierId);
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