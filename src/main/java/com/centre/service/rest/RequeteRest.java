package com.centre.service.rest;

import com.centre.service.model.Requete;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "/api/requetes")
public interface RequeteRest {

    @GetMapping
    ResponseEntity<?> getAllRequetes();

    @GetMapping("/client/{clientId}")
    ResponseEntity<?> getRequetesByClient(@PathVariable Long clientId);

    @GetMapping("/guichetier/{guichetierId}")
    ResponseEntity<?> getRequetesByGuichetier(@PathVariable Long guichetierId);

    @GetMapping("/technicien/{technicienId}")
    ResponseEntity<?> getRequetesByTechnicien(@PathVariable Long technicienId);

    @PostMapping("/addrequete")
    ResponseEntity<?> addRequete(@RequestBody Requete requete);

    @GetMapping("/guichetier/least")
    ResponseEntity<?> getGuichetierWithLeastRequests();

    @PutMapping("/updaterequete/{id}")
    ResponseEntity<?> updateRequete(@PathVariable Long id, @RequestBody Requete requete);

    @PutMapping("/archiverequete/{id}")
    ResponseEntity<?> archiveRequete(@PathVariable Long id);
}