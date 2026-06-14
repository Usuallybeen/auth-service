package com.example.auth_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.auth_service.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp(){
        this.jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "KNfddu/OMqO5FaHTk1vMiIUWClVYYQ+/S0iIQQHMUSs=");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);
    }

    @Test
    void generateToken_shouldEncodeUsernameInToken(){
        // given
        User user = User.builder().username("test").password("test").role("ROLE_USER").build();

        // when
        String token = jwtService.generateToken(user);

        // then
        assertThat(jwtService.extractUsername(token)).isEqualTo("test");
    }

    @Test
    void generateToken_matchingUsername_notExpired(){
        // given
        User user = User.builder().username("test").password("test").role("ROLE_USER").build();

        // when
        String token = jwtService.generateToken(user);

        // then
        assertThat(jwtService.extractUsername(token)).isEqualTo("test");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void generateToken_differentUsername_notExpired(){
        // given
        User user = User.builder().username("test").password("test").role("ROLE_USER").build();
        User otherUser = User.builder().username("otherUser").password("test").role("ROLE_USER").build();

        // when
        String token = jwtService.generateToken(user);

        // then
        assertThat(jwtService.extractUsername(token)).isNotEqualTo("otherUser");
        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void generateToken_matchingUsername_expired(){
        // given
        User user = User.builder().username("test").password("test").role("ROLE_USER").build();

        // when
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);
        String token = jwtService.generateToken(user);

        // then
        assertThat(jwtService.isTokenValid(token, user)).isFalse();
        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(token));
    }
}
