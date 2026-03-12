package com.example.attendance_Backend.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.attendance_Backend.model.Admin;
import com.example.attendance_Backend.model.Teacher;
import com.example.attendance_Backend.repository.AdminRepository;
import com.example.attendance_Backend.repository.TeacherRepository;

@Component
public class OrphanedTeacherFixer implements CommandLineRunner {

    private final TeacherRepository teacherRepository;
    private final AdminRepository adminRepository;

    public OrphanedTeacherFixer(TeacherRepository teacherRepository, AdminRepository adminRepository) {
        this.teacherRepository = teacherRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- ORPHANED TEACHER FIX START ---");
        
        Admin mainAdmin = adminRepository.findBySchoolCode("MFNGVL")
                .orElse(null);

        if (mainAdmin == null) {
            System.err.println("❌ Could not find Main Admin with school code MFNGVL. Skipping data fix.");
            return;
        }

        List<Teacher> orphanedTeachers = teacherRepository.findAll().stream()
                .filter(t -> t.getAdmin() == null)
                .toList();

        System.out.println("Found " + orphanedTeachers.size() + " orphaned teachers.");

        for (Teacher t : orphanedTeachers) {
            t.setAdmin(mainAdmin);
            teacherRepository.save(t);
            System.out.println("✅ Linked Teacher [" + t.getEmail() + "] to Admin [" + mainAdmin.getEmail() + "]");
        }
        
        System.out.println("--- ORPHANED TEACHER FIX END ---");
    }
}
