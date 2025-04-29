package com.centre.service.serviceImpl;

import com.centre.service.model.Produit;
import com.centre.service.repository.ProduitRepository;
import com.centre.service.repository.UserInfoRepository;
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

    @Override
    public ResponseEntity<?> getAllProduits() {
        try {
            List<Produit> produits = produitRepository.findByArchiverFalse();
            return new ResponseEntity<>(produits, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while retrieving all products: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getProduitById(Long id) {
        try {
            Optional<Produit> produit = produitRepository.findByIdAndArchiverFalse(id);
            if (produit.isPresent()) {
                return new ResponseEntity<>(produit.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("{\"message\":\"Product not found or archived\"}", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error while retrieving product by ID: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> createProduit(Produit produit) {
        try {
            // Validation des données du produit
            if (produit.getNom() == null || produit.getNom().trim().isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Product name is required\"}", HttpStatus.BAD_REQUEST);
            }
            if (produit.getPrix() != null && produit.getPrix() < 0) {
                return new ResponseEntity<>("{\"message\":\"Price cannot be negative\"}", HttpStatus.BAD_REQUEST);
            }
            Optional<Produit> existingProduit = produitRepository.findByNomAndArchiverFalse(produit.getNom());
            if (existingProduit.isPresent()) {
                return new ResponseEntity<>("{\"message\":\"Product name already exists\"}", HttpStatus.BAD_REQUEST);
            }
            produit.setArchiver(false); // Ensure new products are not archived
            Produit savedProduit = produitRepository.save(produit);
            return new ResponseEntity<>(savedProduit, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error while creating product: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateProduit(Long id, Produit produit) {
        try {
            Optional<Produit> optionalProduit = produitRepository.findByIdAndArchiverFalse(id);
            if (optionalProduit.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Product not found or archived\"}", HttpStatus.NOT_FOUND);
            }
            // Validation des données du produit
            if (produit.getNom() != null && !produit.getNom().trim().isEmpty()) {
                Optional<Produit> existingProduit = produitRepository.findByNomAndArchiverFalse(produit.getNom());
                if (existingProduit.isPresent() && !existingProduit.get().getId().equals(id)) {
                    return new ResponseEntity<>("{\"message\":\"Product name already exists\"}",
                            HttpStatus.BAD_REQUEST);
                }
            }
            Produit existing = optionalProduit.get();
            if (produit.getNom() != null && !produit.getNom().trim().isEmpty()) {
                existing.setNom(produit.getNom());
            }
            if (produit.getDescription() != null) {
                existing.setDescription(produit.getDescription());
            }
            if (produit.getTopologie() != null) {
                existing.setTopologie(produit.getTopologie());
            }
            if (produit.getPrix() != null) {
                if (produit.getPrix() < 0) {
                    return new ResponseEntity<>("{\"message\":\"Price cannot be negative\"}", HttpStatus.BAD_REQUEST);
                }
                existing.setPrix(produit.getPrix());
            }
            existing.setArchiver(false); // Ensure updated products are not archived
            Produit updatedProduit = produitRepository.save(existing);
            return new ResponseEntity<>(updatedProduit, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while updating product: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> archiveProduit(Long id) {
        try {
            Optional<Produit> optionalProduit = produitRepository.findByIdAndArchiverFalse(id);
            if (optionalProduit.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Product not found or already archived\"}",
                        HttpStatus.NOT_FOUND);
            }
            Produit produit = optionalProduit.get();
            // Check if the product is 'Any' and associated with non-archived non-client
            // users
            if ("Any".equalsIgnoreCase(produit.getNom()) && userInfoRepository.countNonClientUsersByProduitId(id) > 0) {
                return new ResponseEntity<>(
                        "{\"message\":\"Cannot archive 'Any' product as it is associated with non-archived non-client users\"}",
                        HttpStatus.BAD_REQUEST);
            }
            // Check if the product is referenced by any non-archived user_info records
            long userCount = produitRepository.countUsersByProduitId(id);
            if (userCount > 0) {
                return new ResponseEntity<>(
                        "{\"message\":\"Cannot archive product as it is associated with non-archived users\"}",
                        HttpStatus.BAD_REQUEST);
            }
            produit.setArchiver(true);
            produitRepository.save(produit);
            return new ResponseEntity<>("{\"message\":\"Product archived successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while archiving product: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}