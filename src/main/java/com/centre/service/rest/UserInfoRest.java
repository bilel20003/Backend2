package com.centre.service.rest;

import java.util.List; import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;

import com.centre.service.model.AuthRequest;
import com.centre.service.model.EtatRequete; 
import com.centre.service.model.UserInfo; 
import com.centre.service.model.Requete; 
import com.centre.service.model.Role; 
import com.centre.service.repository.UserInfoRepository;

import com.centre.service.repository.RequeteRepository;

@RequestMapping(path="/api/personnes") 
public interface UserInfoRest {

    @PostMapping(path="/addNewAppuser")
     ResponseEntity<?> addNewAppuser(@RequestBody(required = true) UserInfo UserInfo);
    

     @PostMapping(path = "/login")
     ResponseEntity<?> login(@RequestBody(required = true) AuthRequest authRequest);
    
}