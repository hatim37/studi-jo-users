package com.ecom.users.controller;


import com.ecom.users.dto.UserActivationDto;
import com.ecom.users.service.TokenTechnicService;
import com.ecom.users.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class MicroServiceController {

    private final UserService userService;
    private final TokenTechnicService tokenTechnicService;

    public MicroServiceController(UserService userService,TokenTechnicService tokenTechnicService) {
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
}

