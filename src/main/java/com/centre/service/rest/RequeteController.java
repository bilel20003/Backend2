package com.centre.service.rest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.centre.service.model.EtatRequete;
import com.centre.service.model.Requete;
import com.centre.service.repository.RequeteRepository;
import com.centre.service.service.RequeteService;

@RestController
@RequestMapping("/api/requetes")
public class RequeteController {

    @Autowired
    private RequeteRepository requeteRepository;

    @Autowired
    private RequeteService requeteService;  // Injecting the service

    // Récupérer toutes les requêtes
    @GetMapping
    public ResponseEntity<?> getAllRequetes() {
        try {
            List<Requete> requetes = requeteRepository.findAll();
            if (requetes.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList()); // Retourner une liste vide avec 200 OK
            }
            return ResponseEntity.ok(requetes);
        } catch (Exception e) {
            e.printStackTrace(); // Ajout du log pour voir l'erreur
            return ResponseEntity.status(500).body("Erreur interne du serveur : " + e.getMessage());
        }
    }

    // Récupérer une requête par ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getRequeteById(@PathVariable Long id) {
        try {
            Optional<Requete> requete = requeteRepository.findById(id);
            if (requete.isPresent()) {
                return ResponseEntity.ok(requete.get());
            } else {
                return ResponseEntity.status(404).body("Requête avec l'ID " + id + " non trouvée.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur interne du serveur.");
        }
    }

    // Supprimer une requête par ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRequete(@PathVariable Long id) {
        try {
            // Vérifie si la requête existe
            if (!requeteRepository.existsById(id)) {
                return ResponseEntity.status(404).body("Requête avec l'ID " + id + " non trouvée.");
            } else {
                // Supprime la requête
                requeteRepository.deleteById(id);

                // Vérifie si la suppression a bien eu lieu
                if (requeteRepository.existsById(id)) {
                    return ResponseEntity.status(500).body("Erreur lors de la suppression de la requête.");
                }
                return ResponseEntity.ok("Requête supprimée avec succès !");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la suppression de la requête.");
        }
    }

    // Créer une nouvelle requête
    @PostMapping
    public ResponseEntity<?> createRequete(@RequestBody Requete newRequete) {
        try {
            // Vérification de l'état
            if (newRequete.getEtat() == null || !isValidEtat(newRequete.getEtat())) {
                return ResponseEntity.status(400).body("État invalide. Les états valides sont : NOUVEAU, BROUILLON, EN_COURS_DE_TRAITEMENT, REFUSEE, TRAITEE.");
            }
            
            Requete savedRequete = requeteRepository.save(newRequete);
            return ResponseEntity.ok(savedRequete);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la création de la requête.");
        }
    }

    // Mettre à jour une requête
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRequete(@PathVariable Long id, @RequestBody Requete updatedRequete) {
        try {
            Optional<Requete> existingRequete = requeteRepository.findById(id);
            if (existingRequete.isPresent()) {
                Requete requete = existingRequete.get();

                // Validation de l'état
                if (updatedRequete.getEtat() == null || !isValidEtat(updatedRequete.getEtat())) {
                    return ResponseEntity.status(400).body("État invalide. Les états valides sont : NOUVEAU, BROUILLON, EN_COURS_DE_TRAITEMENT, REFUSEE, TRAITEE.");
                }

                // Mise à jour des champs
                requete.setType(updatedRequete.getType());
                requete.setObjet(updatedRequete.getObjet());
                requete.setDescription(updatedRequete.getDescription());
                requete.setEtat(updatedRequete.getEtat());
                requete.setNoteRetour(updatedRequete.getNoteRetour());
                requete.setDate(updatedRequete.getDate());

                requeteRepository.save(requete);
                return ResponseEntity.ok(requete);
            } else {
                return ResponseEntity.status(404).body("Requête avec l'ID " + id + " non trouvée.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la mise à jour de la requête.");
        }
    }

    // Vérifier si l'état est valide
    private boolean isValidEtat(EtatRequete etat) {
        try {
            EtatRequete.valueOf(etat.name());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Assigner un technicien à une requête
    @PutMapping("/{id}/assign-technician")
    public ResponseEntity<?> assignTechnicianToRequete(@PathVariable Long id, @RequestBody Long technicienId) {
        try {
            Requete updatedRequete = requeteService.assignTechnician(id, technicienId);  // Correctly call service method
            return ResponseEntity.ok(updatedRequete);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de l'assignation du technicien.");
        }
    }
}
