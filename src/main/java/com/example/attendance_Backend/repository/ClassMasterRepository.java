package com.example.attendance_Backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.attendance_Backend.model.ClassMaster;

@Repository
public interface ClassMasterRepository extends JpaRepository<ClassMaster, Integer> {
    List<ClassMaster> findByAdminId(Long adminId);

    Optional<ClassMaster> findByIdAndAdminId(Integer id, Long adminId);

    long countByAdminId(Long adminId);

    List<ClassMaster> findByDepartmentIdAndAdminId(Integer departmentId, Long adminId);

    Optional<ClassMaster> findByClassNameAndAdminId(String className, Long adminId);
 
    List<ClassMaster> findAllByClassNameAndAdminId(String className, Long adminId);

    Optional<ClassMaster> findByClassNameAndDepartmentIdAndAdminId(String className, Integer departmentId, Long adminId);

    // Legacy methods during migration
    List<ClassMaster> findByDepartmentId(Integer departmentId);

    Optional<ClassMaster> findByClassName(String className);
}
