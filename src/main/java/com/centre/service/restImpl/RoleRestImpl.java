package com.centre.service.restImpl;

import com.centre.service.model.Role;
import com.centre.service.rest.RoleRest;
import com.centre.service.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleRestImpl implements RoleRest {

    Logger log = LoggerFactory.getLogger(RoleRestImpl.class);

    @Autowired
    RoleService roleService;

    @Override
    public ResponseEntity<?> addRole(Role role) {
        try {
            return roleService.addRole(role);
        } catch (Exception ex) {
            log.error("Error in addRole: {}", ex);
        }
        return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getAllRoles() {
        try {
            return roleService.getAllRoles();
        } catch (Exception ex) {
            log.error("Error in getAllRoles: {}", ex);
        }
        return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> updateRole(Long id, Role role) {
        try {
            return roleService.updateRole(id, role);
        } catch (Exception ex) {
            log.error("Error in updateRole: {}", ex);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> deleteRole(Long id) {
        try {
            return roleService.deleteRole(id);
        } catch (Exception ex) {
            log.error("Error in deleteRole: {}", ex);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}