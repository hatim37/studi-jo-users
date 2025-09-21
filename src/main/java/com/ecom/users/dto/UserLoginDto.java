package com.ecom.users.dto;

import com.ecom.users.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserLoginDto {
    private long id;
    private String name;
    private String username;
    private String email;
    private String password;
    private Boolean active;
    private Set<RoleDto> roles;

    public UserLoginDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.active = user.getActive();
        this.roles = user.getRoles()
                .stream()
                .map(RoleDto::new)
                .collect(Collectors.toSet());
    }
}

