package com.ecom.users.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateRolesDto {
    private List<String> roleNames;
}
