package com.centre.service.serviceImpl;

import com.centre.service.model.Role;
import com.centre.service.model.Produit;
import com.centre.service.model.Requete;
import com.centre.service.model.Rdv;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.MailSendException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.centre.service.JwtService.JwtService;
import com.centre.service.JwtService.UserInfoDetails;
import com.centre.service.filter.JwtAuthFilter;
import com.centre.service.model.AuthRequest;
import com.centre.service.model.UserInfo;
import com.centre.service.repository.UserInfoRepository;
import com.centre.service.repository.RoleRepository;
import com.centre.service.repository.ProduitRepository;
import com.centre.service.repository.RequeteRepository;
import com.centre.service.repository.RdvRepository;
import com.centre.service.service.UserInfoService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    private static final Logger log = LoggerFactory.getLogger(UserInfoServiceImpl.class);

    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    ProduitRepository produitRepository;

    @Autowired
    RequeteRepository requeteRepository;

    @Autowired
    RdvRepository rdvRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtAuthFilter jwtAuthFilter;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void changePassword(Long id, String newPassword) {
        log.info("Changing password for user ID: {}", id);
        UserInfo user = userInfoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new RuntimeException("Utilisateur non trouvé");
                });
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userInfoRepository.save(user);
        log.info("Password changed successfully for user ID: {}", id);
    }

    @Override
    public UserInfo getAppuserById(Long id) {
        log.info("Fetching user by ID: {}", id);
        return userInfoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new RuntimeException("Utilisateur non trouvé avec l'id : " + id);
                });
    }

    @Override
    public ResponseEntity<?> addNewAppuser(UserInfo userInfo) {
        log.info("Adding new user with email: {}", userInfo.getEmail());
        try {
            if (!ValidateUserInfo(userInfo)) {
                log.warn("Invalid user data provided");
                return new ResponseEntity<>("{\"message\":\"Missing required Data\"}", HttpStatus.BAD_REQUEST);
            }
            Optional<UserInfo> db = userInfoRepository.findByEmailAndArchiverFalse(userInfo.getEmail());
            if (db.isPresent()) {
                log.warn("Email already exists: {}", userInfo.getEmail());
                return new ResponseEntity<>("{\"message\":\"Email already exists\"}", HttpStatus.BAD_REQUEST);
            }
            if (userInfo.getRole() == null || userInfo.getRole().getId() == null) {
                log.warn("Role not provided");
                return new ResponseEntity<>("{\"message\":\"Role must be provided\"}", HttpStatus.BAD_REQUEST);
            }
            Optional<Role> roleOpt = roleRepository.findById(userInfo.getRole().getId());
            if (roleOpt.isEmpty()) {
                log.warn("Role not found with ID: {}", userInfo.getRole().getId());
                return new ResponseEntity<>("{\"message\":\"Role not found\"}", HttpStatus.BAD_REQUEST);
            }
            userInfo.setRole(roleOpt.get());
            boolean isClient = "CLIENT".equalsIgnoreCase(roleOpt.get().getName());
            if (isClient) {
                if (userInfo.getProduit() == null || userInfo.getProduit().getId() == null) {
                    log.warn("Produit not provided for CLIENT role");
                    return new ResponseEntity<>("{\"message\":\"Produit must be provided for CLIENT role\"}",
                            HttpStatus.BAD_REQUEST);
                }
                Optional<Produit> produitOpt = produitRepository.findById(userInfo.getProduit().getId());
                if (produitOpt.isEmpty()) {
                    log.warn("Produit not found with ID: {}", userInfo.getProduit().getId());
                    return new ResponseEntity<>("{\"message\":\"Produit not found\"}", HttpStatus.BAD_REQUEST);
                }
                if ("Any".equalsIgnoreCase(produitOpt.get().getNom())) {
                    log.warn("Cannot assign 'Any' product to CLIENT role");
                    return new ResponseEntity<>("{\"message\":\"Cannot assign 'Any' product to CLIENT role\"}",
                            HttpStatus.BAD_REQUEST);
                }
                userInfo.setProduit(produitOpt.get());
            } else {
                Optional<Produit> anyProduitOpt = produitRepository.findByNomAndArchiverFalse("Any");
                if (anyProduitOpt.isEmpty()) {
                    log.error("Default 'Any' product not found");
                    return new ResponseEntity<>("{\"message\":\"Default 'Any' product not found\"}",
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }
                userInfo.setProduit(anyProduitOpt.get());
            }
            if (userInfo.getService() == null || userInfo.getService().getId() == null) {
                log.warn("Service not provided");
                return new ResponseEntity<>("{\"message\":\"Service must be provided\"}", HttpStatus.BAD_REQUEST);
            }
            userInfo.setPassword(encoder.encode(userInfo.getPassword()));
            userInfo.setStatus("false");
            userInfo.setEmail(userInfo.getEmail().toLowerCase());
            userInfo.setIsDeletable("true");
            userInfo.setArchiver(false);
            userInfoRepository.save(userInfo);
            log.info("User created successfully with email: {}", userInfo.getEmail());
            return new ResponseEntity<>("{\"message\":\"User created successfully\"}", HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error while adding new user: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean ValidateUserInfo(UserInfo userInfo) {
        return !Objects.isNull(userInfo) && StringUtils.hasText(userInfo.getName())
                && StringUtils.hasText(userInfo.getEmail()) && StringUtils.hasText(userInfo.getPassword())
                && userInfo.getRole() != null && userInfo.getService() != null;
    }

    @Override
    public ResponseEntity<?> login(AuthRequest authRequest) {
        log.info("Login attempt for email: {}", authRequest.getEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail().toLowerCase(),
                            authRequest.getPassword()));
            if (authentication != null && authentication.isAuthenticated()) {
                UserInfoDetails userDetails = (UserInfoDetails) authentication.getPrincipal();
                if ("true".equalsIgnoreCase(userDetails.getStatus())) {
                    String token = jwtService.generateToken(userDetails);
                    Map<String, Object> response = new HashMap<>();
                    response.put("token", token);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else {
                    log.warn("User {} is not approved", authRequest.getEmail());
                    return new ResponseEntity<>("{\"message\":\"Wait for admin approval\"}", HttpStatus.BAD_REQUEST);
                }
            } else {
                log.error("Invalid user request for email: {}", authRequest.getEmail());
                throw new UsernameNotFoundException("Invalid user request");
            }
        } catch (DisabledException ex) {
            log.warn("Account disabled for email: {}", authRequest.getEmail());
            return new ResponseEntity<>("{\"message\":\"Wait for admin approval\"}", HttpStatus.UNAUTHORIZED);
        } catch (BadCredentialsException ex) {
            log.warn("Invalid credentials for email: {}", authRequest.getEmail());
            return new ResponseEntity<>("{\"message\":\"Invalid Credentials\"}", HttpStatus.UNAUTHORIZED);
        } catch (UsernameNotFoundException ex) {
            log.error("User not found: {}", authRequest.getEmail());
            throw ex;
        } catch (Exception ex) {
            log.error("Error during login for email {}: {}", authRequest.getEmail(), ex.getMessage(), ex);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllAppuser() {
        log.info("Fetching all app users for email: {}", jwtAuthFilter.getEmail());
        try {
            return new ResponseEntity<>(userInfoRepository.getAllAppuser(jwtAuthFilter.getEmail()), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error while fetching all app users: {}", ex.getMessage(), ex);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllTechniciens() {
        log.info("Fetching all active technicians");
        try {
            List<UserInfo> techniciens = userInfoRepository.findActiveTechniciens();
            return new ResponseEntity<>(techniciens, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while fetching technicians: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateAppuser(Long id, UserInfo updatedUser) {
        log.info("Updating user with ID: {}", id);
        try {
            Optional<UserInfo> optionalUser = userInfoRepository.findByIdAndArchiverFalse(id);
            if (optionalUser.isEmpty()) {
                log.warn("User not found or archived with ID: {}", id);
                return new ResponseEntity<>("{\"message\":\"User not found or archived\"}", HttpStatus.NOT_FOUND);
            }
            if (updatedUser.getEmail() != null) {
                Optional<UserInfo> db = userInfoRepository.findByEmailAndArchiverFalse(updatedUser.getEmail());
                if (db.isPresent() && !db.get().getId().equals(id)) {
                    log.warn("Email already exists: {}", updatedUser.getEmail());
                    return new ResponseEntity<>("{\"message\":\"Email already exists\"}", HttpStatus.BAD_REQUEST);
                }
            }
            UserInfo user = optionalUser.get();
            boolean isUpdated = false;
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
            Role currentRole = user.getRole();
            if (updatedUser.getRole() != null && updatedUser.getRole().getId() != null) {
                Optional<Role> roleOpt = roleRepository.findById(updatedUser.getRole().getId());
                if (roleOpt.isEmpty()) {
                    log.warn("Role not found with ID: {}", updatedUser.getRole().getId());
                    return new ResponseEntity<>("{\"message\":\"Role not found\"}", HttpStatus.BAD_REQUEST);
                }
                user.setRole(roleOpt.get());
                currentRole = roleOpt.get();
                isUpdated = true;
            }
            boolean isClient = currentRole != null && "CLIENT".equalsIgnoreCase(currentRole.getName());
            if (updatedUser.getProduit() != null && updatedUser.getProduit().getId() != null) {
                Optional<Produit> produitOpt = produitRepository.findById(updatedUser.getProduit().getId());
                if (produitOpt.isEmpty()) {
                    log.warn("Produit not found with ID: {}", updatedUser.getProduit().getId());
                    return new ResponseEntity<>("{\"message\":\"Produit not found\"}", HttpStatus.BAD_REQUEST);
                }
                if (isClient && "Any".equalsIgnoreCase(produitOpt.get().getNom())) {
                    log.warn("Cannot assign 'Any' product to CLIENT role");
                    return new ResponseEntity<>("{\"message\":\"Cannot assign 'Any' product to CLIENT role\"}",
                            HttpStatus.BAD_REQUEST);
                }
                user.setProduit(produitOpt.get());
                isUpdated = true;
            } else if (isClient) {
                if (user.getProduit() == null || "Any".equalsIgnoreCase(user.getProduit().getNom())) {
                    log.warn("Produit must be provided for CLIENT role");
                    return new ResponseEntity<>("{\"message\":\"Produit must be provided for CLIENT role\"}",
                            HttpStatus.BAD_REQUEST);
                }
            } else {
                Optional<Produit> anyProduitOpt = produitRepository.findByNomAndArchiverFalse("Any");
                if (anyProduitOpt.isEmpty()) {
                    log.error("Default 'Any' product not found");
                    return new ResponseEntity<>("{\"message\":\"Default 'Any' product not found\"}",
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }
                user.setProduit(anyProduitOpt.get());
                isUpdated = true;
            }
            if (updatedUser.getService() != null && updatedUser.getService().getId() != null) {
                user.setService(updatedUser.getService());
                isUpdated = true;
            }
            if (!isUpdated) {
                log.warn("No valid fields provided for update for user ID: {}", id);
                return new ResponseEntity<>("{\"message\":\"No valid fields provided for update\"}",
                        HttpStatus.BAD_REQUEST);
            }
            user.setArchiver(false);
            userInfoRepository.save(user);
            log.info("User updated successfully with ID: {}", id);
            return new ResponseEntity<>("{\"message\":\"User updated successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error updating user with ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> archiveAppuser(Long id) {
        log.info("Archiving user with ID: {}", id);
        try {
            Optional<UserInfo> optionalUser = userInfoRepository.findByIdAndArchiverFalse(id);
            if (optionalUser.isEmpty()) {
                log.warn("User not found or already archived with ID: {}", id);
                return new ResponseEntity<>("{\"message\":\"User not found or already archived\"}",
                        HttpStatus.NOT_FOUND);
            }
            UserInfo user = optionalUser.get();
            if (!"true".equals(user.getIsDeletable())) {
                log.warn("User is not archivable with ID: {}", id);
                return new ResponseEntity<>("{\"message\":\"User is not archivable\"}", HttpStatus.BAD_REQUEST);
            }
            user.setArchiver(true);
            user.setStatus("false");
            userInfoRepository.save(user);
            // Archive related Requetes and Rdvs for CLIENT role
            if ("CLIENT".equalsIgnoreCase(user.getRole().getName())) {
                List<Requete> requetes = requeteRepository.findByClientIdAndArchiverFalse(id);
                for (Requete requete : requetes) {
                    requete.setArchiver(true);
                    requeteRepository.save(requete);
                }
                List<Rdv> rdvs = rdvRepository.findByClientIdAndArchiverFalse(id);
                for (Rdv rdv : rdvs) {
                    rdv.setArchiver(true);
                    rdvRepository.save(rdv);
                }
            }
            log.info("User and related entities archived successfully with ID: {}", id);
            return new ResponseEntity<>("{\"message\":\"User and related entities archived successfully\"}",
                    HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error archiving user with ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> checkToken() {
        log.info("Checking token validity");
        return new ResponseEntity<>("{\"message\":\"true\"}", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> toggleStatus(Long id) {
        log.info("Toggling status for user with ID: {}", id);
        try {
            Optional<UserInfo> optionalUser = userInfoRepository.findByIdAndArchiverFalse(id);
            if (optionalUser.isEmpty()) {
                log.warn("User not found or archived with ID: {}", id);
                return new ResponseEntity<>("{\"message\":\"User not found or archived\"}", HttpStatus.NOT_FOUND);
            }
            UserInfo user = optionalUser.get();
            user.setStatus("true".equals(user.getStatus()) ? "false" : "true");
            userInfoRepository.save(user);
            log.info("User status toggled successfully for ID: {}", id);
            return new ResponseEntity<>("{\"message\":\"User status toggled successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error toggling user status for ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllArchivedUsers() {
        log.info("Fetching all archived users");
        try {
            List<UserInfo> users = userInfoRepository.findByArchiverTrue();
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while retrieving archived users: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> unarchiveAppuser(Long id) {
        log.info("Unarchiving user with ID: {}", id);
        try {
            Optional<UserInfo> optionalUser = userInfoRepository.findById(id);
            if (optionalUser.isEmpty()) {
                log.warn("User not found with ID: {}", id);
                return new ResponseEntity<>("{\"message\":\"User not found\"}", HttpStatus.NOT_FOUND);
            }
            UserInfo user = optionalUser.get();
            if (!user.isArchiver()) {
                log.warn("User is not archived with ID: {}", id);
                return new ResponseEntity<>("{\"message\":\"User is not archived\"}", HttpStatus.BAD_REQUEST);
            }
            // Check if service and produit are not archived
            if (user.getService().isArchiver()) {
                log.warn("Cannot unarchive user with ID {}: Service is archived", id);
                return new ResponseEntity<>("{\"message\":\"Cannot unarchive user: Service is archived\"}",
                        HttpStatus.BAD_REQUEST);
            }
            if (user.getProduit().isArchiver()) {
                log.warn("Cannot unarchive user with ID {}: Produit is archived", id);
                return new ResponseEntity<>("{\"message\":\"Cannot unarchive user: Produit is archived\"}",
                        HttpStatus.BAD_REQUEST);
            }
            user.setArchiver(false);
            userInfoRepository.save(user);
            log.info("User unarchived successfully with ID: {}", id);
            return new ResponseEntity<>("{\"message\":\"User unarchived successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while unarchiving user with ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> forgotPassword(String email) {
        log.info("Processing forgot password request for email: {}", email);
        try {
            if (!StringUtils.hasText(email)) {
                log.warn("Email is empty or null");
                return new ResponseEntity<>("{\"message\":\"Email is required\"}", HttpStatus.BAD_REQUEST);
            }
            Optional<UserInfo> optionalUser = userInfoRepository.findByEmailAndArchiverFalse(email.toLowerCase());
            if (optionalUser.isEmpty()) {
                log.warn("User not found with email: {}", email);
                return new ResponseEntity<>("{\"message\":\"User not found\"}", HttpStatus.NOT_FOUND);
            }
            UserInfo user = optionalUser.get();
            // Generate a reset token
            String resetToken = UUID.randomUUID().toString();
            user.setResetToken(resetToken);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1)); // Token expires in 1 hour
            try {
                userInfoRepository.save(user);
                log.info("Reset token saved for user: {}", email);
            } catch (Exception e) {
                log.error("Failed to save reset token for user {}: {}", email, e.getMessage(), e);
                return new ResponseEntity<>("{\"message\":\"Failed to save reset token\"}",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            // Send HTML email with a styled button
            String resetLink = "http://localhost:4200/reset-password?token=" + resetToken;
            String userName = StringUtils.hasText(user.getName()) ? user.getName() : "Utilisateur";
            String htmlContent = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<meta charset='UTF-8'>" +
                    "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "</head>" +
                    "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; line-height: 1.6; color: #333333; background-color: #f4f4f4;'>"
                    +
                    "<table width='100%' cellpadding='0' cellspacing='0' style='max-width: 600px; margin: 20px auto; background-color: #ffffff; border: 1px solid #e0e0e0; border-radius: 8px;'>"
                    +
                    "<tr>" +
                    "<td style='padding: 20px; text-align: center; background-color: #1e3c72; border-top-left-radius: 8px; border-top-right-radius: 8px;'>"
                    +
                    "<h1 style='color: #ffffff; margin: 0; font-size: 24px;'>Réinitialisation de mot de passe</h1>" +
                    "</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='padding: 30px;'>" +
                    "<p style='font-size: 16px; margin: 0 0 20px;'>Bonjour " + userName + ",</p>" +
                    "<p style='font-size: 16px; margin: 0 0 20px;'>Vous avez demandé à réinitialiser votre mot de passe. Cliquez sur le bouton ci-dessous pour procéder :</p>"
                    +
                    "<table width='100%' cellpadding='0' cellspacing='0'>" +
                    "<tr>" +
                    "<td style='text-align: center; padding: 20px 0;'>" +
                    "<a href='" + resetLink +
                    "' style='display: inline-block; padding: 12px 30px; background: linear-gradient(to bottom, #1e3c72, #1e3c72); color: #ffffff; text-decoration: none; font-size: 16px; font-weight: bold; border-radius: 5px; box-shadow: 0 2px 5px rgba(0,0,0,0.2); transition: background 0.3s;'>"
                    +
                    "Réinitialiser le mot de passe</a>" +
                    "</td>" +
                    "</tr>" +
                    "</table>" +
                    "<p style='font-size: 14px; color: #666666; margin: 20px 0 0;'>Ce lien est valide pendant 1 heure.</p>"
                    +
                    "<p style='font-size: 14px; color: #666666; margin: 5px 0 0;'>Si vous n'avez pas initié cette demande, veuillez ignorer cet email.</p>"
                    +
                    "</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='padding: 20px; text-align: center; background-color: #f8f9fa; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px;'>"
                    +
                    "<p style='font-size: 12px; color: #666666; margin: 0;'>Cordialement,<br>L'équipe CNI</p>" +
                    "<p style='font-size: 12px; color: #666666; margin: 10px 0 0;'>Contactez-nous : <a href='mailto:support@cni.com' style='color: #1e3c72; text-decoration: none;'>support@cni.com</a></p>"
                    +
                    "</td>" +
                    "</tr>" +
                    "</table>" +
                    "</body>" +
                    "</html>";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject("Réinitialisation de votre mot de passe");
            helper.setText(htmlContent, true); // true indicates HTML content
            try {
                mailSender.send(message);
                log.info("Password reset email with styled button sent successfully to: {}", email);
            } catch (MailSendException e) {
                log.error("Failed to send password reset email to {}: {}", email, e.getMessage(), e);
                return new ResponseEntity<>("{\"message\":\"Failed to send password reset email\"}",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>("{\"message\":\"Password reset email sent successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Unexpected error in forgotPassword for email {}: {}", email, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Unexpected error occurred\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> resetPassword(String token, String newPassword) {
        log.info("Processing reset password request with token: {}", token);
        try {
            if (!StringUtils.hasText(token) || !StringUtils.hasText(newPassword)) {
                log.warn("Token or new password is empty");
                return new ResponseEntity<>("{\"message\":\"Token and new password are required\"}",
                        HttpStatus.BAD_REQUEST);
            }
            Optional<UserInfo> optionalUser = userInfoRepository.findByResetToken(token);
            if (optionalUser.isEmpty()) {
                log.warn("Invalid or expired token: {}", token);
                return new ResponseEntity<>("{\"message\":\"Invalid or expired token\"}", HttpStatus.BAD_REQUEST);
            }
            UserInfo user = optionalUser.get();
            if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                user.setResetToken(null);
                user.setResetTokenExpiry(null);
                userInfoRepository.save(user);
                log.warn("Token has expired for user: {}", user.getEmail());
                return new ResponseEntity<>("{\"message\":\"Token has expired\"}", HttpStatus.BAD_REQUEST);
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userInfoRepository.save(user);
            log.info("Password reset successfully for user: {}", user.getEmail());
            return new ResponseEntity<>("{\"message\":\"Password reset successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error in resetPassword: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}