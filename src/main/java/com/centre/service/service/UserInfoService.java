package com.centre.service.service;

import org.springframework.http.ResponseEntity; 
import org.springframework.stereotype.Service;

import com.centre.service.model.AuthRequest;
import com.centre.service.model.UserInfo;

@Service 
public interface UserInfoService {

    ResponseEntity<?> addNewAppuser(UserInfo UserInfo);

    ResponseEntity<?> login(AuthRequest authRequest);
    
}