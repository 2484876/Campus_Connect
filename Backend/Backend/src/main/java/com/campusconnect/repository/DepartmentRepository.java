package com.campusconnect.repository;
import com.campusconnect.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);
}