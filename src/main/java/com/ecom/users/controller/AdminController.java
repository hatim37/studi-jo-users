package com.ecom.users.controller;

import com.ecom.users.dto.UpdateRolesDto;
import com.ecom.users.dto.UserDto;
import com.ecom.users.repository.UserRepository;
import com.ecom.users.service.AdminService;
import com.ecom.users.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RestController
public class AdminController {
    private final UserRepository userRepository;
    private final AdminService adminService;

    public AdminController(UserRepository userRepository, AdminService adminService) {
        this.userRepository = userRepository;
        this.adminService = adminService;
    }

    @GetMapping(path = "/admin/users")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public List<UserDto> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/admin/updateRoles/{userId}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<?> updateRoles(@PathVariable Long userId, @RequestBody UpdateRolesDto rolesNames) {
        return ResponseEntity.ok().body(this.adminService.adminAddRoleUsers(userId, rolesNames));
    }




}
