package com.moneyapp.v1.service;

import com.moneyapp.v1.repository.UserRepository;
import com.moneyapp.v1.dto.LoginRequestDTO;
import com.moneyapp.v1.dto.LoginResponseDTO;
import com.moneyapp.v1.dto.RegisterRequestDTO;
import com.moneyapp.v1.dto.RegisterResponseDTO;
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
            orElseThrow(() -> new RuntimeException("Email or password invalids."));
        
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email or password invalids.");
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
}
