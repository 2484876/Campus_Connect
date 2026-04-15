package com.campusconnect.repository;
import com.campusconnect.entity.User;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    @Query("SELECT u FROM User u WHERE u.isActive = true AND (LOWER(u.name) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(u.department) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<User> searchUsers(@Param("q") String query, Pageable pageable);
    @Query("SELECT u FROM User u WHERE u.department = :dept AND u.isActive = true AND u.id != :uid")
    Page<User> findByDepartment(@Param("dept") String dept, @Param("uid") Long userId, Pageable pageable);
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    Page<User> findByRoleAndIsActiveTrue(@Param("role") String role, Pageable pageable);
}