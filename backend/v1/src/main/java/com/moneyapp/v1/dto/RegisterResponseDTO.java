package com.moneyapp.v1.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterResponseDTO {
    private String message;
    private UUID id;
    private String email;
    private String name;
    private String token;
}
