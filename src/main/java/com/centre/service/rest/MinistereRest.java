package com.centre.service.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.centre.service.model.Ministere;

@RequestMapping(path = "/api/ministeres")
public interface MinistereRest {

    @PostMapping(path = "/addNewMinistere")
    ResponseEntity<?> addNewMinistere(@RequestBody(required = true) Ministere ministere);

    @GetMapping(path = "/getAllMinisteres")
    ResponseEntity<?> getAllMinisteres();

    @PutMapping("/updateMinistere/{id}")
    ResponseEntity<?> updateMinistere(@PathVariable Long id, @RequestBody Ministere ministere);

    @PutMapping("/archiveMinistere/{id}")
    ResponseEntity<?> archiveMinistere(@PathVariable Long id);
}