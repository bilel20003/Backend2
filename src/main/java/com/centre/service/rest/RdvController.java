package com.centre.service.rest;

import com.centre.service.repository.UserInfoRepository;
import com.centre.service.model.Rdv;
import com.centre.service.model.UserInfo;
import com.centre.service.repository.RdvRepository;
import com.centre.service.service.RdvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rdvs")
public class RdvController {

    @Autowired
    private RdvService rdvService;

    @Autowired
    private UserInfoRepository personneRepository;

    @Autowired
    private RdvRepository rdvRepository;

    // Récupérer tous les rendez-vous
    @GetMapping
    public ResponseEntity<?> getAllRdv() {
        List<Rdv> rdvs = rdvService.getAllRdv();
        if (rdvs.isEmpty()) {
            return ResponseEntity.status(404).body("Aucun rendez-vous trouvé.");
        }
        return ResponseEntity.ok(rdvs);
    }

    // Récupérer un rendez-vous par ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getRdvById(@PathVariable Long id) {
        Optional<Rdv> rdv = rdvService.getRdvById(id);
        if (rdv.isPresent()) {
            return ResponseEntity.ok(rdv.get());
        } else {
            return ResponseEntity.status(404).body("Rendez-vous avec l'ID " + id + " non trouvé.");
        }
    }

    // Supprimer un rendez-vous par ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRdv(@PathVariable Long id) {
        boolean isDeleted = rdvService.deleteRdv(id);
        if (isDeleted) {
            return ResponseEntity.ok("Rendez-vous supprimé avec succès !");
        } else {
            return ResponseEntity.status(404).body("Rendez-vous avec l'ID " + id + " non trouvé.");
        }
    }

    // Créer un nouveau rendez-vous
    @PostMapping
    public ResponseEntity<?> createRdv(@RequestBody Rdv newRdv) {
        try {
            // Ajouter la date d'envoi automatiquement à la création du RDV
            newRdv.setDateEnvoi(new Date());

            // Récupérer la personne (client, guichetier) en fonction de leur ID
            Optional<UserInfo> clientOpt = personneRepository.findById(newRdv.getClient().getId());
            if (clientOpt.isPresent()) {
                newRdv.setClient(clientOpt.get());
            } else {
                return ResponseEntity.status(404).body("Client non trouvé.");
            }

            Optional<UserInfo> guichetierOpt = personneRepository.findById(newRdv.getGuichetier().getId());
            if (guichetierOpt.isPresent()) {
                newRdv.setGuichetier(guichetierOpt.get());
            } else {
                return ResponseEntity.status(404).body("Guichetier non trouvé.");
            }

            Rdv savedRdv = rdvRepository.save(newRdv);
            return ResponseEntity.ok(savedRdv);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la création du rendez-vous.");
        }
    }

    // Mettre à jour un rendez-vous
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRdv(@PathVariable Long id, @RequestBody Rdv updatedRdv) {
        Rdv updated = rdvService.updateRdv(id, updatedRdv);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.status(404).body("Rendez-vous avec l'ID " + id + " non trouvé.");
        }
    }
}
