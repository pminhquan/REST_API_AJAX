package com.hcmute.springboot.controller;

import com.hcmute.springboot.entity.OtpPurpose;
import com.hcmute.springboot.entity.User;
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
public class VerifyOtpController {

    private final IUserService userService;
    private final IOtpService otpService;

    public VerifyOtpController(IUserService userService, IOtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    @GetMapping("/verify-otp")
    public String showVerify(HttpSession session) {
        String email = session != null ? (String) session.getAttribute("pendingVerifyEmail") : null;
        if (email == null) {
            return "redirect:/register";
        }
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerify(
            @RequestParam(value = "otp", required = false) String otp,
            HttpServletRequest request,
            HttpSession session,
            Model model
    ) {
        String email = session != null ? (String) session.getAttribute("pendingVerifyEmail") : null;
        if (email == null) {
            return "redirect:/register";
        }

        if (otp == null || otp.trim().isEmpty()) {
            model.addAttribute("error", "Verification code is required.");
            return "verify-otp";
        }

        User user = userService.findByEmail(email);
        if (user == null) {
            model.addAttribute("error", "Invalid user session.");
            return "verify-otp";
        }

        boolean verified = otpService.verifyOtp(user, OtpPurpose.REGISTER, otp.trim());
        if (verified) {
            boolean activated = userService.activateUser(user.getId());
            if (activated) {
                session.removeAttribute("pendingVerifyEmail");
                model.addAttribute("success", "Your account has been successfully verified and activated!");
            } else {
                model.addAttribute("error", "Failed to activate user account.");
            }
        } else {
            model.addAttribute("error", "Invalid, expired, or blocked verification code.");
        }

        return "verify-otp";
    }
}
