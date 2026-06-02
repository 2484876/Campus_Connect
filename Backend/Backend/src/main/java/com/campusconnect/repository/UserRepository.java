package com.campusconnect.repository;
import com.campusconnect.entity.User;
import com.campusconnect.enums.Role;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
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

    @Query(value = "SELECT * FROM users WHERE birthday IS NOT NULL AND MONTH(birthday) = ?1 AND DAY(birthday) = ?2 AND is_active = 1", nativeQuery = true)
    List<User> findByBirthdayMonthAndDay(int month, int day);

    @Query(value = "SELECT * FROM users WHERE work_anniversary IS NOT NULL AND MONTH(work_anniversary) = ?1 AND DAY(work_anniversary) = ?2 AND is_active = 1", nativeQuery = true)
    List<User> findByWorkAnniversaryMonthAndDay(int month, int day);

    @Query("SELECT u FROM User u WHERE (:q IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%'))) AND (:active IS NULL OR u.isActive = :active) AND (:role IS NULL OR u.role = :role) ORDER BY u.createdAt DESC")
    Page<User> adminSearch(@Param("q") String q, @Param("active") Boolean active, @Param("role") Role role, Pageable pageable);

    long countByIsActiveTrue();
    long countByRole(Role role);
    long countByRoleAndIsActiveTrue(Role role);
    long countByCreatedAtAfter(LocalDateTime since);
}