package com.centre.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.centre.service.model.Servicee;
import com.centre.service.repository.ServiceeRepository;
import com.centre.service.service.ServiceeService;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceeServiceImpl implements ServiceeService {

    @Autowired
    private ServiceeRepository serviceRepository;

    @Override
    public ResponseEntity<?> addNewService(Servicee service) {
        try {
            if (service.getNomService() == null || service.getNomService().trim().isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Service name is required\"}", HttpStatus.BAD_REQUEST);
            }
            if (service.getMinistere() == null || service.getMinistere().getId() == null) {
                return new ResponseEntity<>("{\"message\":\"Ministere is required\"}", HttpStatus.BAD_REQUEST);
            }
            Optional<Servicee> existingService = serviceRepository
                    .findByNomServiceAndArchiverFalse(service.getNomService());
            if (existingService.isPresent()) {
                return new ResponseEntity<>("{\"message\":\"Service name already exists\"}", HttpStatus.BAD_REQUEST);
            }
            service.setArchiver(false); // Ensure new services are not archived
            serviceRepository.save(service);
            return new ResponseEntity<>("{\"message\":\"Service created successfully\"}", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong: " + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllServices() {
        try {
            List<Servicee> services = serviceRepository.findAllWithMinistere();
            return new ResponseEntity<>(services, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong: " + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateService(Long id, Servicee service) {
        try {
            Optional<Servicee> optionalService = serviceRepository.findByIdAndArchiverFalse(id);
            if (optionalService.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Service not found or archived\"}", HttpStatus.NOT_FOUND);
            }
            if (service.getNomService() != null && !service.getNomService().trim().isEmpty()) {
                Optional<Servicee> existingService = serviceRepository
                        .findByNomServiceAndArchiverFalse(service.getNomService());
                if (existingService.isPresent() && !existingService.get().getId().equals(id)) {
                    return new ResponseEntity<>("{\"message\":\"Service name already exists\"}",
                            HttpStatus.BAD_REQUEST);
                }
            }
            Servicee existing = optionalService.get();
            if (service.getNomService() != null && !service.getNomService().trim().isEmpty()) {
                existing.setNomService(service.getNomService());
            }
            if (service.getMinistere() != null && service.getMinistere().getId() != null) {
                existing.setMinistere(service.getMinistere());
            }
            existing.setArchiver(false); // Ensure updated services are not archived
            serviceRepository.save(existing);
            return new ResponseEntity<>("{\"message\":\"Service updated successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong: " + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> archiveService(Long id) {
        try {
            Optional<Servicee> optionalService = serviceRepository.findByIdAndArchiverFalse(id);
            if (optionalService.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Service not found or already archived\"}",
                        HttpStatus.NOT_FOUND);
            }
            Servicee service = optionalService.get();
            service.setArchiver(true);
            serviceRepository.save(service);
            return new ResponseEntity<>("{\"message\":\"Service archived successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong: " + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}