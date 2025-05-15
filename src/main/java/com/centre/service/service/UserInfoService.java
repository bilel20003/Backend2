package com.centre.service.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.centre.service.model.AuthRequest;
import com.centre.service.model.UserInfo;

@Service
public interface UserInfoService {

    ResponseEntity<?> addNewAppuser(UserInfo userInfo);

    ResponseEntity<?> login(AuthRequest authRequest);

    ResponseEntity<?> getAllAppuser();

    ResponseEntity<?> getAllTechniciens();

    ResponseEntity<?> updateAppuser(Long id, UserInfo userInfo);

    ResponseEntity<?> archiveAppuser(Long id);

    ResponseEntity<?> checkToken();

    ResponseEntity<?> toggleStatus(Long id);

    void changePassword(Long id, String newPassword);

    UserInfo getAppuserById(Long id);

    ResponseEntity<?> getAllArchivedUsers();

    ResponseEntity<?> unarchiveAppuser(Long id);

    ResponseEntity<?> forgotPassword(String email);

    ResponseEntity<?> resetPassword(String token, String newPassword);

    ResponseEntity<?> sendWelcomeEmail(String email); // Nouvelle méthode pour l'email de bienvenue
}