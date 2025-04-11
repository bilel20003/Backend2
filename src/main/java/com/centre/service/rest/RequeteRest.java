package com.centre.service.rest;

import com.centre.service.model.Requete;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "/api/requetes")
public interface RequeteRest {

    @GetMapping
    ResponseEntity<?> getAllRequetes();

    @PostMapping
    ResponseEntity<?> addRequete(@RequestBody Requete requete);

    @PutMapping("/{id}")
    ResponseEntity<?> updateRequete(@PathVariable Long id, @RequestBody Requete requete);

    @DeleteMapping("/{id}")
    ResponseEntity<?> deleteRequete(@PathVariable Long id);
}