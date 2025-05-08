package com.centre.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.centre.service.model.Ministere;
import com.centre.service.model.Rdv;
import com.centre.service.model.Requete;
import com.centre.service.model.Servicee;
import com.centre.service.model.UserInfo;
import com.centre.service.repository.MinistereRepository;
import com.centre.service.repository.RdvRepository;
import com.centre.service.repository.RequeteRepository;
import com.centre.service.repository.ServiceeRepository;
import com.centre.service.repository.UserInfoRepository;
import com.centre.service.service.MinistereService;

import java.util.List;
import java.util.Optional;

@Service
public class MinistereServiceImpl implements MinistereService {

    @Autowired
    private MinistereRepository ministereRepository;

    @Autowired
    private ServiceeRepository serviceeRepository;

    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    RequeteRepository requeteRepository;

    @Autowired
    RdvRepository rdvRepository;

    @Override
    public ResponseEntity<?> addNewMinistere(Ministere ministere) {
        try {
            if (ministere.getNomMinistere() == null || ministere.getNomMinistere().trim().isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Ministere name is required\"}", HttpStatus.BAD_REQUEST);
            }
            Optional<Ministere> existingMinistere = ministereRepository
                    .findByNomMinistereAndArchiverFalse(ministere.getNomMinistere());
            if (existingMinistere.isPresent()) {
                return new ResponseEntity<>("{\"message\":\"Ministere name already exists\"}", HttpStatus.BAD_REQUEST);
            }
            ministere.setArchiver(false);
            ministereRepository.save(ministere);
            return new ResponseEntity<>("{\"message\":\"Ministere created successfully\"}", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong: " + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllMinisteres() {
        try {
            List<Ministere> ministeres = ministereRepository.findByArchiverFalse();
            return new ResponseEntity<>(ministeres, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong: " + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateMinistere(Long id, Ministere ministere) {
        try {
            Optional<Ministere> optionalMinistere = ministereRepository.findByIdAndArchiverFalse(id);
            if (optionalMinistere.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Ministere not found or archived\"}", HttpStatus.NOT_FOUND);
            }
            if (ministere.getNomMinistere() != null && !ministere.getNomMinistere().trim().isEmpty()) {
                Optional<Ministere> existingMinistere = ministereRepository
                        .findByNomMinistereAndArchiverFalse(ministere.getNomMinistere());
                if (existingMinistere.isPresent() && !existingMinistere.get().getId().equals(id)) {
                    return new ResponseEntity<>("{\"message\":\"Ministere name already exists\"}",
                            HttpStatus.BAD_REQUEST);
                }
            }
            Ministere existing = optionalMinistere.get();
            if (ministere.getNomMinistere() != null && !ministere.getNomMinistere().trim().isEmpty()) {
                existing.setNomMinistere(ministere.getNomMinistere());
            }
            existing.setArchiver(false);
            ministereRepository.save(existing);
            return new ResponseEntity<>("{\"message\":\"Ministere updated successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong: " + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> archiveMinistere(Long id) {
        try {
            Optional<Ministere> optionalMinistere = ministereRepository.findByIdAndArchiverFalse(id);
            if (optionalMinistere.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Ministere not found or already archived\"}",
                        HttpStatus.NOT_FOUND);
            }
            Ministere ministere = optionalMinistere.get();
            ministere.setArchiver(true);
            ministereRepository.save(ministere);

            // Archive related services
            List<Servicee> services = serviceeRepository.findAll().stream()
                    .filter(s -> s.getMinistere().getId().equals(id) && !s.isArchiver())
                    .toList();
            for (Servicee service : services) {
                service.setArchiver(true);
                serviceeRepository.save(service);
                // Archive related UserInfo
                List<UserInfo> users = userInfoRepository.findAll().stream()
                        .filter(u -> u.getService().getId().equals(service.getId()) && !u.isArchiver())
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
            }

            return new ResponseEntity<>("{\"message\":\"Ministere and related entities archived successfully\"}",
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong: " + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> getAllArchivedMinisteres() {
        try {
            List<Ministere> ministeres = ministereRepository.findByArchiverTrue();
            return new ResponseEntity<>(ministeres, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong: " + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> unarchiveMinistere(Long id) {
        try {
            Optional<Ministere> optionalMinistere = ministereRepository.findById(id);
            if (optionalMinistere.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Ministere not found\"}", HttpStatus.NOT_FOUND);
            }
            Ministere ministere = optionalMinistere.get();
            if (!ministere.isArchiver()) {
                return new ResponseEntity<>("{\"message\":\"Ministere is not archived\"}", HttpStatus.BAD_REQUEST);
            }
            ministere.setArchiver(false);
            ministereRepository.save(ministere);
            return new ResponseEntity<>("{\"message\":\"Ministere unarchived successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong: " + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}