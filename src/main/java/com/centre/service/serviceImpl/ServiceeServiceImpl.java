package com.centre.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.centre.service.model.Rdv;
import com.centre.service.model.Requete;
import com.centre.service.model.Servicee;
import com.centre.service.model.UserInfo;
import com.centre.service.repository.ServiceeRepository;
import com.centre.service.repository.UserInfoRepository;
import com.centre.service.repository.RequeteRepository;
import com.centre.service.repository.RdvRepository;
import com.centre.service.service.ServiceeService;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceeServiceImpl implements ServiceeService {

    @Autowired
    private ServiceeRepository serviceeRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private RequeteRepository requeteRepository;

    @Autowired
    private RdvRepository rdvRepository;

    @Override
    public ResponseEntity<?> addNewService(Servicee service) {
        try {
            if (service.getNomService() == null || service.getNomService().trim().isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Service name is required\"}", HttpStatus.BAD_REQUEST);
            }
            if (service.getMinistere() == null || service.getMinistere().getId() == null) {
                return new ResponseEntity<>("{\"message\":\"Ministere is required\"}", HttpStatus.BAD_REQUEST);
            }
            Optional<Servicee> existingService = serviceeRepository
                    .findByNomServiceAndArchiverFalse(service.getNomService());
            if (existingService.isPresent()) {
                return new ResponseEntity<>("{\"message\":\"Service name already exists\"}", HttpStatus.BAD_REQUEST);
            }
            service.setArchiver(false);
            serviceeRepository.save(service);
            return new ResponseEntity<>("{\"message\":\"Service created successfully\"}", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong: " + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllServices() {
        try {
            List<Servicee> services = serviceeRepository.findAllWithMinistere();
            return new ResponseEntity<>(services, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong: " + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateService(Long id, Servicee service) {
        try {
            Optional<Servicee> optionalService = serviceeRepository.findByIdAndArchiverFalse(id);
            if (optionalService.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Service not found or archived\"}", HttpStatus.NOT_FOUND);
            }
            if (service.getNomService() != null && !service.getNomService().trim().isEmpty()) {
                Optional<Servicee> existingService = serviceeRepository
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
            existing.setArchiver(false);
            serviceeRepository.save(existing);
            return new ResponseEntity<>("{\"message\":\"Service updated successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong: " + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> archiveService(Long id) {
        try {
            Optional<Servicee> optionalService = serviceeRepository.findByIdAndArchiverFalse(id);
            if (optionalService.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Service not found or already archived\"}",
                        HttpStatus.NOT_FOUND);
            }
            Servicee service = optionalService.get();
            service.setArchiver(true);
            serviceeRepository.save(service);

            // Archive related UserInfo
            List<UserInfo> users = userInfoRepository.findAll().stream()
                    .filter(u -> u.getService().getId().equals(id) && !u.isArchiver())
                    .toList();
            for (UserInfo user : users) {
                user.setArchiver(true);
                userInfoRepository.save(user);
                // Archive related Requetes and Rdvs for CLIENT role
                if ("CLIENT".equalsIgnoreCase(user.getRole().getName())) {
                    List<Requete> requetes = requeteRepository.findByClientIdAndArchiverFalse(user.getId());
                    for (Requete requete : requetes) {
                        requete.setArchiver(true);
                        requeteRepository.save(requete);
                    }
                    List<Rdv> rdvs = rdvRepository.findByClientIdAndArchiverFalse(user.getId());
                    for (Rdv rdv : rdvs) {
                        rdv.setArchiver(true);
                        rdvRepository.save(rdv);
                    }
                }
            }

            return new ResponseEntity<>("{\"message\":\"Service and related entities archived successfully\"}",
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong: " + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> getAllArchivedServices() {
        try {
            List<Servicee> services = serviceeRepository.findByArchiverTrue();
            return new ResponseEntity<>(services, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong: " + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> unarchiveService(Long id) {
        try {
            Optional<Servicee> optionalService = serviceeRepository.findById(id);
            if (optionalService.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Service not found\"}", HttpStatus.NOT_FOUND);
            }
            Servicee service = optionalService.get();
            if (!service.isArchiver()) {
                return new ResponseEntity<>("{\"message\":\"Service is not archived\"}", HttpStatus.BAD_REQUEST);
            }
            // Check if ministere is not archived
            if (service.getMinistere().isArchiver()) {
                return new ResponseEntity<>("{\"message\":\"Cannot unarchive service: Ministere is archived\"}",
                        HttpStatus.BAD_REQUEST);
            }
            service.setArchiver(false);
            serviceeRepository.save(service);
            return new ResponseEntity<>("{\"message\":\"Service unarchived successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong: " + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}