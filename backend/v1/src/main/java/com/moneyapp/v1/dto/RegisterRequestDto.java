package com.moneyapp.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import com.moneyapp.v1.enums.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
@AllArgsConstructor
public class RegisterRequestDto {
    
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Role is required")
    private Role role;

    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{6,}$",
        message = "Password must have at least 6 characters, one uppercase letter, one number and one special character"
    )
    private String password;
}
