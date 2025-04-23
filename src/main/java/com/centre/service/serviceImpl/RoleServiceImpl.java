package com.centre.service.serviceImpl;

import com.centre.service.model.Role;
import com.centre.service.repository.RoleRepository;
import com.centre.service.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Autowired
    RoleRepository roleRepository;

    @Override
    public ResponseEntity<?> addRole(Role role) {
        try {
            if (!validateRole(role)) {
                return new ResponseEntity<>("{\"message\":\"Missing required data\"}", HttpStatus.BAD_REQUEST);
            }

            if (roleRepository.existsByName(role.getName())) {
                return new ResponseEntity<>("{\"message\":\"Role name already exists\"}", HttpStatus.BAD_REQUEST);
            }

            roleRepository.save(role);
            return new ResponseEntity<>("{\"message\":\"Role created successfully\"}", HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error while adding role: {}", e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllRoles() {
        try {
            List<Role> roles = roleRepository.findAll();
            return new ResponseEntity<>(roles, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while getting all roles: {}", e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateRole(Long id, Role updatedRole) {
        try {
            Optional<Role> optionalRole = roleRepository.findById(id);
            if (optionalRole.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Role not found\"}", HttpStatus.NOT_FOUND);
            }

            if (updatedRole.getName() != null && roleRepository.existsByName(updatedRole.getName())) {
                Optional<Role> existingRole = roleRepository.findByName(updatedRole.getName());
                if (existingRole.isPresent() && !existingRole.get().getId().equals(id)) {
                    return new ResponseEntity<>("{\"message\":\"Role name already exists\"}", HttpStatus.BAD_REQUEST);
                }
            }

            Role role = optionalRole.get();
            boolean isUpdated = false;

            if (StringUtils.hasText(updatedRole.getName())) {
                role.setName(updatedRole.getName());
                isUpdated = true;
            }

            if (!isUpdated) {
                return new ResponseEntity<>("{\"message\":\"No valid fields provided for update\"}",
                        HttpStatus.BAD_REQUEST);
            }

            roleRepository.save(role);
            return new ResponseEntity<>("{\"message\":\"Role updated successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error updating role: {}", e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> deleteRole(Long id) {
        try {
            Optional<Role> optionalRole = roleRepository.findById(id);
            if (optionalRole.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Role not found\"}", HttpStatus.NOT_FOUND);
            }

            roleRepository.deleteById(id);
            return new ResponseEntity<>("{\"message\":\"Role deleted successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error deleting role: {}", e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean validateRole(Role role) {
        return role != null && StringUtils.hasText(role.getName());
    }
}