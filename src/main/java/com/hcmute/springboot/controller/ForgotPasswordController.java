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

import java.util.logging.Logger;

@Controller
public class ForgotPasswordController {

    private static final Logger LOGGER = Logger.getLogger(ForgotPasswordController.class.getName());
    private static final String RESET_ACK_MESSAGE = "If the email is registered, a password reset request has been processed.";

    private final IUserService userService;
    private final IOtpService otpService;
    private final IEmailService emailService;

    public ForgotPasswordController(IUserService userService, IOtpService otpService, IEmailService emailService) {
        this.userService = userService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @GetMapping("/forgot-password")
    public String showForgotPassword(
            @RequestParam(value = "action", required = false) String action,
            HttpSession session
    ) {
        if ("cancel".equals(action) && session != null) {
            session.removeAttribute("pendingResetEmail");
            return "redirect:/forgot-password";
        }
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "otp", required = false) String otp,
            HttpServletRequest request,
            HttpSession session,
            Model model
    ) {
        if ("request".equals(action)) {
            return handleRequestOtp(email, session, model);
        } else if ("verify".equals(action)) {
            return handleVerifyOtp(otp, session, model);
        }
        return "redirect:/forgot-password";
    }

    private String handleRequestOtp(String email, HttpSession session, Model model) {
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email is required.");
            return "forgot-password";
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            model.addAttribute("error", "Please enter a valid email address.");
            return "forgot-password";
        }

        try {
            User user = userService.findByEmail(email.trim());
            if (user != null) {
                String otp = otpService.generateOtp(user, OtpPurpose.FORGOT_PASSWORD);
                boolean emailSent = emailService.sendOtpEmail(email.trim(), otp, OtpPurpose.FORGOT_PASSWORD);
                if (!emailSent) {
                    LOGGER.warning("Password reset email delivery failed for user id: " + user.getId());
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Password reset request error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        if (session != null) {
            session.setAttribute("pendingResetEmail", email.trim());
        }
        model.addAttribute("message", RESET_ACK_MESSAGE);
        return "forgot-password";
    }

    private String handleVerifyOtp(String otp, HttpSession session, Model model) {
        String email = session != null ? (String) session.getAttribute("pendingResetEmail") : null;
        if (email == null) {
            return "redirect:/forgot-password";
        }

        if (otp == null || otp.trim().isEmpty()) {
            model.addAttribute("error", "Verification code is required.");
            return "forgot-password";
        }

        User user = userService.findByEmail(email);
        if (user == null) {
            model.addAttribute("error", "Invalid or expired verification code.");
            return "forgot-password";
        }

        boolean verified = otpService.verifyOtp(user, OtpPurpose.FORGOT_PASSWORD, otp.trim());
        if (verified) {
            session.removeAttribute("pendingResetEmail");
            session.setAttribute("resetEmail", email);
            session.setAttribute("resetAuthorized", true);
            session.setAttribute("resetExpiry", System.currentTimeMillis() + 5 * 60 * 1000);
            return "redirect:/reset-password";
        } else {
            model.addAttribute("error", "Invalid or expired verification code.");
            return "forgot-password";
        }
    }
}
