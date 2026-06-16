package com.example.auth_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.auth_service.dto.AuthRequest;
import com.example.auth_service.entity.User;
import com.example.auth_service.exceptions.UsernameAlreadyExistsException;
import com.example.auth_service.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.SQLException;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager aManager;

    @InjectMocks
    private AuthService authService;

    @Test
    public void register_user(){
        // given
        AuthRequest request = new AuthRequest("test", "pass");

        when(passwordEncoder.encode(any())).thenReturn(request.getPassword());
        when(jwtService.generateToken(any())).thenReturn("token");

        // when
        String token = authService.register(request);

        // then
        verify(passwordEncoder).encode(any());
        verify(userRepository).save(any());
        verify(jwtService).generateToken(any());

        assertThat(token).isEqualTo("token");
    }

    @Test
    public void register_alreadyExisting_user() {
        // given
        AuthRequest request = new AuthRequest("test", "pass");

        when(passwordEncoder.encode(any())).thenReturn(request.getPassword());
        when(userRepository.save(any())).thenThrow(new DataIntegrityViolationException("", new ConstraintViolationException("constraint violation", new SQLException(), User.USERNAME_CONSTRAINT)));

        // then
        assertThrows(UsernameAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    public void authenticate_user(){
        // given
        AuthRequest request = new AuthRequest("test", "pass");

        User user = User.builder().username(request.getUsername()).password(request.getPassword()).build();
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, null);

        when(aManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken(any())).thenReturn("token");

        // when
        String token = authService.authenticate(request);

        // then
        verify(aManager).authenticate(any());
        verify(jwtService).generateToken(any());

        assertThat(token).isEqualTo("token");
    }

    @Test
    public void authenticate_badCredentials_user(){
        // given
        AuthRequest request = new AuthRequest("test", "pass");

        when(aManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        // then
        assertThrows(BadCredentialsException.class, () -> authService.authenticate(request));
    }
}
