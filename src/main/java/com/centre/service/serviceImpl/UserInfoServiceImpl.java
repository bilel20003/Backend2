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
        log.info("Changement de mot de passe pour l'utilisateur avec l'ID : {}", id);
        UserInfo user = userInfoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé avec l'ID : {}", id);
                    return new RuntimeException("Utilisateur non trouvé avec l'ID : " + id);
                });
        if (newPassword == null || newPassword.trim().isEmpty()) {
            log.error("Le nouveau mot de passe est vide pour l'utilisateur avec l'ID : {}", id);
            throw new RuntimeException("Le nouveau mot de passe ne peut pas être vide.");
        }
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userInfoRepository.save(user);
        log.info("Mot de passe changé avec succès pour l'utilisateur avec l'ID : {}", id);
    }

    @Override
    public UserInfo getAppuserById(Long id) {
        log.info("Récupération de l'utilisateur avec l'ID : {}", id);
        return userInfoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé avec l'ID : {}", id);
                    return new RuntimeException("Utilisateur non trouvé avec l'ID : " + id);
                });
    }

    @Override
    public ResponseEntity<?> addNewAppuser(UserInfo userInfo) {
        log.info("Ajout d'un nouvel utilisateur avec l'email : {}", userInfo.getEmail());
        try {
            if (!ValidateUserInfo(userInfo)) {
                log.warn("Données utilisateur invalides fournies");
                return new ResponseEntity<>("{\"message\":\"Données requises manquantes\"}", HttpStatus.BAD_REQUEST);
            }
            Optional<UserInfo> db = userInfoRepository.findByEmailAndArchiverFalse(userInfo.getEmail());
            if (db.isPresent()) {
                log.warn("L'email existe déjà : {}", userInfo.getEmail());
                return new ResponseEntity<>("{\"message\":\"Cet email existe déjà\"}", HttpStatus.BAD_REQUEST);
            }
            if (userInfo.getRole() == null || userInfo.getRole().getId() == null) {
                log.warn("Rôle non fourni");
                return new ResponseEntity<>("{\"message\":\"Le rôle doit être fourni\"}", HttpStatus.BAD_REQUEST);
            }
            Optional<Role> roleOpt = roleRepository.findById(userInfo.getRole().getId());
            if (roleOpt.isEmpty()) {
                log.warn("Rôle non trouvé avec l'ID : {}", userInfo.getRole().getId());
                return new ResponseEntity<>("{\"message\":\"Rôle non trouvé\"}", HttpStatus.BAD_REQUEST);
            }
            userInfo.setRole(roleOpt.get());
            boolean isClient = "CLIENT".equalsIgnoreCase(roleOpt.get().getName());
            if (isClient) {
                if (userInfo.getProduit() == null || userInfo.getProduit().getId() == null) {
                    log.warn("Produit non fourni pour le rôle CLIENT");
                    return new ResponseEntity<>("{\"message\":\"Le produit doit être fourni pour le rôle CLIENT\"}",
                            HttpStatus.BAD_REQUEST);
                }
                Optional<Produit> produitOpt = produitRepository.findById(userInfo.getProduit().getId());
                if (produitOpt.isEmpty()) {
                    log.warn("Produit non trouvé avec l'ID : {}", userInfo.getProduit().getId());
                    return new ResponseEntity<>("{\"message\":\"Produit non trouvé\"}", HttpStatus.BAD_REQUEST);
                }
                if ("Any".equalsIgnoreCase(produitOpt.get().getNom())) {
                    log.warn("Impossible d'attribuer le produit 'Any' au rôle CLIENT");
                    return new ResponseEntity<>(
                            "{\"message\":\"Impossible d'attribuer le produit 'Any' au rôle CLIENT\"}",
                            HttpStatus.BAD_REQUEST);
                }
                userInfo.setProduit(produitOpt.get());
            } else {
                Optional<Produit> anyProduitOpt = produitRepository.findByNomAndArchiverFalse("Any");
                if (anyProduitOpt.isEmpty()) {
                    log.error("Produit par défaut 'Any' non trouvé");
                    return new ResponseEntity<>("{\"message\":\"Produit par défaut 'Any' non trouvé\"}",
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }
                userInfo.setProduit(anyProduitOpt.get());
            }
            if (userInfo.getService() == null || userInfo.getService().getId() == null) {
                log.warn("Service non fourni");
                return new ResponseEntity<>("{\"message\":\"Le service doit être fourni\"}", HttpStatus.BAD_REQUEST);
            }
            userInfo.setPassword(encoder.encode(userInfo.getPassword()));
            userInfo.setStatus("false");
            userInfo.setEmail(userInfo.getEmail().toLowerCase());
            userInfo.setIsDeletable("true");
            userInfo.setArchiver(false);
            userInfoRepository.save(userInfo);
            log.info("Utilisateur créé avec succès avec l'email : {}", userInfo.getEmail());
            return new ResponseEntity<>("{\"message\":\"Utilisateur créé avec succès\"}", HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout d'un nouvel utilisateur : {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Une erreur s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean ValidateUserInfo(UserInfo userInfo) {
        return !Objects.isNull(userInfo) && StringUtils.hasText(userInfo.getName())
                && StringUtils.hasText(userInfo.getEmail()) && StringUtils.hasText(userInfo.getPassword())
                && userInfo.getRole() != null && userInfo.getService() != null;
    }

    @Override
    public ResponseEntity<?> login(AuthRequest authRequest) {
        log.info("Tentative de connexion pour l'email : {}", authRequest.getEmail());
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
                    log.warn("L'utilisateur {} n'est pas approuvé", authRequest.getEmail());
                    return new ResponseEntity<>("{\"message\":\"En attente de l'approbation de l'administrateur\"}",
                            HttpStatus.BAD_REQUEST);
                }
            } else {
                log.error("Demande d'utilisateur invalide pour l'email : {}", authRequest.getEmail());
                throw new UsernameNotFoundException("Demande d'utilisateur invalide");
            }
        } catch (DisabledException ex) {
            log.warn("Compte désactivé pour l'email : {}", authRequest.getEmail());
            return new ResponseEntity<>("{\"message\":\"En attente de l'acceptation de l'administrateur\"}",
                    HttpStatus.UNAUTHORIZED);
        } catch (BadCredentialsException ex) {
            log.warn("Identifiants invalides pour l'email : {}", authRequest.getEmail());
            return new ResponseEntity<>("{\"message\":\"Identifiants invalides\"}", HttpStatus.UNAUTHORIZED);
        } catch (UsernameNotFoundException ex) {
            log.error("Utilisateur non trouvé : {}", authRequest.getEmail());
            return new ResponseEntity<>("{\"message\":\"Utilisateur non trouvé\"}", HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            log.error("Erreur lors de la connexion pour l'email {} : {}", authRequest.getEmail(), ex.getMessage(), ex);
            return new ResponseEntity<>("{\"message\":\"Une erreur s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllAppuser() {
        log.info("Récupération de tous les utilisateurs pour l'email : {}", jwtAuthFilter.getEmail());
        try {
            return new ResponseEntity<>(userInfoRepository.getAllAppuser(jwtAuthFilter.getEmail()), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Erreur lors de la récupération de tous les utilisateurs : {}", ex.getMessage(), ex);
            return new ResponseEntity<>("{\"message\":\"Une erreur s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllTechniciens() {
        log.info("Récupération de tous les techniciens actifs");
        try {
            List<UserInfo> techniciens = userInfoRepository.findActiveTechniciens();
            if (techniciens.isEmpty()) {
                log.info("Aucun technicien actif trouvé");
                return new ResponseEntity<>("{\"message\":\"Aucun technicien actif trouvé\"}", HttpStatus.OK);
            }
            return new ResponseEntity<>(techniciens, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des techniciens : {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Une erreur s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateAppuser(Long id, UserInfo updatedUser) {
        log.info("Mise à jour de l'utilisateur avec l'ID : {}", id);
        try {
            Optional<UserInfo> optionalUser = userInfoRepository.findByIdAndArchiverFalse(id);
            if (optionalUser.isEmpty()) {
                log.warn("Utilisateur non trouvé ou archivé avec l'ID : {}", id);
                return new ResponseEntity<>("{\"message\":\"Utilisateur non trouvé ou déjà archivé\"}",
                        HttpStatus.NOT_FOUND);
            }
            if (updatedUser == null) {
                log.warn("Aucune donnée utilisateur fournie pour la mise à jour de l'ID : {}", id);
                return new ResponseEntity<>("{\"message\":\"Aucune donnée utilisateur fournie pour la mise à jour\"}",
                        HttpStatus.BAD_REQUEST);
            }
            if (updatedUser.getEmail() != null) {
                Optional<UserInfo> db = userInfoRepository.findByEmailAndArchiverFalse(updatedUser.getEmail());
                if (db.isPresent() && !db.get().getId().equals(id)) {
                    log.warn("L'email existe déjà : {}", updatedUser.getEmail());
                    return new ResponseEntity<>("{\"message\":\"Cet email existe déjà\"}", HttpStatus.BAD_REQUEST);
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
                    log.warn("Rôle non trouvé avec l'ID : {}", updatedUser.getRole().getId());
                    return new ResponseEntity<>("{\"message\":\"Rôle non trouvé\"}", HttpStatus.BAD_REQUEST);
                }
                user.setRole(roleOpt.get());
                currentRole = roleOpt.get();
                isUpdated = true;
            }
            boolean isClient = currentRole != null && "CLIENT".equalsIgnoreCase(currentRole.getName());
            if (updatedUser.getProduit() != null && updatedUser.getProduit().getId() != null) {
                Optional<Produit> produitOpt = produitRepository.findById(updatedUser.getProduit().getId());
                if (produitOpt.isEmpty()) {
                    log.warn("Produit non trouvé avec l'ID : {}", updatedUser.getProduit().getId());
                    return new ResponseEntity<>("{\"message\":\"Produit non trouvé\"}", HttpStatus.BAD_REQUEST);
                }
                if (isClient && "Any".equalsIgnoreCase(produitOpt.get().getNom())) {
                    log.warn("Impossible d'attribuer le produit 'Any' au rôle CLIENT");
                    return new ResponseEntity<>(
                            "{\"message\":\"Impossible d'attribuer le produit 'Any' au rôle CLIENT\"}",
                            HttpStatus.BAD_REQUEST);
                }
                user.setProduit(produitOpt.get());
                isUpdated = true;
            } else if (isClient) {
                if (user.getProduit() == null || "Any".equalsIgnoreCase(user.getProduit().getNom())) {
                    log.warn("Le produit doit être fourni pour le rôle CLIENT");
                    return new ResponseEntity<>("{\"message\":\"Le produit doit être fourni pour le rôle CLIENT\"}",
                            HttpStatus.BAD_REQUEST);
                }
            } else {
                Optional<Produit> anyProduitOpt = produitRepository.findByNomAndArchiverFalse("Any");
                if (anyProduitOpt.isEmpty()) {
                    log.error("Produit par défaut 'Any' non trouvé");
                    return new ResponseEntity<>("{\"message\":\"Produit par défaut 'Any' non trouvé\"}",
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
                log.warn("Aucun champ valide fourni pour la mise à jour de l'utilisateur avec l'ID : {}", id);
                return new ResponseEntity<>("{\"message\":\"Aucun champ valide fourni pour la mise à jour\"}",
                        HttpStatus.BAD_REQUEST);
            }
            user.setArchiver(false);
            userInfoRepository.save(user);
            log.info("Utilisateur mis à jour avec succès avec l'ID : {}", id);
            return new ResponseEntity<>("{\"message\":\"Utilisateur mis à jour avec succès\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de l'utilisateur avec l'ID {} : {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Une erreur s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> archiveAppuser(Long id) {
        log.info("Archivage de l'utilisateur avec l'ID : {}", id);
        try {
            Optional<UserInfo> optionalUser = userInfoRepository.findByIdAndArchiverFalse(id);
            if (optionalUser.isEmpty()) {
                log.warn("Utilisateur non trouvé ou déjà archivé avec l'ID : {}", id);
                return new ResponseEntity<>("{\"message\":\"Utilisateur non trouvé ou déjà archivé\"}",
                        HttpStatus.NOT_FOUND);
            }
            UserInfo user = optionalUser.get();
            if (!"true".equals(user.getIsDeletable())) {
                log.warn("L'utilisateur n'est pas archivable avec l'ID : {}", id);
                return new ResponseEntity<>("{\"message\":\"Cet utilisateur ne peut pas être archivé\"}",
                        HttpStatus.BAD_REQUEST);
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
            log.info("Utilisateur et entités associées archivés avec succès avec l'ID : {}", id);
            return new ResponseEntity<>("{\"message\":\"Utilisateur et entités associées archivés avec succès\"}",
                    HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de l'archivage de l'utilisateur avec l'ID {} : {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Une erreur s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> checkToken() {
        log.info("Vérification de la validité du jeton");
        return new ResponseEntity<>("{\"message\":\"Jeton valide\"}", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> toggleStatus(Long id) {
        log.info("Changement de statut pour l'utilisateur avec l'ID : {}", id);
        try {
            Optional<UserInfo> optionalUser = userInfoRepository.findByIdAndArchiverFalse(id);
            if (optionalUser.isEmpty()) {
                log.warn("Utilisateur non trouvé ou archivé avec l'ID : {}", id);
                return new ResponseEntity<>("{\"message\":\"Utilisateur non trouvé ou déjà archivé\"}",
                        HttpStatus.NOT_FOUND);
            }
            UserInfo user = optionalUser.get();
            user.setStatus("true".equals(user.getStatus()) ? "false" : "true");
            userInfoRepository.save(user);
            log.info("Statut de l'utilisateur changé avec succès pour l'ID : {}", id);
            return new ResponseEntity<>("{\"message\":\"Statut de l'utilisateur changé avec succès\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors du changement de statut de l'utilisateur pour l'ID {} : {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Une erreur s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllArchivedUsers() {
        log.info("Récupération de tous les utilisateurs archivés");
        try {
            List<UserInfo> users = userInfoRepository.findByArchiverTrue();
            if (users.isEmpty()) {
                log.info("Aucun utilisateur archivé trouvé");
                return new ResponseEntity<>("{\"message\":\"Aucun utilisateur archivé trouvé\"}", HttpStatus.OK);
            }
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs archivés : {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Une erreur s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> unarchiveAppuser(Long id) {
        log.info("Désarchivage de l'utilisateur avec l'ID : {}", id);
        try {
            Optional<UserInfo> optionalUser = userInfoRepository.findById(id);
            if (optionalUser.isEmpty()) {
                log.warn("Utilisateur non trouvé avec l'ID : {}", id);
                return new ResponseEntity<>("{\"message\":\"Utilisateur non trouvé\"}", HttpStatus.NOT_FOUND);
            }
            UserInfo user = optionalUser.get();
            if (!user.isArchiver()) {
                log.warn("L'utilisateur n'est pas archivé avec l'ID : {}", id);
                return new ResponseEntity<>("{\"message\":\"Cet utilisateur n'est pas archivé\"}",
                        HttpStatus.BAD_REQUEST);
            }
            // Check if service and produit are not archived
            if (user.getService().isArchiver()) {
                log.warn("Impossible de désarchiver l'utilisateur avec l'ID {} : le service est archivé", id);
                return new ResponseEntity<>(
                        "{\"message\":\"Impossible de désarchiver l'utilisateur : le service est archivé\"}",
                        HttpStatus.BAD_REQUEST);
            }
            if (user.getProduit().isArchiver()) {
                log.warn("Impossible de désarchiver l'utilisateur avec l'ID {} : le produit est archivé", id);
                return new ResponseEntity<>(
                        "{\"message\":\"Impossible de désarchiver l'utilisateur : le produit est archivé\"}",
                        HttpStatus.BAD_REQUEST);
            }
            user.setArchiver(false);
            userInfoRepository.save(user);
            log.info("Utilisateur désarchivé avec succès avec l'ID : {}", id);
            return new ResponseEntity<>("{\"message\":\"Utilisateur désarchivé avec succès\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors du désarchivage de l'utilisateur avec l'ID {} : {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Une erreur s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> forgotPassword(String email) {
        log.info("Traitement de la demande de réinitialisation de mot de passe pour l'email : {}", email);
        try {
            if (!StringUtils.hasText(email)) {
                log.warn("L'email est vide ou null");
                return new ResponseEntity<>("{\"message\":\"L'email est requis\"}", HttpStatus.BAD_REQUEST);
            }
            Optional<UserInfo> optionalUser = userInfoRepository.findByEmailAndArchiverFalse(email.toLowerCase());
            if (optionalUser.isEmpty()) {
                log.warn("Utilisateur non trouvé avec l'email : {}", email);
                return new ResponseEntity<>("{\"message\":\"Utilisateur non trouvé\"}", HttpStatus.NOT_FOUND);
            }
            UserInfo user = optionalUser.get();
            // Generate a reset token
            String resetToken = UUID.randomUUID().toString();
            user.setResetToken(resetToken);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1)); // Token expires in 1 hour
            try {
                userInfoRepository.save(user);
                log.info("Jeton de réinitialisation enregistré pour l'utilisateur : {}", email);
            } catch (Exception e) {
                log.error("Échec de l'enregistrement du jeton de réinitialisation pour l'utilisateur {} : {}", email,
                        e.getMessage(), e);
                return new ResponseEntity<>("{\"message\":\"Échec de l'enregistrement du jeton de réinitialisation\"}",
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
                log.info("Email de réinitialisation de mot de passe envoyé avec succès à : {}", email);
            } catch (MailSendException e) {
                log.error("Échec de l'envoi de l'email de réinitialisation de mot de passe à {} : {}", email,
                        e.getMessage(), e);
                return new ResponseEntity<>(
                        "{\"message\":\"Échec de l'envoi de l'email de réinitialisation de mot de passe\"}",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(
                    "{\"message\":\"Email de réinitialisation de mot de passe envoyé avec succès\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur inattendue dans forgotPassword pour l'email {} : {}", email, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Une erreur inattendue s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> sendWelcomeEmail(String email) {
        log.info("Traitement de l'envoi de l'email de bienvenue pour l'email : {}", email);
        try {
            if (!StringUtils.hasText(email)) {
                log.warn("L'email est vide ou null");
                return new ResponseEntity<>("{\"message\":\"L'email est requis\"}", HttpStatus.BAD_REQUEST);
            }
            Optional<UserInfo> optionalUser = userInfoRepository.findByEmailAndArchiverFalse(email.toLowerCase());
            if (optionalUser.isEmpty()) {
                log.warn("Utilisateur non trouvé avec l'email : {}", email);
                return new ResponseEntity<>("{\"message\":\"Utilisateur non trouvé\"}", HttpStatus.NOT_FOUND);
            }
            UserInfo user = optionalUser.get();
            // Generate a reset token
            String resetToken = UUID.randomUUID().toString();
            user.setResetToken(resetToken);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1)); // Token expires in 1 hour
            try {
                userInfoRepository.save(user);
                log.info("Jeton de réinitialisation enregistré pour l'utilisateur : {}", email);
            } catch (Exception e) {
                log.error("Échec de l'enregistrement du jeton de réinitialisation pour l'utilisateur {} : {}", email,
                        e.getMessage(), e);
                return new ResponseEntity<>("{\"message\":\"Échec de l'enregistrement du jeton de réinitialisation\"}",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            // Send HTML email with a styled button for welcome message
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
                    "<h1 style='color: #ffffff; margin: 0; font-size: 24px;'>Bienvenue chez CSI Connecte !</h1>" +
                    "</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='padding: 30px;'>" +
                    "<p style='font-size: 16px; margin: 0 0 20px;'>Bonjour " + userName + ",</p>" +
                    "<p style='font-size: 16px; margin: 0 0 20px;'>Votre compte a été créé avec succès. Veuillez définir votre mot de passe en cliquant sur le bouton ci-dessous :</p>"
                    +
                    "<table width='100%' cellpadding='0' cellspacing='0'>" +
                    "<tr>" +
                    "<td style='text-align: center; padding: 20px 0;'>" +
                    "<a href='" + resetLink +
                    "' style='display: inline-block; padding: 12px 30px; background: linear-gradient(to bottom, #1e3c72, #1e3c72); color: #ffffff; text-decoration: none; font-size: 16px; font-weight: bold; border-radius: 5px; box-shadow: 0 2px 5px rgba(0,0,0,0.2); transition: background 0.3s;'>"
                    +
                    "Définir votre mot de passe</a>" +
                    "</td>" +
                    "</tr>" +
                    "</table>" +
                    "<p style='font-size: 14px; color: #666666; margin: 20px 0 0;'>Ce lien est valide pendant 1 heure.</p>"
                    +
                    "<p style='font-size: 14px; color: #666666; margin: 5px 0 0;'>Si vous n'avez pas demandé la création de ce compte, veuillez nous contacter immédiatement.</p>"
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
            helper.setSubject("Bienvenue chez CNI - Définissez votre mot de passe");
            helper.setText(htmlContent, true); // true indicates HTML content
            try {
                mailSender.send(message);
                log.info("Email de bienvenue envoyé avec succès à : {}", email);
            } catch (MailSendException e) {
                log.error("Échec de l'envoi de l'email de bienvenue à {} : {}", email, e.getMessage(), e);
                return new ResponseEntity<>("{\"message\":\"Échec de l'envoi de l'email de bienvenue\"}",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>("{\"message\":\"Email de bienvenue envoyé avec succès\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur inattendue dans sendWelcomeEmail pour l'email {} : {}", email, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Une erreur inattendue s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> resetPassword(String token, String newPassword) {
        log.info("Traitement de la réinitialisation de mot de passe avec le jeton : {}", token);
        try {
            if (!StringUtils.hasText(token) || !StringUtils.hasText(newPassword)) {
                log.warn("Le jeton ou le nouveau mot de passe est vide");
                return new ResponseEntity<>("{\"message\":\"Le jeton et le nouveau mot de passe sont requis\"}",
                        HttpStatus.BAD_REQUEST);
            }
            Optional<UserInfo> optionalUser = userInfoRepository.findByResetToken(token);
            if (optionalUser.isEmpty()) {
                log.warn("Jeton invalide ou expiré : {}", token);
                return new ResponseEntity<>("{\"message\":\"Jeton invalide ou expiré\"}", HttpStatus.BAD_REQUEST);
            }
            UserInfo user = optionalUser.get();
            if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                user.setResetToken(null);
                user.setResetTokenExpiry(null);
                userInfoRepository.save(user);
                log.warn("Le jeton a expiré pour l'utilisateur : {}", user.getEmail());
                return new ResponseEntity<>("{\"message\":\"Le jeton a expiré\"}", HttpStatus.BAD_REQUEST);
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userInfoRepository.save(user);
            log.info("Mot de passe réinitialisé avec succès pour l'utilisateur : {}", user.getEmail());
            return new ResponseEntity<>("{\"message\":\"Mot de passe réinitialisé avec succès\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur dans resetPassword : {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Une erreur s'est produite\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}