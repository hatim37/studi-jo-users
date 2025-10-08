package com.ecom.users.controller;


import com.ecom.users.dto.NewPasswordDto;
import com.ecom.users.dto.UserActivationDto;
import com.ecom.users.dto.UserDto;
import com.ecom.users.dto.UserLoginDto;
import com.ecom.users.entity.User;
import com.ecom.users.repository.UserRepository;
import com.ecom.users.service.TokenTechnicService;
import com.ecom.users.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class MicroServiceController {

    private final UserService userService;
    private final TokenTechnicService tokenTechnicService;

    public MicroServiceController(UserService userService, TokenTechnicService tokenTechnicService) {
        this.userService = userService;
        this.tokenTechnicService = tokenTechnicService;
    }

    @PostMapping("/_internal/user-activation")
    public void activationDeviceId(@RequestBody UserActivationDto userActivationDto){
        this.userService.activationUser(userActivationDto);
    }

    @GetMapping(path = "/tokenTechnic")
    public String token() {
       return this.tokenTechnicService.getTechnicalToken();
    }

    @GetMapping("/_internal/users-login/{email}")
    public UserLoginDto userLogin(@PathVariable String email){
        User user = userService.findByEmail(email);
        return new UserLoginDto(user);
    }

    @GetMapping("/_internal/users/{id}")
    public User findById(@PathVariable Long id){
        return (userService.findById(id));
    }

    @GetMapping(path = "/_internal/allUsers")
    public List<UserDto> getUsers() {
        return userService.findAll();
    }

    @PostMapping(path = "/_internal/new-password")
    public ResponseEntity<?> newPassword(@RequestBody NewPasswordDto newPasswordDto) {
        return ResponseEntity.ok().body(this.userService.newPassword(newPasswordDto));
    }

}

