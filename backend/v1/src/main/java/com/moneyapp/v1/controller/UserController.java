package com.moneyapp.v1.controller;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.moneyapp.v1.dto.LoginRequestDto;
import com.moneyapp.v1.dto.LoginResponseDto;
import com.moneyapp.v1.dto.RegisterRequestDto;
import com.moneyapp.v1.dto.RegisterResponseDto;
import com.moneyapp.v1.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    
    @PostMapping("/create")
    public RegisterResponseDto createUser(@Valid @RequestBody RegisterRequestDto request) {
        return userService.createUser(request);
    }

    @PostMapping("/login")
    public LoginResponseDto loginUser(@RequestBody LoginRequestDto dto) {
        return userService.loginUser(dto);
    }
}