package iprwc_backend.controller;



import iprwc_backend.dto.request.UserRequest;
import iprwc_backend.dto.response.UserResponse;
import iprwc_backend.entity.UserRole;
import iprwc_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Get user by ID - Users can access their own profile, admins can access any
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id, Authentication authentication) {
        String currentUserEmail = authentication.getName();
        UserResponse currentUser = userService.getUserByEmail(currentUserEmail);

        // Allow if requesting own profile OR if admin
        if (currentUser.getId().equals(id) || currentUser.getRole().equals("ADMIN")) {
            UserResponse user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Get all users - Admin only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get users by role - Admin only
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable UserRole role) {
        List<UserResponse> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    // Update user - Users can update their own profile, admins can update any
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest userRequest,
            Authentication authentication) {

        String currentUserEmail = authentication.getName();
        UserResponse currentUser = userService.getUserByEmail(currentUserEmail);

        // Allow if updating own profile OR if admin
        if (currentUser.getId().equals(id) || currentUser.getRole().equals("ADMIN")) {
            UserResponse updatedUser = userService.updateUser(id, userRequest);
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Toggle user role - Admin only
    @PutMapping("/{id}/toggle-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> toggleUserRole(@PathVariable Long id) {
        UserResponse updatedUser = userService.toggleUserRole(id);
        return ResponseEntity.ok(updatedUser);
    }

    // Delete user - Admin only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}