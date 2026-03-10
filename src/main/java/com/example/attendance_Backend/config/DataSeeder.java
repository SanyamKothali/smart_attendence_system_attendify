package com.example.attendance_Backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.attendance_Backend.model.Admin;
import com.example.attendance_Backend.repository.AdminRepository;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initData(AdminRepository adminRepository,
            com.example.attendance_Backend.repository.UserRepository userRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return args -> {
            // Seed Admin User
            try {
                adminRepository.findByEmail("admin@attendify.com").ifPresentOrElse(
                    admin -> {
                        if (!"MFNGVL".equals(admin.getSchoolCode())) {
                            // Only update if no other admin has this code to avoid Duplicate Entry crash
                            if (adminRepository.findBySchoolCode("MFNGVL").isEmpty()) {
                                admin.setSchoolCode("MFNGVL");
                                adminRepository.save(admin);
                                System.out.println("✅ Updated existing admin school code to: MFNGVL");
                            } else {
                                System.out.println("ℹ️ School code MFNGVL is already assigned to another admin.");
                            }
                        }
                    },
                    () -> {
                        if (adminRepository.findBySchoolCode("MFNGVL").isEmpty()) {
                            Admin admin = new Admin();
                            admin.setName("Admin Director");
                            admin.setEmail("admin@attendify.com");
                            admin.setPassword(passwordEncoder.encode("admin123"));
                            admin.setSchoolCode("MFNGVL");
                            adminRepository.save(admin);
                            System.out.println("✅ Seeded default admin account: admin@attendify.com / admin123");
                        } else {
                            System.out.println("ℹ️ Skipping admin seed: School code MFNGVL already exists.");
                        }
                    }
                );
            } catch (Exception e) {
                System.err.println("❌ Error during DataSeeding: " + e.getMessage());
            }
        };
    }
}
