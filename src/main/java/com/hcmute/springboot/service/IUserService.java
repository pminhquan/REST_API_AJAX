package com.hcmute.springboot.service;

import com.hcmute.springboot.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {

    User register(String username, String email, String plaintextPassword);

    User findById(int id);

    User findByUsername(String username);

    User findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean verifyPassword(User user, String plaintextPassword);

    boolean activateUser(int id);

    boolean updatePassword(int id, String newPlaintextPassword);

    boolean updateProfile(int userId, String fullname, String phone, String images);

    long countAllUsers();

    // Admin User Management
    Page<User> searchUsers(String keyword, Pageable pageable);

    Page<User> getUsersPage(Pageable pageable);

    User createAdminUser(User user, String plaintextPassword);

    User updateAdminUser(User user, String optionalNewPassword);

    User updateAdminUser(User user, String optionalNewPassword, Integer currentAuthUserId);

    boolean deleteUser(int id);

    boolean deleteUser(int id, Integer currentAuthUserId);
}
