package com.centre.service.serviceImpl;

import com.centre.service.model.Produit;
import com.centre.service.model.Rdv;
import com.centre.service.model.Requete;
import com.centre.service.model.Objet;
import com.centre.service.model.UserInfo;
import com.centre.service.repository.ProduitRepository;
import com.centre.service.repository.ObjetRepository;
import com.centre.service.repository.UserInfoRepository;
import com.centre.service.repository.RequeteRepository;
import com.centre.service.repository.RdvRepository;
import com.centre.service.service.ProduitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProduitServiceImpl implements ProduitService {

    private static final Logger log = LoggerFactory.getLogger(ProduitServiceImpl.class);

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private ObjetRepository objetRepository;

    @Autowired
    private RequeteRepository requeteRepository;

    @Autowired
    private RdvRepository rdvRepository;

    @Override
    public ResponseEntity<?> getAllProduits() {
        log.info("Récupération de tous les produits non archivés");
        try {
            List<Produit> produits = produitRepository.findByArchiverFalse();
            if (produits.isEmpty()) {
                log.info("Aucun produit non archivé trouvé");
                return new ResponseEntity<>("{\"message\":\"Aucun produit non archivé trouvé\"}", HttpStatus.OK);
            }
            return new ResponseEntity<>(produits, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de tous les produits : {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Une erreur s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getProduitById(Long id) {
        log.info("Récupération du produit avec l'ID : {}", id);
        try {
            Optional<Produit> produit = produitRepository.findByIdAndArchiverFalse(id);
            if (produit.isPresent()) {
                return new ResponseEntity<>(produit.get(), HttpStatus.OK);
            } else {
                log.warn("Produit non trouvé ou archivé avec l'ID : {}", id);
                return new ResponseEntity<>("{\"message\":\"Produit non trouvé ou archivé\"}", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du produit par ID : {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Une erreur s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> createProduit(Produit produit) {
        log.info("Création d'un nouveau produit : {}", produit.getNom());
        try {
            if (produit == null) {
                log.warn("Aucune donnée de produit fournie pour la création");
                return new ResponseEntity<>("{\"message\":\"Aucune donnée de produit fournie\"}",
                        HttpStatus.BAD_REQUEST);
            }
            if (produit.getNom() == null || produit.getNom().trim().isEmpty()) {
                log.warn("Le nom du produit est requis");
                return new ResponseEntity<>("{\"message\":\"Le nom du produit est requis\"}", HttpStatus.BAD_REQUEST);
            }
            if (produit.getPrix() != null && produit.getPrix() < 0) {
                log.warn("Le prix ne peut pas être négatif : {}", produit.getPrix());
                return new ResponseEntity<>("{\"message\":\"Le prix ne peut pas être négatif\"}",
                        HttpStatus.BAD_REQUEST);
            }
            Optional<Produit> existingProduit = produitRepository.findByNomAndArchiverFalse(produit.getNom());
            if (existingProduit.isPresent()) {
                log.warn("Le nom du produit existe déjà : {}", produit.getNom());
                return new ResponseEntity<>("{\"message\":\"Le nom du produit existe déjà\"}", HttpStatus.BAD_REQUEST);
            }
            produit.setArchiver(false);
            Produit savedProduit = produitRepository.save(produit);
            log.info("Produit créé avec succès avec l'ID : {}", savedProduit.getId());
            return new ResponseEntity<>(savedProduit, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Erreur lors de la création du produit : {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Une erreur s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateProduit(Long id, Produit produit) {
        log.info("Mise à jour du produit avec l'ID : {}", id);
        try {
            Optional<Produit> optionalProduit = produitRepository.findByIdAndArchiverFalse(id);
            if (optionalProduit.isEmpty()) {
                log.warn("Produit non trouvé ou archivé avec l'ID : {}", id);
                return new ResponseEntity<>("{\"message\":\"Produit non trouvé ou archivé\"}", HttpStatus.NOT_FOUND);
            }
            if (produit == null) {
                log.warn("Aucune donnée de produit fournie pour la mise à jour de l'ID : {}", id);
                return new ResponseEntity<>("{\"message\":\"Aucune donnée de produit fournie pour la mise à jour\"}",
                        HttpStatus.BAD_REQUEST);
            }
            if (produit.getNom() != null && !produit.getNom().trim().isEmpty()) {
                Optional<Produit> existingProduit = produitRepository.findByNomAndArchiverFalse(produit.getNom());
                if (existingProduit.isPresent() && !existingProduit.get().getId().equals(id)) {
                    log.warn("Le nom du produit existe déjà : {}", produit.getNom());
                    return new ResponseEntity<>("{\"message\":\"Le nom du produit existe déjà\"}",
                            HttpStatus.BAD_REQUEST);
                }
            }
            Produit existing = optionalProduit.get();
            boolean isUpdated = false;
            if (produit.getNom() != null && !produit.getNom().trim().isEmpty()) {
                existing.setNom(produit.getNom());
                isUpdated = true;
            }
            if (produit.getDescription() != null) {
                existing.setDescription(produit.getDescription());
                isUpdated = true;
            }
            if (produit.getTopologie() != null) {
                existing.setTopologie(produit.getTopologie());
                isUpdated = true;
            }
            if (produit.getPrix() != null) {
                if (produit.getPrix() < 0) {
                    log.warn("Le prix ne peut pas être négatif : {}", produit.getPrix());
                    return new ResponseEntity<>("{\"message\":\"Le prix ne peut pas être négatif\"}",
                            HttpStatus.BAD_REQUEST);
                }
                existing.setPrix(produit.getPrix());
                isUpdated = true;
            }
            if (!isUpdated) {
                log.warn("Aucun champ valide fourni pour la mise à jour du produit avec l'ID : {}", id);
                return new ResponseEntity<>("{\"message\":\"Aucun champ valide fourni pour la mise à jour\"}",
                        HttpStatus.BAD_REQUEST);
            }
            existing.setArchiver(false);
            Produit updatedProduit = produitRepository.save(existing);
            log.info("Produit mis à jour avec succès avec l'ID : {}", updatedProduit.getId());
            return new ResponseEntity<>(updatedProduit, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du produit : {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Une erreur s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> archiveProduit(Long id) {
        log.info("Archivage du produit avec l'ID : {}", id);
        try {
            Optional<Produit> optionalProduit = produitRepository.findByIdAndArchiverFalse(id);
            if (optionalProduit.isEmpty()) {
                log.warn("Produit non trouvé ou déjà archivé avec l'ID : {}", id);
                return new ResponseEntity<>("{\"message\":\"Produit non trouvé ou déjà archivé\"}",
                        HttpStatus.NOT_FOUND);
            }
            Produit produit = optionalProduit.get();
            if ("Any".equalsIgnoreCase(produit.getNom()) && userInfoRepository.countNonClientUsersByProduitId(id) > 0) {
                log.warn(
                        "Impossible d'archiver le produit 'Any' car il est associé à des utilisateurs non archivés et non clients");
                return new ResponseEntity<>(
                        "{\"message\":\"Impossible d'archiver le produit 'Any' car il est associé à des utilisateurs non archivés et non clients\"}",
                        HttpStatus.BAD_REQUEST);
            }
            produit.setArchiver(true);
            produitRepository.save(produit);

            // Archive related UserInfo
            List<UserInfo> users = userInfoRepository.findAll().stream()
                    .filter(u -> u.getProduit().getId().equals(id) && !u.isArchiver())
                    .toList();
            for (UserInfo user : users) {
                user.setArchiver(true);
                userInfoRepository.save(user);
                // Archive related Requetes and Rdvs for CLIENT role
                if ("CLIENT".equalsIgnoreCase(user.getRole().getName())) {
                    List<Requete> requetes = requeteRepository.findByClientIdAndArchiverFalse(user.getId());
                    for (Requete requete : requetes) {
                        requete.setArchiver(true);
                        requeteRepository.save(requete);
                    }
                    List<Rdv> rdvs = rdvRepository.findByClientIdAndArchiverFalse(user.getId());
                    for (Rdv rdv : rdvs) {
                        rdv.setArchiver(true);
                        rdvRepository.save(rdv);
                    }
                }
            }

            // Archive related Objets
            List<Objet> objets = objetRepository.findAll().stream()
                    .filter(o -> o.getProduit().getId().equals(id) && !o.isArchiver())
                    .toList();
            for (Objet objet : objets) {
                long requeteCount = objetRepository.countRequetesByObjetId(objet.getId());
                if (requeteCount == 0) {
                    objet.setArchiver(true);
                    objetRepository.save(objet);
                } else {
                    log.warn("Impossible d'archiver l'objet avec l'ID {} en raison de requêtes non archivées",
                            objet.getId());
                }
            }

            log.info("Produit et entités associées archivés avec succès avec l'ID : {}", id);
            return new ResponseEntity<>("{\"message\":\"Produit et entités associées archivés avec succès\"}",
                    HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de l'archivage du produit : {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Une erreur s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> getAllArchivedProduits() {
        log.info("Récupération de tous les produits archivés");
        try {
            List<Produit> produits = produitRepository.findByArchiverTrue();
            if (produits.isEmpty()) {
                log.info("Aucun produit archivé trouvé");
                return new ResponseEntity<>("{\"message\":\"Aucun produit archivé trouvé\"}", HttpStatus.OK);
            }
            return new ResponseEntity<>(produits, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des produits archivés : {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Une erreur s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> unarchiveProduit(Long id) {
        log.info("Désarchivage du produit avec l'ID : {}", id);
        try {
            Optional<Produit> optionalProduit = produitRepository.findById(id);
            if (optionalProduit.isEmpty()) {
                log.warn("Produit non trouvé avec l'ID : {}", id);
                return new ResponseEntity<>("{\"message\":\"Produit non trouvé\"}", HttpStatus.NOT_FOUND);
            }
            Produit produit = optionalProduit.get();
            if (!produit.isArchiver()) {
                log.warn("Le produit n'est pas archivé avec l'ID : {}", id);
                return new ResponseEntity<>("{\"message\":\"Le produit n'est pas archivé\"}", HttpStatus.BAD_REQUEST);
            }
            produit.setArchiver(false);
            produitRepository.save(produit);
            log.info("Produit désarchivé avec succès avec l'ID : {}", id);
            return new ResponseEntity<>("{\"message\":\"Produit désarchivé avec succès\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors du désarchivage du produit : {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Une erreur s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}