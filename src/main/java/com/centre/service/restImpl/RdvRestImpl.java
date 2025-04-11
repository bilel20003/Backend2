package com.centre.service.restImpl;

import com.centre.service.model.Rdv;
import com.centre.service.rest.RdvRest;
import com.centre.service.service.RdvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RdvRestImpl implements RdvRest {

    @Autowired
    private RdvService rdvService;

    @Override
    public ResponseEntity<?> addRdv(@RequestBody Rdv rdv) {
        return rdvService.addRdv(rdv);
    }

    @Override
    public ResponseEntity<?> getAllRdvs() {
        return rdvService.getAllRdvs();
    }

    @Override
    public ResponseEntity<?> updateRdv(@PathVariable Long id, @RequestBody Rdv rdv) {
        return rdvService.updateRdv(id, rdv);
    }

    @Override
    public ResponseEntity<?> deleteRdv(@PathVariable Long id) {
        return rdvService.deleteRdv(id);
    }
}