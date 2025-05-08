package com.centre.service.service;

import com.centre.service.model.Produit;
import org.springframework.http.ResponseEntity;

public interface ProduitService {

    ResponseEntity<?> getAllProduits();

    ResponseEntity<?> getProduitById(Long id);

    ResponseEntity<?> createProduit(Produit produit);

    ResponseEntity<?> updateProduit(Long id, Produit produit);

    ResponseEntity<?> archiveProduit(Long id);

    ResponseEntity<?> getAllArchivedProduits();

    ResponseEntity<?> unarchiveProduit(Long id);

}