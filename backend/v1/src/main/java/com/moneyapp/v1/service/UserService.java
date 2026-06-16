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

        String password = passwordEncoder.encode(request.getPassword());
        User user = userFactory.create(request.getName(), request.getEmail(), password, Role.USER);
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
        User user = userRepository.findByEmail(dto.getEmail())
            .orElseThrow(() -> new InvalidRequestException("Invalid credentials."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidRequestException("Invalid credentials.");
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

        return toDto(user);
    }

    public List<UserResponseDto> getAllUsers(User userRequest) {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    public UserResponseDto updateUser(User userRequest, UpdateUserRequestDto request) {
        User user = userRepository.findById(userRequest.getId())
            .orElseThrow(() -> new NotFoundException("User not found."));

        if (request.getName() != null) user.setName(request.getName());

        if (request.getEmail() != null) {
            if (!request.getEmail().equals(user.getEmail())
                    && userRepository.existsByEmail(request.getEmail())) {
                throw new InvalidRequestException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        user.setUpdatedAt(new Date());
        userRepository.save(user);

        return toDto(user);
    }

    public UserResponseDto updateUserRole(UpdateUserRoleRequestDto request, UUID user_id) {
        User userModified = userRepository.findById(user_id)
            .orElseThrow(() -> new NotFoundException("User not found."));

        userModified.setRole(request.getRole());
        userModified.setUpdatedAt(new Date());
        userRepository.save(userModified);

        return toDto(userModified);
    }

    public void deleteUser(UUID user_id, User userRequest) {
        boolean isSelf = userRequest.getId().equals(user_id);
        boolean isAdminOrModerator = userRequest.getRole() == Role.ADMIN
            || userRequest.getRole() == Role.MODERATOR;

        if (!isSelf && !isAdminOrModerator) {
            throw new UnauthorizedException("User is not authorized to make this action.");
        }

        User target = userRepository.findById(user_id)
            .orElseThrow(() -> new NotFoundException("User not found."));

        target.setActive(false);
        target.setDeletedAt(new Date());
        userRepository.save(target);
    }

    private UserResponseDto toDto(User user) {
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
}
