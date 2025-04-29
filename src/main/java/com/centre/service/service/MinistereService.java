package com.centre.service.service;

import org.springframework.http.ResponseEntity;
import com.centre.service.model.Ministere;

public interface MinistereService {

    ResponseEntity<?> addNewMinistere(Ministere ministere);

    ResponseEntity<?> getAllMinisteres();

    ResponseEntity<?> updateMinistere(Long id, Ministere ministere);

    ResponseEntity<?> archiveMinistere(Long id);
}