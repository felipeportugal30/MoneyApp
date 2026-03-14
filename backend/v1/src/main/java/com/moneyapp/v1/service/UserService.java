package com.moneyapp.v1.service;

import com.moneyapp.v1.repository.UserRepository;
import com.moneyapp.v1.dto.LoginRequestDTO;
import com.moneyapp.v1.dto.LoginResponseDTO;
import com.moneyapp.v1.dto.RegisterRequestDTO;
import com.moneyapp.v1.dto.RegisterResponseDTO;
import com.moneyapp.v1.exception.InvalidRequestException;
import com.moneyapp.v1.exception.NotFoundException;
import com.moneyapp.v1.model.User;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponseDTO createUser(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidRequestException("Email já está em uso");
        }
        
        User user = new User();
        
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User newUser = userRepository.save(user);

        String token = jwtService.generateToken(newUser);
        
        return new RegisterResponseDTO(
            "User created successfully",
            newUser.getId(),
            newUser.getEmail(),
            newUser.getName(),
            token
        );
    }

    public LoginResponseDTO loginUser(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail()).
            orElseThrow(() -> new NotFoundException("Email not found."));
        
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidRequestException("Invalid password.");
        }

        String token = jwtService.generateToken(user);
        
        return new LoginResponseDTO(
                "Login successfully",
                user.getId(),
                user.getName(),
                user.getEmail(),
                token
        );
    }

    public User getUserByEmail(String email) {
        
        if (email == null) {
            throw new InvalidRequestException("Email can't be null");
        }

        User user = userRepository.findByEmail(email).
            orElseThrow(() -> new NotFoundException("Email not found."));

        return user;
    }
}
