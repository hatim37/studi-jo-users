package com.ecom.users.dto;

import lombok.Data;

@Data
public class NewPasswordDto {
    private Long userId;
    private String password;
}
