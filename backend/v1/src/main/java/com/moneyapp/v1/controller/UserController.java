package com.moneyapp.v1.controller;

import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<RegisterResponseDto> createUser(@Valid @RequestBody RegisterRequestDto request) {
        return ResponseEntity.status(201).body(userService.createUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> loginUser(@RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(userService.loginUser(dto));
    }
}