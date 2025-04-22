package com.centre.service.restImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.centre.service.model.Produit;
import com.centre.service.rest.ProduitRest;
import com.centre.service.service.ProduitService;

@RestController
public class ProduitRestImpl implements ProduitRest {

    private static final Logger log = LoggerFactory.getLogger(ProduitRestImpl.class);

    @Autowired
    private ProduitService produitService;

    @Override
    public ResponseEntity<?> createProduit(Produit produit) {
        try {
            return produitService.createProduit(produit);
        } catch (Exception ex) {
            log.error("Error in createProduit: {}", ex);
        }
        return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getAllProduits() {
        try {
            return produitService.getAllProduits();
        } catch (Exception ex) {
            log.error("Error in getAllProduits: {}", ex);
        }
        return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getProduitById(Long id) {
        try {
            return produitService.getProduitById(id);
        } catch (Exception ex) {
            log.error("Error in getProduitById: {}", ex);
        }
        return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> updateProduit(Long id, Produit produit) {
        try {
            return produitService.updateProduit(id, produit);
        } catch (Exception ex) {
            log.error("Error in updateProduit: {}", ex);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> deleteProduit(Long id) {
        try {
            return produitService.deleteProduit(id);
        } catch (Exception ex) {
            log.error("Error in deleteProduit: {}", ex);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}