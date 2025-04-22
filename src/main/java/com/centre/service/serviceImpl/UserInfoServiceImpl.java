package com.centre.service.serviceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.centre.service.JwtService.JwtService;
import com.centre.service.JwtService.UserInfoDetails;
import com.centre.service.filter.JwtAuthFilter;
import com.centre.service.model.AuthRequest;
import com.centre.service.model.Role;
import com.centre.service.model.UserInfo;
import com.centre.service.repository.UserInfoRepository;
import com.centre.service.service.UserInfoService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    Logger log = LoggerFactory.getLogger(UserInfoServiceImpl.class);

    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtAuthFilter jwtAuthFilter;

    @Override
    public ResponseEntity<?> addNewAppuser(UserInfo userInfo) {
        try {
            // Vérifiez si les informations de l'utilisateur sont valides
            if (!ValidateUserInfo(userInfo)) {
                return new ResponseEntity<>("{\"message\":\"Missing required Data\"}", HttpStatus.BAD_REQUEST);
            }

            // Vérifiez si l'email existe déjà
            Optional<UserInfo> db = userInfoRepository.findByEmail(userInfo.getEmail());
            if (db.isPresent()) {
                return new ResponseEntity<>("{\"message\":\"Email already exists\"}", HttpStatus.BAD_REQUEST);
            }

            // Encodez le mot de passe et préparez l'utilisateur pour l'enregistrement
            userInfo.setPassword(encoder.encode(userInfo.getPassword()));
            userInfo.setStatus("false");
            userInfo.setEmail(userInfo.getEmail().toLowerCase());
            userInfo.setIsDeletable("true");

            // Assurez-vous que le service est correctement défini
            if (userInfo.getService() == null) {
                return new ResponseEntity<>("{\"message\":\"Service must be provided\"}", HttpStatus.BAD_REQUEST);
            }

            // Enregistrez l'utilisateur dans la base de données
            userInfoRepository.save(userInfo);
            return new ResponseEntity<>("{\"message\":\"User  created successfully\"}", HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error while adding new user: {}", e);
        }
        return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean ValidateUserInfo(UserInfo userInfo) {
        return !Objects.isNull(userInfo) && StringUtils.hasText(userInfo.getName())
                && StringUtils.hasText(userInfo.getEmail()) && StringUtils.hasText(userInfo.getPassword());

    }

    @Override
    public ResponseEntity<?> login(AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail().toLowerCase(),
                            authRequest.getPassword()));

            if (authentication != null && authentication.isAuthenticated()) {
                UserInfoDetails userDetails = (UserInfoDetails) authentication.getPrincipal();

                // Vérifiez le statut de l'utilisateur
                if ("true".equalsIgnoreCase(userDetails.getStatus())) {
                    String token = jwtService.generateToken(userDetails);

                    // Créer une réponse JSON avec le token et le rôle
                    Map<String, Object> response = new HashMap<>();
                    response.put("token", token);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(
                            "{\"message\":\"Wait for admin approval\"}",
                            HttpStatus.BAD_REQUEST);
                }
            } else {
                throw new UsernameNotFoundException("Invalid user request");
            }
        } catch (DisabledException ex) {
            return new ResponseEntity<>(
                    "{\"message\":\"Wait for admin approval\"}",
                    HttpStatus.UNAUTHORIZED);
        } catch (BadCredentialsException ex) {
            return new ResponseEntity<>(
                    "{\"message\":\"Invalid Credentials\"}",
                    HttpStatus.UNAUTHORIZED);
        } catch (UsernameNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error while login: {}", ex);
            return new ResponseEntity<>(
                    "{\"message\":\"Something went wrong\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllAppuser() {
        try {
            return new ResponseEntity<>(userInfoRepository.getAllAppuser(jwtAuthFilter.getEmail()), HttpStatus.OK);

        } catch (Exception ex) {
            log.error("Error while getAllAppuser: {}", ex.getMessage());
        }
        return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getAllTechniciens() {
        try {
            List<UserInfo> techniciens = userInfoRepository.findActiveTechniciens();
            return new ResponseEntity<>(techniciens, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while getting all techniciens: {}", e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateAppuser(Long id, UserInfo updatedUser) {
        try {
            // Vérifiez si l'utilisateur existe
            Optional<UserInfo> optionalUser = userInfoRepository.findById(id);
            if (optionalUser.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"User  not found\"}", HttpStatus.NOT_FOUND);
            }

            // Vérifiez si l'email envoyé existe déjà
            if (updatedUser.getEmail() != null) {
                Optional<UserInfo> db = userInfoRepository.findByEmail(updatedUser.getEmail());
                if (db.isPresent() && !db.get().getId().equals(id)) { // Vérifiez que l'email n'appartient pas à un
                                                                      // autre utilisateur
                    return new ResponseEntity<>("{\"message\":\"Email already exists\"}", HttpStatus.BAD_REQUEST);
                }
            }

            // Récupérez l'utilisateur actuel
            UserInfo user = optionalUser.get();
            boolean isUpdated = false;

            // Mettez à jour les champs si présents
            if (updatedUser.getName() != null && !updatedUser.getName().trim().isEmpty()) {
                user.setName(updatedUser.getName());
                isUpdated = true;
            }

            if (updatedUser.getEmail() != null && !updatedUser.getEmail().trim().isEmpty()) {
                user.setEmail(updatedUser.getEmail().toLowerCase());
                isUpdated = true;
            }

            if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
                user.setPassword(encoder.encode(updatedUser.getPassword()));
                isUpdated = true;
            }

            if (updatedUser.getStatus() != null) {
                user.setStatus(updatedUser.getStatus());
                isUpdated = true;
            }

            if (updatedUser.getRole() != null) {
                user.setRole(updatedUser.getRole());
                isUpdated = true;
            }

            if (updatedUser.getService() != null) {
                user.setService(updatedUser.getService()); // Mettez à jour le service
                isUpdated = true;
            }

            // Si aucune donnée n'a été mise à jour, renvoie un message d'erreur
            if (!isUpdated) {
                return new ResponseEntity<>("{\"message\":\"No valid fields provided for update\"}",
                        HttpStatus.BAD_REQUEST);
            }

            // Sauvegarde l'utilisateur mis à jour
            userInfoRepository.save(user);
            return new ResponseEntity<>("{\"message\":\"User  updated successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error updating user: {}", e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> deleteAppuser(Long id) {
        try {
            Optional<UserInfo> optionalUser = userInfoRepository.findById(id);
            if (optionalUser.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"User not found\"}", HttpStatus.NOT_FOUND);
            }

            UserInfo user = optionalUser.get();
            if (!"true".equals(user.getIsDeletable())) {
                return new ResponseEntity<>("{\"message\":\"User is not deletable\"}", HttpStatus.BAD_REQUEST);
            }

            userInfoRepository.deleteById(id);
            return new ResponseEntity<>("{\"message\":\"User deleted successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> checkToken() {
        return new ResponseEntity<>("{\"message\":\"true\"}", HttpStatus.OK);
    }

}
