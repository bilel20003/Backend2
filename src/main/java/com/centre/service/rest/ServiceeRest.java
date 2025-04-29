package com.centre.service.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.centre.service.model.Servicee;

@RequestMapping(path = "/api/services")
public interface ServiceeRest {

    @GetMapping(path = "/getAllServices")
    ResponseEntity<?> getAllServices();

    @PostMapping(path = "/addNewService")
    ResponseEntity<?> addNewService(@RequestBody(required = true) Servicee service);

    @PutMapping("/updateService/{id}")
    ResponseEntity<?> updateService(@PathVariable Long id, @RequestBody Servicee service);

    @PutMapping("/archiveService/{id}")
    ResponseEntity<?> archiveService(@PathVariable Long id);
}