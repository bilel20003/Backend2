package com.centre.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.centre.service.model.Servicee;
import com.centre.service.repository.ServiceeRepository;
import com.centre.service.service.ServiceeService;

import java.util.List;

@Service
public class ServiceeServiceImpl implements ServiceeService {

    @Autowired
    private ServiceeRepository serviceRepository;

    @Override
    public ResponseEntity<?> addNewService(Servicee service) {
        try {
            serviceRepository.save(service);
            return new ResponseEntity<>("{\"message\":\"Service created successfully\"}", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllServices() {
        try {
            List<Servicee> services = serviceRepository.findAllWithMinistere();
            return new ResponseEntity<>(services, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateService(Long id, Servicee service) {
        try {
            if (!serviceRepository.existsById(id)) {
                return new ResponseEntity<>("{\"message\":\"Service not found\"}", HttpStatus.NOT_FOUND);
            }
            service.setId(id);
            serviceRepository.save(service);
            return new ResponseEntity<>("{\"message\":\"Service updated successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> deleteService(Long id) {
        try {
            if (!serviceRepository.existsById(id)) {
                return new ResponseEntity<>("{\"message\":\"Service not found\"}", HttpStatus.NOT_FOUND);
            }
            serviceRepository.deleteById(id);
            return new ResponseEntity<>("{\"message\":\"Service deleted successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}