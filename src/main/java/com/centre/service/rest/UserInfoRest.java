package com.centre.service.rest;


import org.springframework.http.ResponseEntity; 
import org.springframework.web.bind.annotation.*;
import com.centre.service.model.AuthRequest;
 import com.centre.service.model.UserInfo; 


@RequestMapping(path="/api/personnes") 
public interface UserInfoRest {

    @PostMapping(path="/addNewAppuser")
     ResponseEntity<?> addNewAppuser(@RequestBody(required = true) UserInfo UserInfo);
    

     @PostMapping(path = "/login")
     ResponseEntity<?> login(@RequestBody(required = true) AuthRequest authRequest);

     @GetMapping(path="/getallappuser")
     public ResponseEntity<?> getAllAppuser();
    
     @PutMapping("/updateAppuser/{id}")
     ResponseEntity<?> updateAppuser(@PathVariable Long id, @RequestBody UserInfo userInfo);

     @DeleteMapping("/deleteAppuser/{id}")
     ResponseEntity<?> deleteAppuser(@PathVariable Long id);

     @GetMapping(path="/checktoken")
     ResponseEntity<?> checkToken();


}