package com.hcmute.springboot.repository;

import com.hcmute.springboot.entity.Role;
import com.hcmute.springboot.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countByRoleAndActiveTrue(Role role);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.active = true ORDER BY u.id ASC")
    List<User> findActiveUsersForUpdate(@Param("role") Role role);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(u.fullname) LIKE LOWER(CONCAT('%', :kw, '%'))")
    Page<User> searchUsers(@Param("kw") String keyword, Pageable pageable);
}
