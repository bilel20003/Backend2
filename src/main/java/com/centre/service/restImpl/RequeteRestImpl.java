package com.centre.service.restImpl;

import com.centre.service.model.Requete;
import com.centre.service.rest.RequeteRest;
import com.centre.service.service.RequeteService;
import com.centre.service.serviceImpl.RequeteServiceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequeteRestImpl implements RequeteRest {

    private static final Logger log = LoggerFactory.getLogger(RequeteServiceImpl.class);

    @Autowired
    private RequeteService requeteService;

    @Override
    public ResponseEntity<?> addRequete(@RequestBody Requete requete) {
        return requeteService.addRequete(requete);
    }

    @Override
    public ResponseEntity<?> getAllRequetes() {
        return requeteService.getAllRequetes();
    }

    @Override
    public ResponseEntity<?> updateRequete(@PathVariable Long id, @RequestBody Requete requete) {
        return requeteService.updateRequete(id, requete);
    }

    @Override
    public ResponseEntity<?> deleteRequete(@PathVariable Long id) {
        return requeteService.deleteRequete(id);
    }
}