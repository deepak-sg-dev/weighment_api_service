package com.suguna.weighment_api_service.admin.controller;


import com.suguna.weighment_api_service.admin.dtos.responses.UserFilterDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.FirebaseTokenDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UserRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UserResponseDTO;
import com.suguna.weighment_api_service.admin.response.ApiResponse;
import com.suguna.weighment_api_service.admin.response.Response;
import com.suguna.weighment_api_service.admin.services.UserService;
import com.suguna.weighment_api_service.admin.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User API")
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private UserUtils userUtils;

    /*@Operation(summary = "Create user")
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(
            @RequestBody UserRequestDTO dto) {

        System.out.println("User Called");

        return ResponseEntity.ok(service.createUser(dto));
    }*/



    @Operation(summary = "Create user")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(
            @RequestBody UserRequestDTO dto) {

        System.out.println("User Called");

        UserResponseDTO userResponseDTO = service.createUser(dto);

        return Response.success("User created successfully", userResponseDTO);

    }

    @Operation(summary = "Update user")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @RequestBody UserRequestDTO dto) {

        return ResponseEntity.ok(service.updateUser(id, dto));
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.getUserById(id));
    }

    @Operation(summary = "Get all users")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {

        return Response.success("User fetched successfully", service.getAllUsers());

    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {


        return Response.success("User deactivated", service.deleteUser(id));

    }


    @Operation(summary = "Fetch users with Filter")
    @PostMapping("/search")
    public ResponseEntity<?> getUsers(@RequestBody UserFilterDTO filter) {

        return Response.success(
                "Users fetched successfully",
                service.getUsers(filter)
        );
    }

    @PostMapping("/update-firebase-token")
    @Operation(summary = "Update Firebase Token")
    public ResponseEntity<?> updateFirebaseToken(
            @RequestBody FirebaseTokenDTO dto) {

        service.updateFirebaseToken(
                userUtils.getCurrentUserId(),
                dto.getFirebaseToken()
        );

        return Response.success("Token updated", null);
    }

}

