package com.hcmute.springboot.controller;

import com.hcmute.springboot.entity.Role;
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
public class LoginController {

    private final IUserService userService;

    public LoginController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLogin(HttpServletRequest request, HttpSession session) {
        if (session != null && session.getAttribute("authenticatedUserId") != null) {
            String role = (String) session.getAttribute("authenticatedUserRole");
            return "ADMIN".equalsIgnoreCase(role) ? "redirect:/categories" : "redirect:/home";
        }
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam(value = "identifier", required = false) String identifier,
            @RequestParam(value = "password", required = false) String password,
            HttpServletRequest request,
            Model model
    ) {
        String genericError = "Invalid username/email or password.";

        if (identifier == null || identifier.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            model.addAttribute("error", genericError);
            return "login";
        }

        User user = userService.findByEmail(identifier.trim());
        if (user == null) {
            user = userService.findByUsername(identifier.trim());
        }

        if (user == null || !user.isActive()) {
            model.addAttribute("error", genericError);
            return "login";
        }

        boolean verified = userService.verifyPassword(user, password);
        if (!verified) {
            model.addAttribute("error", genericError);
            return "login";
        }

        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("authenticatedUserId", user.getId());
        Role role = user.getRole() != null ? user.getRole() : Role.CUSTOMER;
        session.setAttribute("authenticatedUserRole", role.name());
        session.setAttribute("account", user);

        return "ADMIN".equalsIgnoreCase(role.name()) ? "redirect:/categories" : "redirect:/home";
    }
}
