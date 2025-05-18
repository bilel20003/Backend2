package com.centre.service.rest;

import com.centre.service.model.Requete;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/addrequete", consumes = { "multipart/form-data" })
    ResponseEntity<?> addRequete(
            @RequestPart("requete") Requete requete,
            @RequestPart(value = "files", required = false) MultipartFile[] files);

    @GetMapping("/guichetier/least")
    ResponseEntity<?> getGuichetierWithLeastRequests();

    @PutMapping("/updaterequete/{id}")
    ResponseEntity<?> updateRequete(@PathVariable Long id, @RequestBody Requete requete);

    @PutMapping("/archiverequete/{id}")
    ResponseEntity<?> archiveRequete(@PathVariable Long id);

    @GetMapping("/download/{pieceJointeId}")
    ResponseEntity<?> downloadPieceJointe(@PathVariable Long pieceJointeId);

    // Nouvel endpoint pour récupérer une pièce jointe par ID
    @GetMapping("/piece-jointe/{id}")
    ResponseEntity<?> getPieceJointeParId(@PathVariable Long id);
}