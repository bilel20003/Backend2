package com.centre.service.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.centre.service.model.AuthRequest;
import com.centre.service.model.UserInfo;
import java.util.Map;

@RequestMapping(path = "/api/personnes")
public interface UserInfoRest {

    @GetMapping("/appuser/{id}")
    ResponseEntity<UserInfo> getAppuserById(@PathVariable Long id);

    @PostMapping(path = "/addNewAppuser")
    ResponseEntity<?> addNewAppuser(@RequestBody(required = true) UserInfo userInfo);

    @PostMapping(path = "/login")
    ResponseEntity<?> login(@RequestBody(required = true) AuthRequest authRequest);

    @GetMapping(path = "/getallappuser")
    ResponseEntity<?> getAllAppuser();

    @GetMapping(path = "/getalltechniciens")
    ResponseEntity<?> getAllTechniciens();

    @PutMapping("/updateAppuser/{id}")
    ResponseEntity<?> updateAppuser(@PathVariable Long id, @RequestBody UserInfo userInfo);

    @PutMapping("/archiveAppuser/{id}")
    ResponseEntity<?> archiveAppuser(@PathVariable Long id);

    @GetMapping(path = "/checktoken")
    ResponseEntity<?> checkToken();

    @PutMapping("/toggleStatus/{id}")
    ResponseEntity<?> toggleStatus(@PathVariable Long id);

    @PutMapping("/changePassword/{id}")
    ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> body);

}