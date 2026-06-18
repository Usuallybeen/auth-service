package com.example.auth_service.controller;

import com.example.auth_service.dto.UserResponse;
import com.example.auth_service.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUserDetails(@AuthenticationPrincipal User user){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(UserResponse.builder().username(user.getUsername()).role(user.getRole()).build());
    }
}
