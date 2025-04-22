package com.centre.service.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.centre.service.model.Objet;

@RequestMapping(path = "/api/objets")
public interface ObjetRest {

    @PostMapping("/addobjet")
    ResponseEntity<?> createObjet(@RequestBody(required = true) Objet objet);

    @GetMapping("/getallobjets")
    ResponseEntity<?> getAllObjets();

    @GetMapping("/getobjet/{id}")
    ResponseEntity<?> getObjetById(@PathVariable Long id);

    @PutMapping("/updateobjet/{id}")
    ResponseEntity<?> updateObjet(@PathVariable Long id, @RequestBody Objet objet);

    @DeleteMapping("/deleteobjet/{id}")
    ResponseEntity<?> deleteObjet(@PathVariable Long id);
}