package com.centre.service.serviceImpl;

import com.centre.service.model.EtatRequete;
import com.centre.service.model.Requete;
import com.centre.service.model.UserInfo;
import com.centre.service.model.Objet;
import com.centre.service.repository.RequeteRepository;
import com.centre.service.repository.UserInfoRepository;
import com.centre.service.repository.ObjetRepository;
import com.centre.service.service.RequeteService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

    @Autowired
    private ObjetRepository objetRepository;

    @Override
    public ResponseEntity<?> addRequete(Requete requete) {
        try {
            log.info("Received request to add requete: {}", requete);

            // Validate Objet
            if (requete.getObjet() == null || requete.getObjet().getId() == null) {
                log.error("Objet is missing or has no ID");
                return new ResponseEntity<>("{\"message\":\"Objet is required\"}", HttpStatus.BAD_REQUEST);
            }

            // Check if Objet exists
            Optional<Objet> objetOpt = objetRepository.findById(requete.getObjet().getId());
            if (objetOpt.isEmpty()) {
                log.error("Objet with ID {} not found", requete.getObjet().getId());
                return new ResponseEntity<>("{\"message\":\"Objet not found\"}", HttpStatus.BAD_REQUEST);
            }
            requete.setObjet(objetOpt.get());

            // Validate Client (if provided)
            if (requete.getClient() != null && requete.getClient().getId() != null) {
                Optional<UserInfo> clientOpt = userInfoRepository.findById(requete.getClient().getId());
                if (clientOpt.isEmpty()) {
                    log.error("Client with ID {} not found", requete.getClient().getId());
                    return new ResponseEntity<>("{\"message\":\"Client not found\"}", HttpStatus.BAD_REQUEST);
                }
                requete.setClient(clientOpt.get());
            }

            // Validate Guichetier (if provided)
            if (requete.getGuichetier() != null && requete.getGuichetier().getId() != null) {
                Optional<UserInfo> guichetierOpt = userInfoRepository.findById(requete.getGuichetier().getId());
                if (guichetierOpt.isEmpty()) {
                    log.error("Guichetier with ID {} not found", requete.getGuichetier().getId());
                    return new ResponseEntity<>("{\"message\":\"Guichetier not found\"}", HttpStatus.BAD_REQUEST);
                }
                requete.setGuichetier(guichetierOpt.get());
            }

            // Validate Technicien (if provided)
            if (requete.getTechnicien() != null && requete.getTechnicien().getId() != null) {
                Optional<UserInfo> technicienOpt = userInfoRepository.findById(requete.getTechnicien().getId());
                if (technicienOpt.isEmpty()) {
                    log.error("Technicien with ID {} not found", requete.getTechnicien().getId());
                    return new ResponseEntity<>("{\"message\":\"Technicien not found\"}", HttpStatus.BAD_REQUEST);
                }
                requete.setTechnicien(technicienOpt.get());
            }

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
            log.error("Error adding requete: {}", e.getMessage(), e);
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
            log.error("Error retrieving all requetes: {}", e.getMessage(), e);
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
            log.error("Error retrieving requetes for client {}: {}", clientId, e.getMessage(), e);
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
            log.error("Error retrieving requetes for guichetier {}: {}", guichetierId, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Error retrieving requetes for guichetier\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getRequetesByTechnicien(Long technicienId) {
        try {
            List<Requete> requetes = requeteRepository.findByTechnicienId(technicienId);
            return new ResponseEntity<>(requetes, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving requetes for technicien {}: {}", technicienId, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Error retrieving requetes for technicien\"}",
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
    public ResponseEntity<?> updateRequete(Long id, Requete updatedRequete) {
        try {
            Optional<Requete> existingRequeteOpt = requeteRepository.findById(id);
            if (existingRequeteOpt.isEmpty()) {
                log.error("Requete with ID {} not found", id);
                return new ResponseEntity<>("{\"message\":\"Requête non trouvée\"}", HttpStatus.NOT_FOUND);
            }

            Requete existingRequete = existingRequeteOpt.get();
            boolean isUpdated = false;

            // Update Objet if provided
            if (updatedRequete.getObjet() != null && updatedRequete.getObjet().getId() != null) {
                Optional<Objet> objetOpt = objetRepository.findById(updatedRequete.getObjet().getId());
                if (objetOpt.isEmpty()) {
                    log.error("Objet with ID {} not found", updatedRequete.getObjet().getId());
                    return new ResponseEntity<>("{\"message\":\"Objet not found\"}", HttpStatus.BAD_REQUEST);
                }
                existingRequete.setObjet(objetOpt.get());
                isUpdated = true;
            }

            // Update Client if provided
            if (updatedRequete.getClient() != null && updatedRequete.getClient().getId() != null) {
                Optional<UserInfo> clientOpt = userInfoRepository.findById(updatedRequete.getClient().getId());
                if (clientOpt.isEmpty()) {
                    log.error("Client with ID {} not found", updatedRequete.getClient().getId());
                    return new ResponseEntity<>("{\"message\":\"Client not found\"}", HttpStatus.BAD_REQUEST);
                }
                existingRequete.setClient(clientOpt.get());
                isUpdated = true;
            }

            // Update Guichetier if provided
            if (updatedRequete.getGuichetier() != null && updatedRequete.getGuichetier().getId() != null) {
                Optional<UserInfo> guichetierOpt = userInfoRepository.findById(updatedRequete.getGuichetier().getId());
                if (guichetierOpt.isEmpty()) {
                    log.error("Guichetier with ID {} not found", updatedRequete.getGuichetier().getId());
                    return new ResponseEntity<>("{\"message\":\"Guichetier not found\"}", HttpStatus.BAD_REQUEST);
                }
                existingRequete.setGuichetier(guichetierOpt.get());
                isUpdated = true;
            }

            // Update Technicien if provided
            if (updatedRequete.getTechnicien() != null && updatedRequete.getTechnicien().getId() != null) {
                Optional<UserInfo> technicienOpt = userInfoRepository.findById(updatedRequete.getTechnicien().getId());
                if (technicienOpt.isEmpty()) {
                    log.error("Technicien with ID {} not found", updatedRequete.getTechnicien().getId());
                    return new ResponseEntity<>("{\"message\":\"Technicien not found\"}", HttpStatus.BAD_REQUEST);
                }
                existingRequete.setTechnicien(technicienOpt.get());
                isUpdated = true;
            }

            // Update other fields if provided
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

            if (!isUpdated) {
                log.warn("No valid fields provided for update of requete ID {}", id);
                return new ResponseEntity<>("{\"message\":\"Aucun champ valide fourni pour la mise à jour\"}",
                        HttpStatus.BAD_REQUEST);
            }

            requeteRepository.save(existingRequete);
            return new ResponseEntity<>("{\"message\":\"Requête mise à jour avec succès\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error updating requete with ID {}: {}", id, e.getMessage(), e);
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
                log.error("Requete with ID {} not found", id);
                return new ResponseEntity<>("{\"message\":\"Requête non trouvée\"}", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error deleting requete with ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Erreur lors de la suppression de la requête\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}