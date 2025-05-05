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

    @GetMapping("/{id}")
    ResponseEntity<?> getRdvById(@PathVariable Long id);

    @PutMapping("/{id}")
    ResponseEntity<?> updateRdv(@PathVariable Long id, @RequestBody Rdv rdv);

    @PutMapping("/{id}/archive")
    ResponseEntity<?> archiveRdv(@PathVariable Long id);

    @PutMapping("/{id}/refuse")
    ResponseEntity<?> refuseRdv(@PathVariable Long id, @RequestParam Long technicienId,
            @RequestParam String noteRetour);

    @GetMapping("/client/{clientId}")
    ResponseEntity<?> getRdvsByClient(@PathVariable Long clientId);

    @GetMapping("/technicien/{technicienId}")
    ResponseEntity<?> getRdvsByTechnicien(@PathVariable Long technicienId);

}