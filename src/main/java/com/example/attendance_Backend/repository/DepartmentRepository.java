package com.example.attendance_Backend.repository;

import com.example.attendance_Backend.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    List<Department> findByAdminId(Long adminId);

    @Query("SELECT d FROM Department d WHERE LOWER(TRIM(d.departmentName)) = LOWER(TRIM(:name)) AND d.admin.id = :adminId")
    Optional<Department> findByDepartmentNameIgnoreCaseAndAdminId(@Param("name") String name, @Param("adminId") Long adminId);
}
