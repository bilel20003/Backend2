package com.centre.service.serviceImpl;

import com.centre.service.model.Role;
import com.centre.service.model.Produit;
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
import com.centre.service.model.UserInfo;
import com.centre.service.repository.UserInfoRepository;
import com.centre.service.repository.RoleRepository;
import com.centre.service.repository.ProduitRepository;
import com.centre.service.service.UserInfoService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    Logger log = LoggerFactory.getLogger(UserInfoServiceImpl.class);

    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    ProduitRepository produitRepository;

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
            if (!ValidateUserInfo(userInfo)) {
                return new ResponseEntity<>("{\"message\":\"Missing required Data\"}", HttpStatus.BAD_REQUEST);
            }

            Optional<UserInfo> db = userInfoRepository.findByEmail(userInfo.getEmail());
            if (db.isPresent()) {
                return new ResponseEntity<>("{\"message\":\"Email already exists\"}", HttpStatus.BAD_REQUEST);
            }

            if (userInfo.getRole() == null || userInfo.getRole().getId() == null) {
                return new ResponseEntity<>("{\"message\":\"Role must be provided\"}", HttpStatus.BAD_REQUEST);
            }
            Optional<Role> roleOpt = roleRepository.findById(userInfo.getRole().getId());
            if (roleOpt.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Role not found\"}", HttpStatus.BAD_REQUEST);
            }
            userInfo.setRole(roleOpt.get());

            boolean isClient = "CLIENT".equalsIgnoreCase(roleOpt.get().getName());
            if (isClient) {
                if (userInfo.getProduit() == null || userInfo.getProduit().getId() == null) {
                    return new ResponseEntity<>("{\"message\":\"Produit must be provided for CLIENT role\"}",
                            HttpStatus.BAD_REQUEST);
                }
                Optional<Produit> produitOpt = produitRepository.findById(userInfo.getProduit().getId());
                if (produitOpt.isEmpty()) {
                    return new ResponseEntity<>("{\"message\":\"Produit not found\"}", HttpStatus.BAD_REQUEST);
                }
                if ("Any".equalsIgnoreCase(produitOpt.get().getNom())) {
                    return new ResponseEntity<>("{\"message\":\"Cannot assign 'Any' product to CLIENT role\"}",
                            HttpStatus.BAD_REQUEST);
                }
                userInfo.setProduit(produitOpt.get());
            } else {
                Optional<Produit> anyProduitOpt = produitRepository.findByNom("Any");
                if (anyProduitOpt.isEmpty()) {
                    return new ResponseEntity<>("{\"message\":\"Default 'Any' product not found\"}",
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }
                userInfo.setProduit(anyProduitOpt.get());
            }

            if (userInfo.getService() == null || userInfo.getService().getId() == null) {
                return new ResponseEntity<>("{\"message\":\"Service must be provided\"}", HttpStatus.BAD_REQUEST);
            }

            userInfo.setPassword(encoder.encode(userInfo.getPassword()));
            userInfo.setStatus("false");
            userInfo.setEmail(userInfo.getEmail().toLowerCase());
            userInfo.setIsDeletable("true");

            userInfoRepository.save(userInfo);
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
            log.error("Error while login: {}", ex.getMessage(), ex);
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
            log.error("Error while getAllAppuser: {}", ex.getMessage(), ex);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllTechniciens() {
        try {
            List<UserInfo> techniciens = userInfoRepository.findActiveTechniciens();
            return new ResponseEntity<>(techniciens, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while getting all techniciens: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateAppuser(Long id, UserInfo updatedUser) {
        try {
            Optional<UserInfo> optionalUser = userInfoRepository.findById(id);
            if (optionalUser.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"User not found\"}", HttpStatus.NOT_FOUND);
            }

            if (updatedUser.getEmail() != null) {
                Optional<UserInfo> db = userInfoRepository.findByEmail(updatedUser.getEmail());
                if (db.isPresent() && !db.get().getId().equals(id)) {
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
                    return new ResponseEntity<>("{\"message\":\"Produit not found\"}", HttpStatus.BAD_REQUEST);
                }
                if (isClient && "Any".equalsIgnoreCase(produitOpt.get().getNom())) {
                    return new ResponseEntity<>("{\"message\":\"Cannot assign 'Any' product to CLIENT role\"}",
                            HttpStatus.BAD_REQUEST);
                }
                user.setProduit(produitOpt.get());
                isUpdated = true;
            } else if (isClient) {
                if (user.getProduit() == null || "Any".equalsIgnoreCase(user.getProduit().getNom())) {
                    return new ResponseEntity<>("{\"message\":\"Produit must be provided for CLIENT role\"}",
                            HttpStatus.BAD_REQUEST);
                }
            } else {
                Optional<Produit> anyProduitOpt = produitRepository.findByNom("Any");
                if (anyProduitOpt.isEmpty()) {
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
                return new ResponseEntity<>("{\"message\":\"No valid fields provided for update\"}",
                        HttpStatus.BAD_REQUEST);
            }

            userInfoRepository.save(user);
            return new ResponseEntity<>("{\"message\":\"User updated successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error updating user: {}", e.getMessage(), e);
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
            log.error("Error deleting user: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> checkToken() {
        return new ResponseEntity<>("{\"message\":\"true\"}", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> toggleStatus(Long id) {
        try {
            Optional<UserInfo> optionalUser = userInfoRepository.findById(id);
            if (optionalUser.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"User not found\"}", HttpStatus.NOT_FOUND);
            }

            UserInfo user = optionalUser.get();
            user.setStatus("true".equals(user.getStatus()) ? "false" : "true");
            userInfoRepository.save(user);

            return new ResponseEntity<>("{\"message\":\"User status toggled successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error toggling user status: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}