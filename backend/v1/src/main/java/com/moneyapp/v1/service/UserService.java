package com.moneyapp.v1.service;

import com.moneyapp.v1.repository.UserRepository;
import com.moneyapp.v1.dto.LoginRequestDto;
import com.moneyapp.v1.dto.LoginResponseDto;
import com.moneyapp.v1.dto.RegisterRequestDto;
import com.moneyapp.v1.dto.RegisterResponseDto;
import com.moneyapp.v1.dto.UpdateUserRequestDto;
import com.moneyapp.v1.dto.UpdateUserRoleRequestDto;
import com.moneyapp.v1.dto.UserResponseDto;
import com.moneyapp.v1.enums.Role;
import com.moneyapp.v1.exception.InvalidRequestException;
import com.moneyapp.v1.exception.NotFoundException;
import com.moneyapp.v1.exception.UnauthorizedException;
import com.moneyapp.v1.factory.UserFactory;
import com.moneyapp.v1.model.User;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserFactory userFactory;

    public RegisterResponseDto createUser(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidRequestException("Email already in use");
        }
        
        String password = passwordEncoder.encode((request.getPassword()));

        User user = userFactory.create(request.getName(), request.getEmail(), password, request.getRole());
        
        User newUser = userRepository.save(user);

        String token = jwtService.generateToken(newUser);
        
        return new RegisterResponseDto(
            "User created successfully",
            newUser.getId(),
            newUser.getEmail(),
            newUser.getName(),
            newUser.getRole(),
            token,
            newUser.getCreatedAt()
        );
    }

    public LoginResponseDto loginUser(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail()).
            orElseThrow(() -> new NotFoundException("Email not found."));
        
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidRequestException("Invalid password.");
        }

        if (!user.getActive()) {
            throw new InvalidRequestException("User was deleted");
        }

        String token = jwtService.generateToken(user);
        
        return new LoginResponseDto(
                "Login successfully",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                token,
                user.getCreatedAt()
        );
    }

    public UserResponseDto getUser(User userRequest) {
        User user = userRepository.findById(userRequest.getId())
            .orElseThrow(() -> new NotFoundException("User not found."));
        
        return new UserResponseDto(
            user.getId(),
            user.getEmail(), 
            user.getName(), 
            user.getRole(), 
            user.getCreatedAt(), 
            user.getUpdatedAt(), 
            user.getDeletedAt(), 
            user.getActive()
        );       
    }

    public List<UserResponseDto> getAllUsers(User userRequest) {
        List<User> users = userRepository.findAll();
        
        return users.stream().map(user -> new UserResponseDto(
            user.getId(),
            user.getEmail(), 
            user.getName(), 
            user.getRole(), 
            user.getCreatedAt(), 
            user.getUpdatedAt(), 
            user.getDeletedAt(), 
            user.getActive()
        )).toList();
    }

    public UserResponseDto updateUser(User userRequest, UpdateUserRequestDto request) {
        User user = userRepository.findById(userRequest.getId())
            .orElseThrow(() -> new NotFoundException("User not found."));
        if (request.getName() != null) user.setName(request.getName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        
        user.setUpdatedAt(new Date());
        userRepository.save(user);

        return new UserResponseDto(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getRole(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getDeletedAt(),
            user.getActive()
        );
    }

    public UserResponseDto updateUserRole(UpdateUserRoleRequestDto request, UUID user_id) {
        User userModified = userRepository.findById(user_id)
            .orElseThrow(() -> new NotFoundException("User not found."));

        userModified.setRole(request.getRole());
        userModified.setUpdatedAt(new Date());
        userRepository.save(userModified);

        return new UserResponseDto(
            userModified.getId(),
            userModified.getEmail(),
            userModified.getName(),
            userModified.getRole(),
            userModified.getCreatedAt(),
            userModified.getUpdatedAt(),
            userModified.getDeletedAt(),
            userModified.getActive()
        );
    }

    public UserResponseDto deleteUser(UUID user_id, User userRequest) {
        User user = userRepository.findById(userRequest.getId())
            .orElseThrow(() -> new NotFoundException("User not found."));
        
        boolean isSelf = user.getId().equals(user_id);
        boolean isAdminOrModerator = user.getRole() == Role.ADMIN || user.getRole() == Role.MODERATOR;

        if (!isSelf && !isAdminOrModerator) {
            throw new UnauthorizedException("User is not authorized to make this action.");
        }

        User deletedUser = userRepository.findById(user_id)
            .orElseThrow(() -> new NotFoundException("User not found."));

        deletedUser.setActive(false);
        deletedUser.setDeletedAt(new Date());
        userRepository.save(deletedUser);
        
        return new UserResponseDto(
            deletedUser.getId(),
            deletedUser.getEmail(), 
            deletedUser.getName(), 
            deletedUser.getRole(), 
            deletedUser.getCreatedAt(), 
            deletedUser.getUpdatedAt(), 
            deletedUser.getDeletedAt(), 
            deletedUser.getActive()
        );
    }

}
