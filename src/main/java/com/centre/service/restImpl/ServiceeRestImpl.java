package com.centre.service.restImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import com.centre.service.model.Servicee;
import com.centre.service.rest.ServiceeRest;
import com.centre.service.service.ServiceeService;

@RestController
public class ServiceeRestImpl implements ServiceeRest {

    private static final Logger log = LoggerFactory.getLogger(ServiceeRestImpl.class);

    @Autowired
    private ServiceeService serviceService;

    @Override
    public ResponseEntity<?> addNewService(Servicee service) {
        try {
            return serviceService.addNewService(service);
        } catch (Exception ex) {
            log.error("Error in addNewService: {}", ex.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllServices() {
        try {
            return serviceService.getAllServices();
        } catch (Exception ex) {
            log.error("Error in getAllServices: {}", ex.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateService(Long id, Servicee service) {
        try {
            return serviceService.updateService(id, service);
        } catch (Exception ex) {
            log.error("Error in updateService: {}", ex.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> archiveService(Long id) {
        try {
            return serviceService.archiveService(id);
        } catch (Exception ex) {
            log.error("Error in archiveService: {}", ex.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}