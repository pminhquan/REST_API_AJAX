package com.hcmute.springboot.controller;

import com.hcmute.springboot.entity.User;
import com.hcmute.springboot.service.IUserService;
import com.hcmute.springboot.util.UploadStorage;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
public class ProfileController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final IUserService userService;
    private final ServletContext servletContext;

    public ProfileController(IUserService userService, ServletContext servletContext) {
        this.userService = userService;
        this.servletContext = servletContext;
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Integer userId = getAuthenticatedUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.findById(userId);
        if (user == null) {
            if (session != null) {
                session.removeAttribute("authenticatedUserId");
            }
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam(value = "fullname", required = false) String fullname,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "images", required = false) MultipartFile imageFile,
            HttpSession session,
            Model model
    ) {
        Integer userId = getAuthenticatedUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.findById(userId);
        if (user == null) {
            if (session != null) {
                session.removeAttribute("authenticatedUserId");
            }
            return "redirect:/login";
        }

        String trimmedFullname = fullname != null ? fullname.trim() : "";
        String trimmedPhone = phone != null ? phone.trim() : "";

        user.setFullname(trimmedFullname);
        user.setPhone(trimmedPhone);

        if (trimmedFullname.isEmpty()) {
            model.addAttribute("user", user);
            model.addAttribute("error", "Full name is required.");
            return "profile";
        }

        if (trimmedFullname.length() > 100) {
            model.addAttribute("user", user);
            model.addAttribute("error", "Full name must not exceed 100 characters.");
            return "profile";
        }

        if (!trimmedPhone.isEmpty()) {
            if (trimmedPhone.length() > 30 || !trimmedPhone.matches("^[+]?[0-9\\s\\-().]{3,30}$")) {
                model.addAttribute("user", user);
                model.addAttribute("error", "Invalid phone number.");
                return "profile";
            }
        }

        String oldImage = user.getImages();
        String finalImage = oldImage;
        String newlyUploadedImage = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            if (imageFile.getSize() > 5L * 1024 * 1024) {
                model.addAttribute("user", user);
                model.addAttribute("error", "Image file exceeds maximum allowed size of 5 MB.");
                return "profile";
            }

            String originalName = imageFile.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                model.addAttribute("user", user);
                model.addAttribute("error", "Invalid image filename.");
                return "profile";
            }

            if (originalName.contains("/") || originalName.contains("\\") || originalName.contains("..")) {
                model.addAttribute("user", user);
                model.addAttribute("error", "Invalid image filename.");
                return "profile";
            }

            String safeFileName = Paths.get(originalName).getFileName().toString();
            int dot = safeFileName.lastIndexOf('.');
            if (dot <= 0 || dot == safeFileName.length() - 1) {
                model.addAttribute("user", user);
                model.addAttribute("error", "Unsupported image type.");
                return "profile";
            }

            String ext = safeFileName.substring(dot + 1).toLowerCase(Locale.ROOT);
            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                model.addAttribute("user", user);
                model.addAttribute("error", "Only JPG, JPEG, PNG and WEBP images are allowed.");
                return "profile";
            }

            String generated = UUID.randomUUID() + "." + ext;
            try (InputStream in = imageFile.getInputStream()) {
                newlyUploadedImage = UploadStorage.storeFile(servletContext, in, "avatars", generated);
                finalImage = newlyUploadedImage;
            } catch (IllegalArgumentException e) {
                model.addAttribute("user", user);
                model.addAttribute("error", e.getMessage());
                return "profile";
            } catch (Exception e) {
                model.addAttribute("user", user);
                model.addAttribute("error", "Unable to save uploaded image.");
                return "profile";
            }
        }

        boolean updated = userService.updateProfile(userId, trimmedFullname, trimmedPhone, finalImage);
        if (!updated) {
            if (newlyUploadedImage != null) {
                UploadStorage.deleteFile(servletContext, newlyUploadedImage);
            }
            user.setImages(oldImage);
            model.addAttribute("user", user);
            model.addAttribute("error", "Failed to update profile.");
            return "profile";
        }

        if (newlyUploadedImage != null && oldImage != null && !oldImage.isBlank()) {
            if (!oldImage.startsWith("http://") && !oldImage.startsWith("https://")) {
                UploadStorage.deleteFile(servletContext, oldImage);
            }
        }

        User updatedUser = userService.findById(userId);
        if (updatedUser != null && session != null) {
            session.setAttribute("account", updatedUser);
        }

        return "redirect:/profile";
    }

    private Integer getAuthenticatedUserId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object val = session.getAttribute("authenticatedUserId");
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return null;
    }
}
