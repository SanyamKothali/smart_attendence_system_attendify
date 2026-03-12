package com.example.attendance_Backend.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.attendance_Backend.model.Admin;
import com.example.attendance_Backend.model.User;
import com.example.attendance_Backend.repository.UserRepository;
import com.example.attendance_Backend.security.AdminContextHolder;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordService passwordService;
    @Autowired
    private com.example.attendance_Backend.repository.AdminRepository adminRepository;

    public User registerUser(User user) {
        System.out.println("Processing registration for: " + user.getEmail());
        Long adminId = AdminContextHolder.getAdminId();

        if (adminId != null) {
            System.out.println("Registration using Admin Context ID: " + adminId);
            Admin admin = new Admin();
            admin.setId(adminId);
            user.setAdmin(admin);
        } else if (user.getSchoolCode() != null && !user.getSchoolCode().isBlank()) {
            String trimmedSchoolCode = user.getSchoolCode().trim();
            System.out.println("Registration using School Code: [" + trimmedSchoolCode + "]");
            Admin admin = adminRepository.findBySchoolCode(trimmedSchoolCode)
                    .orElseThrow(() -> {
                        System.err.println("❌ Invalid School Code: [" + trimmedSchoolCode + "]");
                        return new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Invalid School Code");
                    });
            user.setAdmin(admin);
            System.out.println("✅ Found admin for school code: " + admin.getEmail());
        } else if (user.getAdmin() == null) {
            System.err.println("❌ Missing Admin context or School Code");
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Admin context or School Code required for student registration");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            System.err.println("❌ Email already registered: " + user.getEmail());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email already registered");
        }
        user.setPassword(passwordService.encode(user.getPassword()));
        user.setRole("STUDENT"); // Standardize role
        User saved = userRepository.save(user);
        System.out.println("✅ Successfully registered student: " + saved.getEmail());
        return saved;
    }

    public Optional<User> loginUser(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> passwordService.matches(password, user.getPassword()));
    }

    public User updateClassAndDivision(int userId, com.example.attendance_Backend.model.ClassMaster classMaster,
            com.example.attendance_Backend.model.DivisionMaster divisionMaster) {

        Long adminId = AdminContextHolder.getAdminId();
        User user;

        if (adminId != null) {
            user = userRepository.findByIdAndAdminId(userId, adminId)
                    .orElseThrow(() -> new RuntimeException("User not found or unauthorized"));
        } else {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        user.setClassMaster(classMaster);
        user.setDivisionMaster(divisionMaster);
        return userRepository.save(user);
    }

    public void resetPassword(String email, String mobile, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        if (user.getMobilenumber() == null || !user.getMobilenumber().equals(mobile)) {
            throw new RuntimeException("Mobile number does not match our records");
        }

        user.setPassword(passwordService.encode(newPassword));
        userRepository.save(user);
    }
}
