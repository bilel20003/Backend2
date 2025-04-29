package com.centre.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.centre.service.model.Ministere;
import com.centre.service.repository.MinistereRepository;
import com.centre.service.service.MinistereService;

import java.util.List;
import java.util.Optional;

@Service
public class MinistereServiceImpl implements MinistereService {

    @Autowired
    private MinistereRepository ministereRepository;

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
            ministere.setArchiver(false); // Ensure new ministeres are not archived
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
            existing.setArchiver(false); // Ensure updated ministeres are not archived
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
            return new ResponseEntity<>("{\"message\":\"Ministere archived successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong: " + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}