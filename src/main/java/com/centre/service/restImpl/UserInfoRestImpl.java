package com.centre.service.restImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.centre.service.model.AuthRequest;
import com.centre.service.model.UserInfo;
import com.centre.service.rest.UserInfoRest;
import com.centre.service.service.UserInfoService;
import java.util.Map;

@RestController
public class UserInfoRestImpl implements UserInfoRest {

    Logger log = LoggerFactory.getLogger(UserInfoRestImpl.class);

    @Autowired
    UserInfoService userInfoService;

    @Override
    public ResponseEntity<UserInfo> getAppuserById(@PathVariable Long id) {
        return ResponseEntity.ok(userInfoService.getAppuserById(id));
    }

    @Override
    public ResponseEntity<?> addNewAppuser(UserInfo userInfo) {
        try {
            return userInfoService.addNewAppuser(userInfo);
        } catch (Exception ex) {
            log.error("Error in addNewAppuser: {}", ex);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> changePassword(Long id, Map<String, String> body) {
        String newPassword = body.get("password");
        userInfoService.changePassword(id, newPassword);
        return ResponseEntity.ok(Map.of("message", "Mot de passe changé avec succès"));
    }

    @Override
    public ResponseEntity<?> login(AuthRequest authRequest) {
        try {
            return userInfoService.login(authRequest);
        } catch (Exception ex) {
            log.error("Error in login: {}", ex);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllAppuser() {
        try {
            return userInfoService.getAllAppuser();
        } catch (Exception ex) {
            log.error("Error in getAllAppuser: {}", ex);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllTechniciens() {
        try {
            return userInfoService.getAllTechniciens();
        } catch (Exception ex) {
            log.error("Error in getAllTechniciens: {}", ex);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
    public ResponseEntity<?> archiveAppuser(Long id) {
        try {
            return userInfoService.archiveAppuser(id);
        } catch (Exception ex) {
            log.error("Error in archiveAppuser: {}", ex);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> checkToken() {
        return userInfoService.checkToken();
    }

    @Override
    public ResponseEntity<?> toggleStatus(Long id) {
        try {
            return userInfoService.toggleStatus(id);
        } catch (Exception ex) {
            log.error("Error in toggleStatus: {}", ex);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}