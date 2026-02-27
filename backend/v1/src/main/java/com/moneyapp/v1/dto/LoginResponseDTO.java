package com.moneyapp.v1.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponseDTO {
    private String message;
    private UUID id;
    private String name;
    private String email;
    private String token;
}
