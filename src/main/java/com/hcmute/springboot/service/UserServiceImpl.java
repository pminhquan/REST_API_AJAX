package com.hcmute.springboot.service;

import com.hcmute.springboot.entity.Role;
import com.hcmute.springboot.entity.User;
import com.hcmute.springboot.repository.OtpTokenRepository;
import com.hcmute.springboot.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;

    public UserServiceImpl(UserRepository userRepository, OtpTokenRepository otpTokenRepository) {
        this.userRepository = userRepository;
        this.otpTokenRepository = otpTokenRepository;
    }

    @Override
    public User register(String username, String email, String plaintextPassword) {
        if (existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        String hashedPassword = BCrypt.hashpw(plaintextPassword, BCrypt.gensalt());
        User user = new User(username, email, hashedPassword);
        user.setRole(Role.CUSTOMER);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        return userRepository.findByUsername(username.trim()).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return userRepository.findByEmail(email.trim()).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByUsername(username.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByEmail(email.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyPassword(User user, String plaintextPassword) {
        if (user == null || user.getPasswordHash() == null || plaintextPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plaintextPassword, user.getPasswordHash());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean activateUser(int id) {
        User user = findById(id);
        if (user != null) {
            user.setActive(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean updatePassword(int id, String newPlaintextPassword) {
        User user = findById(id);
        if (user != null) {
            String hashedPassword = BCrypt.hashpw(newPlaintextPassword, BCrypt.gensalt());
            user.setPasswordHash(hashedPassword);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateProfile(int userId, String fullname, String phone, String images) {
        User user = findById(userId);
        if (user != null) {
            user.setFullname(fullname);
            user.setPhone(phone);
            user.setImages(images);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public long countAllUsers() {
        return userRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.searchUsers(keyword.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getUsersPage(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User createAdminUser(User user, String plaintextPassword) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (plaintextPassword == null || plaintextPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        String hashedPassword = BCrypt.hashpw(plaintextPassword, BCrypt.gensalt());
        user.setPasswordHash(hashedPassword);
        return userRepository.save(user);
    }

    @Override
    public User updateAdminUser(User user, String optionalNewPassword) {
        return updateAdminUser(user, optionalNewPassword, null);
    }

    @Override
    public User updateAdminUser(User user, String optionalNewPassword, Integer currentAuthUserId) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        User existing = findById(user.getId());
        if (existing == null) {
            throw new IllegalArgumentException("User does not exist");
        }

        // Protect invariants against self-demotion and self-deactivation
        if (currentAuthUserId != null && currentAuthUserId.equals(user.getId())) {
            if (user.getRole() != null && user.getRole() != Role.ADMIN && existing.getRole() == Role.ADMIN) {
                throw new IllegalStateException("Cannot demote your own account.");
            }
            if (!user.isActive() && existing.isActive()) {
                throw new IllegalStateException("Cannot deactivate your own account.");
            }
        }

        // Protect invariant against demoting or deactivating the last active ADMIN
        if (existing.getRole() == Role.ADMIN && existing.isActive()) {
            boolean roleChangedAway = user.getRole() != null && user.getRole() != Role.ADMIN;
            boolean deactivated = !user.isActive();
            if (roleChangedAway || deactivated) {
                List<User> activeAdmins = userRepository.findActiveUsersForUpdate(Role.ADMIN);
                if (activeAdmins.size() <= 1) {
                    throw new IllegalStateException("Cannot demote or deactivate the last active administrator.");
                }
            }
        }

        // Check username uniqueness if changed
        if (!existing.getUsername().equalsIgnoreCase(user.getUsername()) && existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        // Check email uniqueness if changed
        if (!existing.getEmail().equalsIgnoreCase(user.getEmail()) && existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        existing.setUsername(user.getUsername());
        existing.setEmail(user.getEmail());
        existing.setFullname(user.getFullname());
        existing.setPhone(user.getPhone());
        existing.setRole(user.getRole() != null ? user.getRole() : existing.getRole());
        existing.setActive(user.isActive());
        if (user.getImages() != null && !user.getImages().isBlank()) {
            existing.setImages(user.getImages());
        }
        if (optionalNewPassword != null && !optionalNewPassword.isBlank()) {
            if (optionalNewPassword.length() < 6) {
                throw new IllegalArgumentException("Password must be at least 6 characters");
            }
            existing.setPasswordHash(BCrypt.hashpw(optionalNewPassword, BCrypt.gensalt()));
        }
        return userRepository.save(existing);
    }

    @Override
    public boolean deleteUser(int id) {
        return deleteUser(id, null);
    }

    @Override
    public boolean deleteUser(int id, Integer currentAuthUserId) {
        if (currentAuthUserId != null && currentAuthUserId == id) {
            throw new IllegalStateException("Cannot delete your own account.");
        }

        User target = userRepository.findById(id).orElse(null);
        if (target == null) {
            return false;
        }

        if (target.getRole() == Role.ADMIN && target.isActive()) {
            List<User> activeAdmins = userRepository.findActiveUsersForUpdate(Role.ADMIN);
            if (activeAdmins.size() <= 1) {
                throw new IllegalStateException("Cannot delete the last active administrator.");
            }
        }

        // Delete dependent OTP tokens first in same transaction
        otpTokenRepository.deleteByUser(target);
        userRepository.delete(target);
        return true;
    }
}
