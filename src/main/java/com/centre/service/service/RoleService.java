package com.centre.service.service;

import com.centre.service.model.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface RoleService {

    ResponseEntity<?> addRole(Role role);

    ResponseEntity<?> getAllRoles();

    ResponseEntity<?> updateRole(Long id, Role role);

    ResponseEntity<?> deleteRole(Long id);
}