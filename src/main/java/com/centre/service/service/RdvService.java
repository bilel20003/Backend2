package com.centre.service.service;

import com.centre.service.model.Rdv;
import org.springframework.http.ResponseEntity;

public interface RdvService {

    ResponseEntity<?> addRdv(Rdv rdv);

    ResponseEntity<?> getAllRdvs();

    ResponseEntity<?> updateRdv(Long id, Rdv rdv);

    ResponseEntity<?> deleteRdv(Long id);
}