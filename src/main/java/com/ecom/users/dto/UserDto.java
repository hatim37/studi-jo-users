package com.ecom.users.dto;

import com.ecom.users.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDto {
    private long id;
    private String name;
    private String username;
    private String email;
    private Boolean active;
    private Set<RoleDto> roles;

    public UserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.active = user.getActive();
        this.roles = user.getRoles()
                .stream()
                .map(RoleDto::new)
                .collect(Collectors.toSet());
    }
}


