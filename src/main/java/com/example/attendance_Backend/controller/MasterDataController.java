package com.example.attendance_Backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.attendance_Backend.dto.TeacherDTO;
import com.example.attendance_Backend.model.Admin;
import com.example.attendance_Backend.model.ClassMaster;
import com.example.attendance_Backend.model.ClassSubject;
import com.example.attendance_Backend.model.Department;
import com.example.attendance_Backend.model.DivisionMaster;
import com.example.attendance_Backend.model.SubjectMaster;
import com.example.attendance_Backend.model.Teacher;
import com.example.attendance_Backend.repository.AttendanceRepository;
import com.example.attendance_Backend.repository.AttendanceSessionRepository;
import com.example.attendance_Backend.repository.ClassMasterRepository;
import com.example.attendance_Backend.repository.ClassSubjectRepository;
import com.example.attendance_Backend.repository.DepartmentRepository;
import com.example.attendance_Backend.repository.DivisionMasterRepository;
import com.example.attendance_Backend.repository.SubjectMasterRepository;
import com.example.attendance_Backend.repository.TeacherTimetableRepository;
import com.example.attendance_Backend.repository.UserRepository;
import com.example.attendance_Backend.security.AdminContextHolder;

@RestController
@RequestMapping("/api/master")
@CrossOrigin(origins = "*")
public class MasterDataController {

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private ClassMasterRepository classMasterRepository;
    @Autowired
    private DivisionMasterRepository divisionMasterRepository;
    @Autowired
    private SubjectMasterRepository subjectMasterRepository;
    @Autowired
    private ClassSubjectRepository classSubjectRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AttendanceSessionRepository attendanceSessionRepository;
    @Autowired
    private TeacherTimetableRepository teacherTimetableRepository;

    // ==========================================
    // 1. DEPARTMENT CRUD
    // ==========================================
    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getAllDepartments() {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(departmentRepository.findByAdminId(adminId));
    }

    @PostMapping("/departments")
    public ResponseEntity<?> createDepartment(@RequestBody Department department) {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String trimmedName = department.getDepartmentName() != null ? department.getDepartmentName().trim() : "";
        if (trimmedName.isEmpty()) {
            return ResponseEntity.badRequest().body("Department name cannot be empty.");
        }
        department.setDepartmentName(trimmedName);

        if (departmentRepository.findByDepartmentNameIgnoreCaseAndAdminId(trimmedName, adminId).isPresent()) {
            return ResponseEntity.badRequest().body("Department with this name already exists in your organization.");
        }

        Admin admin = new Admin();
        admin.setId(adminId);
        department.setAdmin(admin);

        return ResponseEntity.ok(departmentRepository.save(department));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable int id) {
        if (!classMasterRepository.findByDepartmentId(id).isEmpty() ||
                !subjectMasterRepository.findByDepartmentId(id).isEmpty()) {
            return ResponseEntity.badRequest().body("Cannot delete department. It is linked to classes or subjects.");
        }
        departmentRepository.deleteById(id);
        return ResponseEntity.ok("Deleted successfully.");
    }

    // ==========================================
    // 2. CLASS MASTER CRUD
    // ==========================================
    @GetMapping("/classes")
    public ResponseEntity<List<ClassMaster>> getAllClasses() {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(classMasterRepository.findByAdminId(adminId));
    }

    @PostMapping("/classes")
    public ResponseEntity<?> createClass(@RequestBody ClassMaster classMaster) {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        boolean alreadyExists;
        if (classMaster.getDepartment() != null && classMaster.getDepartment().getId() != 0) {
            alreadyExists = classMasterRepository.findByClassNameAndDepartmentIdAndAdminId(
                    classMaster.getClassName(), classMaster.getDepartment().getId(), adminId).isPresent();
        } else {
            // Check uniqueness for global classes (where department is null)
            alreadyExists = classMasterRepository.findByClassNameAndDepartmentIdAndAdminId(
                    classMaster.getClassName(), null, adminId).isPresent();
        }

        if (alreadyExists) {
            return ResponseEntity.badRequest().body("Class with this name already exists in this department/scope.");
        }

        Admin admin = new Admin();
        admin.setId(adminId);
        classMaster.setAdmin(admin);

        return ResponseEntity.ok(classMasterRepository.save(classMaster));
    }

