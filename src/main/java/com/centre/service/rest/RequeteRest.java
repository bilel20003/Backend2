package com.centre.service.rest;

import com.centre.service.model.Requete;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "/api/requetes")
public interface RequeteRest {

    @GetMapping
    ResponseEntity<?> getAllRequetes();

    @GetMapping("/client/{clientId}") // Nouvelle méthode pour récupérer les requêtes par client
    ResponseEntity<?> getRequetesByClient(@PathVariable Long clientId);

    @GetMapping("/guichetier/{guichetierId}") // Nouvel endpoint
    ResponseEntity<?> getRequetesByGuichetier(@PathVariable Long guichetierId);

    @PostMapping("/addrequete")
    ResponseEntity<?> addRequete(@RequestBody Requete requete);

    @GetMapping("/guichetier/least")
    ResponseEntity<?> getGuichetierWithLeastRequests();

    @PutMapping("/updaterequete/{id}")
    ResponseEntity<?> updateRequete(@PathVariable Long id, @RequestBody Requete requete);

    @DeleteMapping("/deleterequete/{id}")
    ResponseEntity<?> deleteRequete(@PathVariable Long id);
}