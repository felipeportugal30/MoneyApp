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
public class UserResponseDto {
    private UUID id;
    private String email;
    private String name;
    private Role role;
    private Date createdAt;
    private Date updatedAt;
    private Date deletedAt;
    private boolean active;
}