    @DeleteMapping("/classes/{id}")
    public ResponseEntity<?> deleteClass(@PathVariable int id) {
        if (!divisionMasterRepository.findByClassMasterId(id).isEmpty() ||
                !classSubjectRepository.findByClassMasterId(id).isEmpty()) {
            return ResponseEntity.badRequest().body("Cannot delete class. It is linked to divisions or subjects.");
        }
        classMasterRepository.deleteById(id);
        return ResponseEntity.ok("Deleted successfully.");
    }

    @GetMapping("/classes/{id}/divisions")
    public ResponseEntity<List<DivisionMaster>> getDivisionsByClass(@PathVariable int id) {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(divisionMasterRepository.findByClassMasterIdAndAdminId(id, adminId));
    }

    @GetMapping("/classes/{id}/subjects")
    public ResponseEntity<List<SubjectMaster>> getSubjectsByClass(@PathVariable int id) {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<ClassSubject> mappings = classSubjectRepository.findByClassMasterIdAndAdminId(id, adminId);
        List<SubjectMaster> subjects = mappings.stream().map(ClassSubject::getSubjectMaster)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/classes/name/{className}/divisions")
    public ResponseEntity<List<DivisionMaster>> getDivisionsByClassName(@PathVariable String className) {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<ClassMaster> classes = classMasterRepository.findAllByClassNameAndAdminId(className, adminId);
        if (classes.isEmpty())
            return ResponseEntity.ok(new java.util.ArrayList<>());

        List<Integer> ids = classes.stream().map(ClassMaster::getId).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(divisionMasterRepository.findByClassMasterIdInAndAdminId(ids, adminId));
    }

    @GetMapping("/classes/name/{className}/subjects")
    public ResponseEntity<List<SubjectMaster>> getSubjectsByClassName(@PathVariable String className) {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<ClassMaster> classes = classMasterRepository.findAllByClassNameAndAdminId(className, adminId);
        if (classes.isEmpty())
            return ResponseEntity.ok(new java.util.ArrayList<>());

        List<Integer> ids = classes.stream().map(ClassMaster::getId).collect(java.util.stream.Collectors.toList());
        List<ClassSubject> mappings = classSubjectRepository.findByClassMasterIdInAndAdminId(ids, adminId);
        List<SubjectMaster> subjects = mappings.stream().map(ClassSubject::getSubjectMaster)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(subjects);
    }

    // ==========================================
    // 3. DIVISION MASTER CRUD
    // ==========================================
    @GetMapping("/divisions")
    public ResponseEntity<List<DivisionMaster>> getAllDivisions() {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(divisionMasterRepository.findByAdminId(adminId));
    }

    @PostMapping("/divisions")
    public ResponseEntity<?> createDivision(@RequestBody DivisionMaster divisionMaster) {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (divisionMasterRepository.findByDivisionNameAndAdminId(divisionMaster.getDivisionName(), adminId)
                .isPresent()) {
            // Check if it's the same class too? Usually divisions are unique per class.
            // But let's check global uniqueness for simplicity if that's the requirement.
            // Actually, natural constraint is per class.
            if (divisionMasterRepository.findByDivisionNameAndClassMasterIdAndAdminId(
                    divisionMaster.getDivisionName(), divisionMaster.getClassMaster().getId(), adminId).isPresent()) {
                return ResponseEntity.badRequest().body("Division with this name already exists for this class.");
            }
        }

        Admin admin = new Admin();
        admin.setId(adminId);
        divisionMaster.setAdmin(admin);

        return ResponseEntity.ok(divisionMasterRepository.save(divisionMaster));
    }

    @DeleteMapping("/divisions/{id}")
    public ResponseEntity<?> deleteDivision(@PathVariable int id) {
        if (userRepository.countByDivisionMaster_Id(id) > 0) {
            return ResponseEntity.badRequest().body("Cannot delete division. It is linked to students.");
        }
        if (attendanceRepository.countByDivisionMaster_Id(id) > 0) {
            return ResponseEntity.badRequest().body("Cannot delete division. It is linked to attendance records.");
        }
        if (attendanceSessionRepository.countByDivisionMaster_Id(id) > 0) {
            return ResponseEntity.badRequest()
                    .body("Cannot delete division. It is linked to active attendance sessions.");
        }
        if (teacherTimetableRepository.countByDivisionMaster_Id(id) > 0) {
            return ResponseEntity.badRequest().body("Cannot delete division. It is linked to teacher timetables.");
        }
        divisionMasterRepository.deleteById(id);
        return ResponseEntity.ok("Deleted successfully.");
    }

    // ==========================================
    // 4. SUBJECT MASTER CRUD
    // ==========================================
    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectMaster>> getAllSubjects() {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(subjectMasterRepository.findByAdminId(adminId));
    }

    @PostMapping("/subjects")
    public ResponseEntity<?> createSubject(@RequestBody SubjectMaster subjectMaster) {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (subjectMasterRepository.findBySubjectNameAndAdminId(subjectMaster.getSubjectName(), adminId).isPresent()) {
            return ResponseEntity.badRequest().body("Subject with this name already exists in your organization.");
        }

        Admin admin = new Admin();
        admin.setId(adminId);
        subjectMaster.setAdmin(admin);

        return ResponseEntity.ok(subjectMasterRepository.save(subjectMaster));
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable int id) {
        if (!classSubjectRepository.findBySubjectMasterId(id).isEmpty()) {
            return ResponseEntity.badRequest().body("Cannot delete subject. It is linked to class mappings.");
        }
        if (attendanceRepository.countBySubjectMaster_Id(id) > 0) {
            return ResponseEntity.badRequest().body("Cannot delete subject. It is linked to attendance records.");
        }
        if (attendanceSessionRepository.countBySubjectMaster_Id(id) > 0) {
            return ResponseEntity.badRequest()
                    .body("Cannot delete subject. It is linked to active attendance sessions.");
        }
        if (teacherTimetableRepository.countBySubjectMaster_Id(id) > 0) {
            return ResponseEntity.badRequest().body("Cannot delete subject. It is linked to teacher timetables.");
        }
        subjectMasterRepository.deleteById(id);
        return ResponseEntity.ok("Deleted successfully.");
    }

    // ==========================================
    // 5. CLASS-SUBJECT MAPPING CRUD
    // ==========================================
    @GetMapping("/class-subjects")
    public List<ClassSubject> getAllClassSubjects() {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return java.util.Collections.emptyList();
        return classSubjectRepository.findByAdminId(adminId);
    }

    @PostMapping("/class-subjects")
    public ResponseEntity<?> createClassSubjectMapping(@RequestBody ClassSubject classSubject) {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (classSubjectRepository
                .findByClassMasterIdAndSubjectMasterIdAndAdminId(classSubject.getClassMaster().getId(),
                        classSubject.getSubjectMaster().getId(), adminId)
                .isPresent()) {
            return ResponseEntity.badRequest().body("Mapping already exists");
        }

        Admin admin = new Admin();
        admin.setId(adminId);
        classSubject.setAdmin(admin);

        return ResponseEntity.ok(classSubjectRepository.save(classSubject));
    }

    @DeleteMapping("/class-subjects/{id}")
    public ResponseEntity<?> deleteClassSubjectMapping(@PathVariable int id) {
        classSubjectRepository.deleteById(id);
        return ResponseEntity.ok("Deleted successfully.");
    }

    @GetMapping("/classes/{classId}/divisions/{divisionId}/teachers")
    public List<TeacherDTO> getTeachersByClassAndDivision(@PathVariable int classId, @PathVariable int divisionId) {
        Long adminId = AdminContextHolder.getAdminId();
        if (adminId == null)
            return java.util.Collections.emptyList();

        List<Teacher> teachers = teacherTimetableRepository
                .findDistinctTeachersByClassMasterIdAndDivisionMasterIdAndAdminId(
                        classId,
                        divisionId,
                        adminId);
        return teachers.stream()
                .map(t -> new TeacherDTO(t.getId(), t.getName()))
                .collect(java.util.stream.Collectors.toList());
    }
}
