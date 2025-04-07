package com.centre.service.serviceImpl;

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
        try { // Vérifiez si les informations de l'utilisateur sont valides 
        if (!ValidateUserInfo(userInfo)) { 
            return new ResponseEntity<>("{\"message\":\"Missing required Data\"}", HttpStatus.BAD_REQUEST); }

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
        userInfo.setRole(userInfo.getRole());

        // Enregistrez l'utilisateur dans la base de données
        userInfoRepository.save(userInfo);
        return new ResponseEntity<>("{\"message\":\"User  created successfully\"}", HttpStatus.CREATED);
    } catch (Exception e) {
        log.error("Error while adding new user: {}", e);
    }
    return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean ValidateUserInfo(UserInfo userInfo) {
        return !Objects.isNull(userInfo) && StringUtils.hasText(userInfo.getName()) && StringUtils.hasText(userInfo.getEmail()) && StringUtils.hasText(userInfo.getPassword());

    }

    @Override
    public ResponseEntity<?> login(AuthRequest authRequest) {
        try{
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail().toLowerCase(), authRequest.getPassword()));
            if (authentication!=null && authentication.isAuthenticated()) {
                UserInfoDetails userDetails = (UserInfoDetails) authentication.getPrincipal();
                if ("true".equalsIgnoreCase(userDetails.getStatus())) {
                    return new ResponseEntity<>("{\"token\":\""+jwtService.generateToken(authRequest.getEmail().toLowerCase())+"\"}", HttpStatus.OK);
                }else{
                    return new ResponseEntity<>("{\"message\":\"Wait for admin approval\"}", HttpStatus.BAD_REQUEST);
                }
                
            }else{
                throw new UsernameNotFoundException("invalide user request");
            }
        }
        catch(UsernameNotFoundException ex){
           throw ex;
        }
        
        catch(BadCredentialsException ex){
            return new ResponseEntity<>("{\"message\":\"Invalid Cridentials\"}", HttpStatus.UNAUTHORIZED);
        }

        catch (Exception e) {
        log.error("Error while login: {}", e);
    }
    return new ResponseEntity<>("{\"message\":\"Something went wrong\"}", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

