package com.hcmute.springboot.controller;

import com.hcmute.springboot.entity.User;
import com.hcmute.springboot.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ResetPasswordController {

    private final IUserService userService;

    public ResetPasswordController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/reset-password")
    public String showResetPassword(HttpSession session) {
        if (!validateResetAuth(session)) {
            invalidateAuthSession(session);
            return "redirect:/forgot-password";
        }
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            HttpSession session,
            Model model
    ) {
        if (!validateResetAuth(session)) {
            invalidateAuthSession(session);
            return "redirect:/forgot-password";
        }

        if (password == null || password.trim().isEmpty() || confirmPassword == null || confirmPassword.trim().isEmpty()) {
            model.addAttribute("error", "Passwords cannot be blank.");
            return "reset-password";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "reset-password";
        }

        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            return "reset-password";
        }

        String email = (String) session.getAttribute("resetEmail");
        User user = userService.findByEmail(email);
        if (user == null) {
            model.addAttribute("error", "Invalid user mapping.");
            return "reset-password";
        }

        try {
            boolean updated = userService.updatePassword(user.getId(), password);
            if (updated) {
                invalidateAuthSession(session);
                return "redirect:/login?message=Password reset successful. Please login.";
            } else {
                model.addAttribute("error", "Failed to update password.");
                return "reset-password";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Unable to reset password. Please try again.");
            return "reset-password";
        }
    }

    private boolean validateResetAuth(HttpSession session) {
        if (session == null) {
            return false;
        }
        String email = (String) session.getAttribute("resetEmail");
        Boolean authorized = (Boolean) session.getAttribute("resetAuthorized");
        Long expiry = (Long) session.getAttribute("resetExpiry");

        if (email == null || authorized == null || !authorized || expiry == null) {
            return false;
        }
        return System.currentTimeMillis() <= expiry;
    }

    private void invalidateAuthSession(HttpSession session) {
        if (session != null) {
            session.removeAttribute("resetEmail");
            session.removeAttribute("resetAuthorized");
            session.removeAttribute("resetExpiry");
        }
    }
}
