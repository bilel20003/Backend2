package com.centre.service.restImpl;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired; import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.RestController;

import com.centre.service.model.AuthRequest;
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
    public ResponseEntity<?> login(AuthRequest authRequest) {
        try{
            return userInfoService.login(authRequest);
        } 
        catch (Exception ex) {
            log.error("Error in login:{}",ex);
        }
        return new ResponseEntity<>("{\"message\":\"Somthing went wrong\"}",HttpStatus.INTERNAL_SERVER_ERROR);
    }



    @Override
    public ResponseEntity<?> getAllAppuser() {
        try{
            return userInfoService.getAllAppuser();
        } 
        catch (Exception ex) {
            log.error("Error in getAllAppuser:{}",ex);
        }
        return new ResponseEntity<>("{\"message\":\"Somthing went wrong\"}",HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> updateAppuser(Long id, UserInfo userInfo) {
        try {
            return userInfoService.updateAppuser(id, userInfo);
        } catch (Exception ex) {
            log.error("Error in updateAppuser: {}", ex);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
     }
    }

    @Override
    public ResponseEntity<?> deleteAppuser(Long id) {
     try {
          return userInfoService.deleteAppuser(id);
      } catch (Exception ex) {
          log.error("Error in deleteAppuser: {}", ex);
          return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }

   
}