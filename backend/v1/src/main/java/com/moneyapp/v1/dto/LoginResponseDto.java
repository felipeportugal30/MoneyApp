package com.moneyapp.v1.dto;

import java.util.Date;
import java.util.UUID;

import com.moneyapp.v1.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponseDto {
    private String message;
    private UUID id;
    private String name;
    private String email;
    private Role role;
    private String token;
    private Date createdAt;
}
