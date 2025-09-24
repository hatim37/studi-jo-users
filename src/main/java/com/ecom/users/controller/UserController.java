package com.ecom.users.controller;


import com.ecom.users.entity.User;
import com.ecom.users.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

@AllArgsConstructor
@RestController
public class UserController {

    private UserService userService;

    @PostMapping(path = "/registration")
    public ResponseEntity<?> inscription(@RequestBody User user) throws GeneralSecurityException {
       return ResponseEntity.ok().body(this.userService.registration(user));
    }


}
