package com.ecom.users.controller;


import com.ecom.users.dto.UserActivationDto;
import com.ecom.users.dto.UserLoginDto;
import com.ecom.users.entity.User;
import com.ecom.users.repository.UserRepository;
import com.ecom.users.service.TokenTechnicService;
import com.ecom.users.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class MicroServiceController {

    private final UserService userService;
    private final TokenTechnicService tokenTechnicService;
    private final UserRepository userRepository;

    public MicroServiceController(UserService userService, TokenTechnicService tokenTechnicService, UserRepository userRepository) {
        this.userService = userService;
        this.tokenTechnicService = tokenTechnicService;
        this.userRepository = userRepository;
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
        User user = userRepository.findByEmail(email).get();
        return new UserLoginDto(user);
    }
}

