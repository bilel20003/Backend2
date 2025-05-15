package com.centre.service.service;

import com.centre.service.model.Objet;
import com.centre.service.model.ObjetType;
import org.springframework.http.ResponseEntity;

public interface ObjetService {

    ResponseEntity<?> getAllObjets();

    ResponseEntity<?> getObjetById(Long id);

    ResponseEntity<?> createObjet(Objet objet);

    ResponseEntity<?> updateObjet(Long id, Objet objet);

    ResponseEntity<?> archiveObjet(Long id);

    ResponseEntity<?> getAllArchivedObjets();

    ResponseEntity<?> unarchiveObjet(Long id);

    // New method to get objects by product and type
    ResponseEntity<?> getObjetsByProduitIdAndType(Long produitId, ObjetType type);

    // New method to get all objects for a product
    ResponseEntity<?> getObjetsByProduitId(Long produitId);
}