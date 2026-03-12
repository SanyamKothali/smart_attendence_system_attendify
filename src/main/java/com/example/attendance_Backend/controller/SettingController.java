package com.example.attendance_Backend.controller;

import com.example.attendance_Backend.model.Setting;
import com.example.attendance_Backend.repository.SettingRepository;
import org.springframework.web.bind.annotation.*;

import com.example.attendance_Backend.security.AdminContextHolder;
import com.example.attendance_Backend.model.Admin;
import com.example.attendance_Backend.repository.AdminRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin
public class SettingController {

    private final SettingRepository settingRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public SettingController(SettingRepository settingRepository, AdminRepository adminRepository,
            PasswordEncoder passwordEncoder) {
        this.settingRepository = settingRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Save or Update Settings
    @PostMapping("/save")
    public ResponseEntity<Setting> saveSettings(@RequestBody Setting newSetting) {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Setting existing = settingRepository.findByAdminId(adminId).orElse(new Setting());

        Admin admin = new Admin();
        admin.setId(adminId);
        existing.setAdmin(admin);

        existing.setAttendanceThreshold(newSetting.getAttendanceThreshold());
        existing.setLateArrivalMinutes(newSetting.getLateArrivalMinutes());
        existing.setAutoMarkAbsentMinutes(newSetting.getAutoMarkAbsentMinutes());
        existing.setManualOverride(newSetting.isManualOverride());
        existing.setSendAlerts(newSetting.isSendAlerts());

        if (existing.getId() == null) {
            // If it's a new record, we might need a unique ID strategy if ID isn't
            // auto-generated
            // But let's assume save handles it or we use adminId as ID if OneToOne
            existing.setId(adminId);
        }

        return ResponseEntity.ok(settingRepository.save(existing));
    }

    // Get Settings
    @GetMapping
    public ResponseEntity<Setting> getSettings() {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(settingRepository.findByAdminId(adminId)
                .orElseGet(() -> {
                    Setting defaultSetting = new Setting();
                    defaultSetting.setId(adminId);
                    Admin admin = new Admin();
                    admin.setId(adminId);
                    defaultSetting.setAdmin(admin);
                    defaultSetting.setAttendanceThreshold(75);
                    defaultSetting.setLateArrivalMinutes(10);
                    defaultSetting.setAutoMarkAbsentMinutes(30);
                    defaultSetting.setManualOverride(false);
                    defaultSetting.setSendAlerts(false);
                    return settingRepository.save(defaultSetting);
                }));
    }

    // Get Admin Profile
    @GetMapping("/profile")
    public ResponseEntity<Admin> getProfile() {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return adminRepository.findById(adminId)
                .map(admin -> {
                    admin.setPassword(null); // Safety: don't send password
                    return ResponseEntity.ok(admin);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Update Admin Profile (Name and/or Password)
    @PostMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> updates) {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return adminRepository.findById(adminId)
                .map(admin -> {
                    if (updates.containsKey("name")) {
                        admin.setName(updates.get("name"));
                    }
                    if (updates.containsKey("password") && updates.get("password") != null
                            && !updates.get("password").isEmpty()) {
                        admin.setPassword(passwordEncoder.encode(updates.get("password")));
                    }
                    adminRepository.save(admin);
                    admin.setPassword(null);
                    return ResponseEntity.ok(admin);
                })
                .orElse(ResponseEntity.notFound().build());
    }

}
