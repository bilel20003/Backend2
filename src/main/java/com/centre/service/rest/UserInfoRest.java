package com.centre.service.rest;

import java.util.List; import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;


import com.centre.service.model.EtatRequete; 
import com.centre.service.model.UserInfo; 
import com.centre.service.model.Requete; 
import com.centre.service.model.Role; 
import com.centre.service.repository.UserInfoRepository;

import com.centre.service.repository.RequeteRepository;

@RequestMapping(path="/api/personnes") 
public interface UserInfoRest {

    // @Autowired
    // private UserInfoRepository personneRepository;

    // @Autowired
    // private RequeteRepository requeteRepository; // Add this line to autowire RequeteRepository



    @PostMapping(path="/addNewAppuser")
     ResponseEntity<?> addNewAppuser(@RequestBody(required = true) UserInfo UserInfo);
    //     {// Validation du rôle
    //     if (newPersonne.getRole() == null || !isValidRole(newPersonne.getRole())) {
    //         return ResponseEntity.status(400).body("Rôle invalide. Les rôles valides sont : CLIENT, GUICHETIER, ADMIN, TECHNICIEN, DIRECTEUR_DACA.");
    //     }

    //     try {
    //         UserInfo savedPersonne = personneRepository.save(newPersonne);
    //         return ResponseEntity.ok(savedPersonne);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(500).body("Erreur interne du serveur lors de la création de la personne.");
    //     }
    // }
    // Récupérer toutes les personnes
    @GetMapping
    public ResponseEntity<?> getAllPersonnes();
    //  {
    //     try {
    //         List<UserInfo> personnes = personneRepository.findAll();
    //         if (personnes.isEmpty()) {
    //             return ResponseEntity.status(404).body("Aucune personne trouvée.");
    //         }
    //         return ResponseEntity.ok(personnes);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(500).body("Erreur interne du serveur lors de la récupération des personnes.");
    //     }
    // }

    // Récupérer une personne par ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getPersonneById(@PathVariable Long id);
    //  {
    //     try {
    //         Optional<UserInfo> personne = personneRepository.findById(id);
    //         if (personne.isPresent()) {
    //             return ResponseEntity.ok(personne.get());
    //         } else {
    //             return ResponseEntity.status(404).body("Personne avec l'ID " + id + " non trouvée.");
    //         }
    //     } catch (Exception e) {
    //         return ResponseEntity.status(500).body("Erreur interne du serveur lors de la récupération de la personne.");
    //     }
    // }

    // Supprimer une personne par ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePersonne(@PathVariable Long id);
    //  {
    //     try {
    //         if (!personneRepository.existsById(id)) {
    //             return ResponseEntity.status(404).body("Personne avec l'ID " + id + " non trouvée.");
    //         } 
    //         personneRepository.deleteById(id);
    //         return ResponseEntity.ok("Personne supprimée avec succès !");
    //     } catch (Exception e) {
    //         return ResponseEntity.status(500).body("Erreur interne du serveur lors de la suppression de la personne.");
    //     }
    // }

    // Créer une nouvelle personne
    

    // Mettre à jour une personne
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePersonne(@PathVariable Long id, @RequestBody UserInfo updatedPersonne) ;
    // {
    //     try {
    //         Optional<UserInfo> existingPersonne = personneRepository.findById(id);
    //         if (existingPersonne.isPresent()) {
    //             UserInfo personne = existingPersonne.get();

    //             // Validation du rôle
    //             if (updatedPersonne.getRole() == null || !isValidRole(updatedPersonne.getRole())) {
    //                 return ResponseEntity.status(400).body("Rôle invalide. Les rôles valides sont : CLIENT, GUICHETIER, ADMIN, TECHNICIEN, DIRECTEUR_DACA.");
    //             }

    //             // Mise à jour des champs
    //             personne.setNom(updatedPersonne.getNom());
    //             personne.setPrenom(updatedPersonne.getPrenom());
    //             personne.setMotDePasse(updatedPersonne.getMotDePasse());
    //             personne.setNumTel(updatedPersonne.getNumTel());
    //             personne.setEmail(updatedPersonne.getEmail()); // Mise à jour de l'email
    //             personne.setRole(updatedPersonne.getRole());

    //             personneRepository.save(personne);
    //             return ResponseEntity.ok(personne);
    //         } else {
    //             return ResponseEntity.status(404).body("Personne avec l'ID " + id + " non trouvée.");
    //         }
    //     } catch (Exception e) {
    //         return ResponseEntity.status(500).body("Erreur interne du serveur lors de la mise à jour de la personne.");
    //     }
    // }
    
    // Vérifier si un rôle est valide
    private boolean isValidRole(Role role) {
        try {
    Role.valueOf(role.name());
            return true;
        } catch (IllegalArgumentException e) {
    return false;
        }
    }

    // Récupérer tous les techniciens disponibles
    @GetMapping("/techniciens")
    public ResponseEntity<?> getAllTechniciens() ;
    // {
    //     try {
    //         List<UserInfo> techniciens = personneRepository.findByRole(Role.TECHNICIEN);
    //         if (techniciens.isEmpty()) {
    //             return ResponseEntity.status(404).body("Aucun technicien trouvé.");
    //         }
    //         return ResponseEntity.ok(techniciens);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(500).body("Erreur interne du serveur lors de la récupération des techniciens.");
    //     }
    // }

    // Assigner un technicien à une requête
    @PutMapping("/{id}/assigner-technicien")
    public ResponseEntity<?> assignTechnician(@PathVariable Long id, @RequestBody Long technicianId);
    //  {
    //     try {
    //         Optional<Requete> requeteOptional = requeteRepository.findById(id); // Use the injected requeteRepository here
    //         if (!requeteOptional.isPresent()) {
    //             return ResponseEntity.status(404).body("Requête avec l'ID " + id + " non trouvée.");
    //         }

    //         Optional<UserInfo> technicienOptional = personneRepository.findById(technicianId);
    //         if (!technicienOptional.isPresent() || technicienOptional.get().getRole() != Role.TECHNICIEN) {
    //             return ResponseEntity.status(400).body("Technicien avec l'ID " + technicianId + " non trouvé ou rôle incorrect.");
    //         }

    //         Requete requete = requeteOptional.get();
    //         UserInfo technicien = technicienOptional.get();

    //         // Assigner le technicien à la requête
    //         requete.setTechnicien(technicien);  // Assurez-vous que la classe Requete a un champ `technicien`
    //         requete.setEtat(EtatRequete.EN_COURS_DE_TRAITEMENT); // Vous pouvez également modifier l'état de la requête
    //         requeteRepository.save(requete);

    //         return ResponseEntity.ok(requete);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(500).body("Erreur lors de l'assignation du technicien.");
    //     }
    // }
}