package com.example.auth_service.service;

import com.example.auth_service.dto.AuthRequest;
import com.example.auth_service.entity.User;
import com.example.auth_service.exceptions.UsernameAlreadyExistsException;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager aManager;

    public String register(AuthRequest request) {
        User user = User.builder().username(request.getUsername()).password(
                passwordEncoder.encode(request.getPassword())).role("ROLE_USER").build();

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException dViolationException) {
            Throwable cause = dViolationException.getCause();
            if (cause instanceof ConstraintViolationException cve && User.USERNAME_CONSTRAINT.equals(cve.getConstraintName())) {
                throw new UsernameAlreadyExistsException();
            }
            throw dViolationException;
        }
        return jwtService.generateToken(user);
    }

    public String authenticate(AuthRequest request) {
        aManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        return jwtService.generateToken(user);
    }
}
