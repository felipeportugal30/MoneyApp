package com.moneyapp.v1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moneyapp.v1.dto.SendMessageRequestDto;
import com.moneyapp.v1.dto.SendMessageResponseDto;
import com.moneyapp.v1.model.User;
import com.moneyapp.v1.service.ConversationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/conversation")
@RequiredArgsConstructor
public class ConversationController {
    
    private final ConversationService conversationService;

    @PostMapping("")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<SendMessageResponseDto> sendMessage(
        @AuthenticationPrincipal User user,
        @RequestBody SendMessageRequestDto request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(conversationService.sendMessage(user, request));
    }
}
