package com.centre.service.serviceImpl;

import com.centre.service.model.EtatRequete;
import com.centre.service.model.Requete;
import com.centre.service.model.UserInfo;
import com.centre.service.repository.RequeteRepository;
import com.centre.service.repository.UserInfoRepository;
import com.centre.service.service.RequeteService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class RequeteServiceImpl implements RequeteService {

    private static final Logger log = LoggerFactory.getLogger(RequeteServiceImpl.class);

    @Autowired
    private RequeteRepository requeteRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Override
    public ResponseEntity<?> addRequete(Requete requete) {
        try {
            log.info("Received request to add requete: {}", requete);
            // Set default values for optional fields if not provided
            if (requete.getEtat() == null) {
                requete.setEtat(EtatRequete.NOUVEAU);
            }
            if (requete.getDate() == null) {
                requete.setDate(new Date());
            }

            requeteRepository.save(requete);
            return new ResponseEntity<>("{\"message\":\"Requête ajoutée avec succès\"}", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Erreur lors de l'ajout de la requête\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllRequetes() {
        try {
            List<Requete> requetes = requeteRepository.findAll();
            return new ResponseEntity<>(requetes, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Erreur lors de la récupération des requêtes\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getRequetesByClient(Long clientId) {
        try {
            List<Requete> requetes = requeteRepository.findByClientId(clientId);
            return new ResponseEntity<>(requetes, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving requetes for client {}: {}", clientId, e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Error retrieving requetes for client\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getRequetesByGuichetier(Long guichetierId) {
        try {
            List<Requete> requetes = requeteRepository.findByGuichetierId(guichetierId);
            return new ResponseEntity<>(requetes, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving requetes for guichetier {}: {}", guichetierId, e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Error retrieving requetes for guichetier\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public UserInfo findGuichetierWithLeastRequests() {
        List<UserInfo> guichetiers = userInfoRepository.findActiveGuichetiers();
        UserInfo selectedGuichetier = null;
        long minRequests = Long.MAX_VALUE;

        for (UserInfo guichetier : guichetiers) {
            long count = requeteRepository.countActiveRequetesForGuichetier(guichetier.getId());
            if (count < minRequests) {
                minRequests = count;
                selectedGuichetier = guichetier;
            }
        }

        return selectedGuichetier;
    }

    @Override
    public ResponseEntity<?> updateRequete(Long id, @RequestBody Requete updatedRequete) {
        try {
            Optional<Requete> existingRequeteOpt = requeteRepository.findById(id);
            if (existingRequeteOpt.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Requête non trouvée\"}", HttpStatus.NOT_FOUND);
            }

            // Récupère la requête actuelle
            Requete existingRequete = existingRequeteOpt.get();

            // Vérifie les champs du body (on ne les met à jour que s'ils sont présents et
            // valides)
            boolean isUpdated = false;

            if (updatedRequete.getType() != null) {
                existingRequete.setType(updatedRequete.getType());
                isUpdated = true;
            }

            if (updatedRequete.getDescription() != null && !updatedRequete.getDescription().trim().isEmpty()) {
                existingRequete.setDescription(updatedRequete.getDescription());
                isUpdated = true;
            }

            if (updatedRequete.getEtat() != null) {
                existingRequete.setEtat(updatedRequete.getEtat());
                isUpdated = true;
            }

            if (updatedRequete.getNoteRetour() != null) {
                existingRequete.setNoteRetour(updatedRequete.getNoteRetour());
                isUpdated = true;
            }

            if (updatedRequete.getDate() != null) {
                existingRequete.setDate(updatedRequete.getDate());
                isUpdated = true;
            }

            // Si aucune donnée n'a été mise à jour, renvoie un message d'erreur
            if (!isUpdated) {
                return new ResponseEntity<>("{\"message\":\"Aucun champ valide fourni pour la mise à jour\"}",
                        HttpStatus.BAD_REQUEST);
            }

            // Sauvegarde la requête mise à jour
            requeteRepository.save(existingRequete);
            return new ResponseEntity<>("{\"message\":\"Requête mise à jour avec succès\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la requête: {}", e.getMessage());
            return new ResponseEntity<>(
                    "{\"message\":\"Une erreur est survenue lors de la mise à jour de la requête\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> deleteRequete(Long id) {
        try {
            if (requeteRepository.existsById(id)) {
                requeteRepository.deleteById(id);
                return new ResponseEntity<>("{\"message\":\"Requête supprimée avec succès\"}", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("{\"message\":\"Requête non trouvée\"}", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Erreur lors de la suppression de la requête\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}