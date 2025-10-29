package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.regex.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
        Pattern.CASE_INSENSITIVE
    );

    @PostMapping
    public ResponseEntity<?> addUser(@RequestBody User user) {
        try {
            if (user.getName() == null || user.getEmail() == null || user.getPassword() == null)
                return ResponseEntity.badRequest().body("Missing required fields");
            if (!EMAIL_PATTERN.matcher(user.getEmail()).matches())
                return ResponseEntity.badRequest().body("Invalid email format");
            if (user.getPassword().length() < 6)
                return ResponseEntity.badRequest().body("Password must be at least 6 characters");
            if (userRepository.findByEmail(user.getEmail()).isPresent())
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
            user.setAdmin(false);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred during signup: " + e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> credentials, HttpSession session) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            if (email == null || password == null)
                return ResponseEntity.badRequest().body("Missing email or password");
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty())
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            User user = userOpt.get();
            if (!password.equals(user.getPassword()))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            session.setAttribute("currentUser", user);
            session.setAttribute("isAdmin", user.isAdmin());
            session.setAttribute("userId", user.getId());
            return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred during login: " + e.getMessage());
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully");
    }
    
    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable int id, @RequestBody Map<String, String> passwordData) {
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");

        if (oldPassword == null || newPassword == null)
            return ResponseEntity.badRequest().body("Missing password fields");
        if (newPassword.length() < 6)
            return ResponseEntity.badRequest().body("New password must be at least 6 characters");
        return userRepository.findById(id)
            .map(user -> {
                if (!oldPassword.equals(user.getPassword()))
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Current password is incorrect");
                user.setPassword(newPassword);
                userRepository.save(user);
                return ResponseEntity.ok("Password updated successfully!");
            })
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }

    @GetMapping
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody User updatedUser) {
        return userRepository.findById(id)
            .map(user -> {
                user.setName(updatedUser.getName());
                user.setEmail(updatedUser.getEmail());
                userRepository.save(user);
                return ResponseEntity.ok("User updated!");
            })
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable int id) {
        if (!userRepository.existsById(id))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted!");
    }

    @PatchMapping("/{id}/admin")
    public ResponseEntity<?> updateAdminStatus(@PathVariable int id, @RequestBody Map<String, Boolean> body) {
        return userRepository.findById(id)
            .map(user -> {
                user.setAdmin(body.getOrDefault("isAdmin", false));
                userRepository.save(user);
                return ResponseEntity.ok("Admin status updated!");
            })
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }
}
