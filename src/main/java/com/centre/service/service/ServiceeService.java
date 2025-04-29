package com.centre.service.service;

import org.springframework.http.ResponseEntity;
import com.centre.service.model.Servicee;

public interface ServiceeService {

    ResponseEntity<?> addNewService(Servicee service);

    ResponseEntity<?> getAllServices();

    ResponseEntity<?> updateService(Long id, Servicee service);

    ResponseEntity<?> archiveService(Long id);
}