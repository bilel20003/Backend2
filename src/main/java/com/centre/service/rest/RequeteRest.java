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

    @PostMapping
    ResponseEntity<?> addRequete(@RequestBody Requete requete);

    @PutMapping("/{id}")
    ResponseEntity<?> updateRequete(@PathVariable Long id, @RequestBody Requete requete);

    @DeleteMapping("/{id}")
    ResponseEntity<?> deleteRequete(@PathVariable Long id);
}