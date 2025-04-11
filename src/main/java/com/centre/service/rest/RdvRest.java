package com.centre.service.rest;

import com.centre.service.model.Rdv;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "/api/rdvs")
public interface RdvRest {

    @PostMapping
    ResponseEntity<?> addRdv(@RequestBody Rdv rdv);

    @GetMapping
    ResponseEntity<?> getAllRdvs();

    @PutMapping("/{id}")
    ResponseEntity<?> updateRdv(@PathVariable Long id, @RequestBody Rdv rdv);

    @DeleteMapping("/{id}")
    ResponseEntity<?> deleteRdv(@PathVariable Long id);
}