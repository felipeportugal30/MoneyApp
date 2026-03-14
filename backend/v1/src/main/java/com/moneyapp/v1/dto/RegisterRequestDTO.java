package com.moneyapp.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
@AllArgsConstructor
public class RegisterRequestDTO {
    
    private String name;
    private String email;

    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{6,}$",
        message = "Password must have at least 6 characters, one uppercase letter, one number and one special character"
    )
    private String password;
}
