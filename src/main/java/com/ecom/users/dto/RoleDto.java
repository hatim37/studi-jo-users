package com.ecom.users.dto;

import com.ecom.users.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {
    private int id;
    private String name;

    public RoleDto(Role role) {
        this.id = role.getId();
        this.name = role.getLibelle();
    }
}