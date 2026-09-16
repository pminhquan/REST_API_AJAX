package com.hcmute.springboot.controller;

import com.hcmute.springboot.entity.OtpPurpose;
import com.hcmute.springboot.entity.User;
import com.hcmute.springboot.service.IEmailService;
import com.hcmute.springboot.service.IOtpService;
import com.hcmute.springboot.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegisterController {

    private final IUserService userService;
    private final IOtpService otpService;
    private final IEmailService emailService;

    public RegisterController(IUserService userService, IOtpService otpService, IEmailService emailService) {
        this.userService = userService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            HttpServletRequest request,
            Model model
    ) {
        if (isEmpty(username) || isEmpty(email) || isEmpty(password) || isEmpty(confirmPassword)) {
            model.addAttribute("error", "All fields are required.");
            return "register";
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            model.addAttribute("error", "Please enter a valid email address.");
            return "register";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "register";
        }

        User user = null;
        User existingUserByUsername = userService.findByUsername(username.trim());
        User existingUserByEmail = userService.findByEmail(email.trim());

        if (existingUserByUsername != null) {
            if (existingUserByUsername.isActive()) {
                model.addAttribute("error", "Username is already registered.");
                return "register";
            } else {
                if (!existingUserByUsername.getEmail().equalsIgnoreCase(email.trim())) {
                    model.addAttribute("error", "Username is already registered.");
                    return "register";
                }
                user = existingUserByUsername;
            }
        }

        if (existingUserByEmail != null) {
            if (existingUserByEmail.isActive()) {
                model.addAttribute("error", "Email is already registered.");
                return "register";
            } else {
                if (!existingUserByEmail.getUsername().equalsIgnoreCase(username.trim())) {
                    model.addAttribute("error", "Email is already registered.");
                    return "register";
                }
                user = existingUserByEmail;
            }
        }

        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            return "register";
        }

        try {
            if (user == null) {
                user = userService.register(username.trim(), email.trim(), password);
            } else {
                userService.updatePassword(user.getId(), password);
            }

            String otp = otpService.generateOtp(user, OtpPurpose.REGISTER);
            boolean emailSent = emailService.sendOtpEmail(email.trim(), otp, OtpPurpose.REGISTER);

            if (!emailSent) {
                model.addAttribute("error", "Failed to send verification email. Please try again.");
                return "register";
            }

            request.getSession().setAttribute("pendingVerifyEmail", email.trim());
            return "redirect:/verify-otp";

        } catch (Exception e) {
            model.addAttribute("error", "Registration could not be completed. Please try again.");
            return "register";
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
