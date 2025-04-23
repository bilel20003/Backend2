package com.centre.service.rest;

import com.centre.service.model.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "/api/roles")
public interface RoleRest {

    @PostMapping(path = "/add")
    ResponseEntity<?> addRole(@RequestBody(required = true) Role role);

    @GetMapping(path = "/getall")
    ResponseEntity<?> getAllRoles();

    @PutMapping("/update/{id}")
    ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Role role);

    @DeleteMapping("/delete/{id}")
    ResponseEntity<?> deleteRole(@PathVariable Long id);
}