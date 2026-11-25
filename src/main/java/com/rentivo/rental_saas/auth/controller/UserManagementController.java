package com.rentivo.rental_saas.auth.controller;

import com.rentivo.rental_saas.auth.entity.Users;
import com.rentivo.rental_saas.auth.service.UserManagementService;
import com.rentivo.rental_saas.common.response.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping("/me")
    public ResponseEntity<?> fetchCurrentUser(
            Authentication authentication
    ) {
        Users user = (Users) authentication.getPrincipal();

        return ResponseEntity.ok(ApiResponse.success(
                200,
                "User details fetched successfully",
                userManagementService.fetchCurrentUser(user.getId())
        ));
    }
}