package com.centre.service.service;

import com.centre.service.model.Objet;
import org.springframework.http.ResponseEntity;

public interface ObjetService {

    ResponseEntity<?> getAllObjets();

    ResponseEntity<?> getObjetById(Long id);

    ResponseEntity<?> createObjet(Objet objet);

    ResponseEntity<?> updateObjet(Long id, Objet objet);

    ResponseEntity<?> archiveObjet(Long id);
}