package com.centre.service.restImpl;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired; import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.RestController;

import com.centre.service.model.UserInfo; 
import com.centre.service.rest.UserInfoRest; 
import com.centre.service.service.UserInfoService;

@RestController 
public class UserInfoRestImpl implements UserInfoRest {

    Logger log = LoggerFactory.getLogger(UserInfoRestImpl.class); 

    @Autowired
    UserInfoService userInfoService;



    @Override
    public ResponseEntity<?> addNewAppuser(UserInfo userInfo) {
        try{
            return userInfoService.addNewAppuser(userInfo);
        } 
        catch (Exception ex) {
            log.error("Error in addNewAppuser:{}",ex);
        }
        return new ResponseEntity<>("{\"message\":\"Somthing went wrong\"}",HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getAllPersonnes() {
        // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'getAllPersonnes'");
    }

    @Override
    public ResponseEntity<?> getPersonneById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPersonneById'");
    }

    @Override
    public ResponseEntity<String> deletePersonne(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deletePersonne'");
    }



    @Override
    public ResponseEntity<?> updatePersonne(Long id, UserInfo updatedPersonne) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updatePersonne'");
    }

    @Override
    public ResponseEntity<?> getAllTechniciens() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllTechniciens'");
    }

    @Override
    public ResponseEntity<?> assignTechnician(Long id, Long technicianId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'assignTechnician'");
    }
}