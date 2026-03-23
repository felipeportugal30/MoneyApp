package com.moneyapp.v1.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.moneyapp.v1.dto.LoginRequestDto;
import com.moneyapp.v1.dto.LoginResponseDto;
import com.moneyapp.v1.dto.RegisterRequestDto;
import com.moneyapp.v1.dto.RegisterResponseDto;
import com.moneyapp.v1.dto.UpdateUserRequestDto;
import com.moneyapp.v1.dto.UpdateUserRoleRequestDto;
import com.moneyapp.v1.dto.UserResponseDto;
import com.moneyapp.v1.model.User;
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

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponseDto> getMe(
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(userService.getUser(user));
    }

    @GetMapping("/list-all")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAll(
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(userService.getAllUsers(user));
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<UserResponseDto> updateUser(
        @AuthenticationPrincipal User user,
        @RequestBody UpdateUserRequestDto request
    ) {
        return ResponseEntity.ok(userService.updateUser(user, request));
    }

    @PutMapping("/update/role/{user_id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserResponseDto> updateUserRole(
        @AuthenticationPrincipal User user,
        @RequestBody UpdateUserRoleRequestDto request,
        @PathVariable UUID user_id
    ) {
        return ResponseEntity.ok(userService.updateUserRole(request, user_id));
    }

    @DeleteMapping("/delete/{user_id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponseDto> deleteUser(
        @AuthenticationPrincipal User user,
        @PathVariable UUID user_id
    ) {
        return ResponseEntity.ok(userService.deleteUser(user_id, user));
    }
}