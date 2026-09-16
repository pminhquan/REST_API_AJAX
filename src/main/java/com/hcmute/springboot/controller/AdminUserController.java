package com.hcmute.springboot.controller;

import com.hcmute.springboot.entity.Role;
import com.hcmute.springboot.entity.User;
import com.hcmute.springboot.service.IUserService;
import com.hcmute.springboot.util.UploadStorage;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Controller
public class AdminUserController {

    public static final int MAX_PAGE_SIZE = 50;
    public static final int DEFAULT_PAGE_SIZE = 10;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final IUserService userService;
    private final ServletContext servletContext;

    public AdminUserController(IUserService userService, ServletContext servletContext) {
        this.userService = userService;
        this.servletContext = servletContext;
    }

    @GetMapping("/admin/users")
    public String listUsers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        if (page <= 0) page = 1;
        if (size <= 0) {
            size = DEFAULT_PAGE_SIZE;
        } else if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        Page<User> userPage = userService.searchUsers(
                keyword, PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"))
        );

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalUsers", userPage.getTotalElements());
        model.addAttribute("keyword", keyword != null ? keyword : "");

        return "admin/user-list";
    }

    @GetMapping("/admin/users/add")
    public String showAddForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("action", "add");
        return "admin/user-form";
    }

    @PostMapping("/admin/users/add")
    public String createUser(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "fullname", required = false) String fullname,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "role", defaultValue = "CUSTOMER") String roleStr,
            @RequestParam(value = "active", defaultValue = "false") boolean active,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            Model model
    ) {
        if (isEmpty(username) || isEmpty(email) || isEmpty(password)) {
            model.addAttribute("error", "Username, email, and password are required.");
            model.addAttribute("action", "add");
            return "admin/user-form";
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            model.addAttribute("error", "Please enter a valid email address.");
            model.addAttribute("action", "add");
            return "admin/user-form";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            model.addAttribute("action", "add");
            return "admin/user-form";
        }

        String storedImage = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                storedImage = storeAvatar(imageFile);
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
                model.addAttribute("action", "add");
                return "admin/user-form";
            }
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setFullname(fullname != null ? fullname.trim() : "");
        user.setPhone(phone != null ? phone.trim() : "");
        user.setRole("ADMIN".equalsIgnoreCase(roleStr) ? Role.ADMIN : Role.CUSTOMER);
        user.setActive(active);
        user.setImages(storedImage != null ? storedImage : "");

        try {
            userService.createAdminUser(user, password);
            return "redirect:/admin/users?message=add_success";
        } catch (Exception e) {
            if (storedImage != null) {
                UploadStorage.deleteFile(servletContext, storedImage);
            }
            model.addAttribute("error", e.getMessage());
            model.addAttribute("action", "add");
            return "admin/user-form";
        }
    }

    @GetMapping("/admin/users/edit")
    public String showEditForm(@RequestParam(value = "id", required = false) String idStr, Model model) {
        if (idStr == null || idStr.trim().isEmpty()) {
            return "redirect:/admin/users";
        }
        try {
            int id = Integer.parseInt(idStr.trim());
            User user = userService.findById(id);
            if (user == null) {
                return "redirect:/admin/users";
            }
            model.addAttribute("user", user);
            model.addAttribute("action", "edit");
            return "admin/user-form";
        } catch (NumberFormatException e) {
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/admin/users/edit")
    public String updateUser(
            @RequestParam(value = "id", required = false) String idStr,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "fullname", required = false) String fullname,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "role", defaultValue = "CUSTOMER") String roleStr,
            @RequestParam(value = "active", defaultValue = "false") boolean active,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            HttpSession session,
            Model model
    ) {
        if (idStr == null || idStr.trim().isEmpty()) {
            return "redirect:/admin/users";
        }
        int id;
        try {
            id = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            return "redirect:/admin/users";
        }

        User existing = userService.findById(id);
        if (existing == null) {
            return "redirect:/admin/users";
        }

        if (isEmpty(username) || isEmpty(email)) {
            model.addAttribute("user", existing);
            model.addAttribute("error", "Username and email are required.");
            model.addAttribute("action", "edit");
            return "admin/user-form";
        }

        String oldImage = existing.getImages();
        String storedImage = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                storedImage = storeAvatar(imageFile);
            } catch (Exception e) {
                model.addAttribute("user", existing);
                model.addAttribute("error", e.getMessage());
                model.addAttribute("action", "edit");
                return "admin/user-form";
            }
        }

        existing.setUsername(username.trim());
        existing.setEmail(email.trim());
        existing.setFullname(fullname != null ? fullname.trim() : "");
        existing.setPhone(phone != null ? phone.trim() : "");
        existing.setRole("ADMIN".equalsIgnoreCase(roleStr) ? Role.ADMIN : Role.CUSTOMER);
        existing.setActive(active);
        if (storedImage != null) {
            existing.setImages(storedImage);
        }

        Integer currentAuthUserId = getCurrentAuthUserId(session);
        try {
            userService.updateAdminUser(existing, (password != null && !password.isBlank()) ? password : null, currentAuthUserId);
            if (storedImage != null && oldImage != null && !oldImage.isBlank()) {
                if (!oldImage.startsWith("http://") && !oldImage.startsWith("https://")) {
                    UploadStorage.deleteFile(servletContext, oldImage);
                }
            }
            return "redirect:/admin/users?message=update_success";
        } catch (Exception e) {
            if (storedImage != null) {
                UploadStorage.deleteFile(servletContext, storedImage);
            }
            model.addAttribute("user", existing);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("action", "edit");
            return "admin/user-form";
        }
    }

    @PostMapping("/admin/users/delete")
    public String deleteUserPost(@RequestParam(value = "id", required = false) String idStr, HttpSession session) {
        return deleteUser(idStr, session);
    }

    private String deleteUser(String idStr, HttpSession session) {
        if (idStr == null || idStr.trim().isEmpty()) {
            return "redirect:/admin/users?error=invalid_id";
        }
        int id;
        try {
            id = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            return "redirect:/admin/users?error=invalid_id";
        }

        Integer currentAuthUserId = getCurrentAuthUserId(session);

        try {
            boolean success = userService.deleteUser(id, currentAuthUserId);
            if (success) {
                return "redirect:/admin/users?message=delete_success";
            } else {
                return "redirect:/admin/users?error=delete_failed";
            }
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("own account")) {
                return "redirect:/admin/users?error=cannot_delete_self";
            }
            if (e.getMessage() != null && e.getMessage().contains("last active administrator")) {
                return "redirect:/admin/users?error=cannot_delete_last_admin";
            }
            return "redirect:/admin/users?error=delete_failed";
        }
    }

    private Integer getCurrentAuthUserId(HttpSession session) {
        if (session != null) {
            Object currentUserId = session.getAttribute("authenticatedUserId");
            if (currentUserId instanceof Number) {
                return ((Number) currentUserId).intValue();
            }
        }
        return null;
    }

    private String storeAvatar(MultipartFile file) throws Exception {
        if (file.getSize() > 5L * 1024 * 1024) {
            throw new IllegalArgumentException("Image file exceeds maximum allowed size of 5 MB.");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("Image file cannot be empty.");
        }
        if (originalName.contains("/") || originalName.contains("\\") || originalName.contains("..")) {
            throw new IllegalArgumentException("Invalid image filename.");
        }

        String safeFileName = Paths.get(originalName).getFileName().toString();
        int dot = safeFileName.lastIndexOf('.');
        if (dot <= 0 || dot == safeFileName.length() - 1) {
            throw new IllegalArgumentException("Unsupported image type.");
        }
        String ext = safeFileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Only JPG, JPEG, PNG and WEBP images are allowed.");
        }

        String generated = UUID.randomUUID() + "." + ext;
        try (InputStream in = file.getInputStream()) {
            return UploadStorage.storeFile(servletContext, in, "avatars", generated);
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
