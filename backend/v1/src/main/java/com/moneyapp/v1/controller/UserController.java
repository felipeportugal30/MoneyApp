package com.moneyapp.v1.controller;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.moneyapp.v1.dto.LoginRequestDTO;
import com.moneyapp.v1.dto.LoginResponseDTO;
import com.moneyapp.v1.dto.RegisterRequestDTO;
import com.moneyapp.v1.dto.RegisterResponseDTO;
import com.moneyapp.v1.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    
    @PostMapping("/create")
    public RegisterResponseDTO createUser(@Valid @RequestBody RegisterRequestDTO request) {
        return userService.createUser(request);
    }

    @PostMapping("/login")
    public LoginResponseDTO loginUser(@RequestBody LoginRequestDTO dto) {
        return userService.loginUser(dto);
    }
}