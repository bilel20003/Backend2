package com.centre.service.serviceImpl;

import com.centre.service.model.Produit;
import com.centre.service.repository.ProduitRepository;
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

    @Override
    public ResponseEntity<?> getAllProduits() {
        try {
            List<Produit> produits = produitRepository.findAll();
            return new ResponseEntity<>(produits, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while retrieving all products: {}", e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getProduitById(Long id) {
        try {
            Optional<Produit> produit = produitRepository.findById(id);
            if (produit.isPresent()) {
                return new ResponseEntity<>(produit.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("{\"message\":\"Product not found\"}", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error while retrieving product by ID: {}", e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> createProduit(Produit produit) {
        try {
            // Validation des données du produit
            if (produit.getNom() == null || produit.getNom().isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Product name is required\"}", HttpStatus.BAD_REQUEST);
            }
            // Enregistrement du produit
            Produit savedProduit = produitRepository.save(produit);
            return new ResponseEntity<>(savedProduit, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error while creating product: {}", e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateProduit(Long id, Produit produit) {
        try {
            if (!produitRepository.existsById(id)) {
                return new ResponseEntity<>("{\"message\":\"Product not found\"}", HttpStatus.NOT_FOUND);
            }
            produit.setId(id);
            Produit updatedProduit = produitRepository.save(produit);
            return new ResponseEntity<>(updatedProduit, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while updating product: {}", e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> deleteProduit(Long id) {
        try {
            if (!produitRepository.existsById(id)) {
                return new ResponseEntity<>("{\"message\":\"Product not found\"}", HttpStatus.NOT_FOUND);
            }
            produitRepository.deleteById(id);
            return new ResponseEntity<>("{\"message\":\"Product deleted successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while deleting product: {}", e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}