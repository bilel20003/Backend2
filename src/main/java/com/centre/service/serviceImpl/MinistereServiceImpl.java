package com.centre.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.centre.service.model.Ministere;
import com.centre.service.repository.MinistereRepository;
import com.centre.service.service.MinistereService;

import java.util.List;

@Service
public class MinistereServiceImpl implements MinistereService {

    @Autowired
    private MinistereRepository ministereRepository;

    @Override
    public ResponseEntity<?> addNewMinistere(Ministere ministere) {
        try {
            ministereRepository.save(ministere);
            return new ResponseEntity<>("{\"message\":\"Ministere created successfully\"}", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllMinisteres() {
        try {
            List<Ministere> ministeres = ministereRepository.findAll();
            return new ResponseEntity<>(ministeres, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateMinistere(Long id, Ministere ministere) {
        try {
            if (!ministereRepository.existsById(id)) {
                return new ResponseEntity<>("{\"message\":\"Ministere not found\"}", HttpStatus.NOT_FOUND);
            }
            ministere.setId(id);
            ministereRepository.save(ministere);
            return new ResponseEntity<>("{\"message\":\"Ministere updated successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> deleteMinistere(Long id) {
        try {
            if (!ministereRepository.existsById(id)) {
                return new ResponseEntity<>("{\"message\":\"Ministere not found\"}", HttpStatus.NOT_FOUND);
            }
            ministereRepository.deleteById(id);
            return new ResponseEntity<>("{\"message\":\"Ministere deleted successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}