package com.hcmute.springboot.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        // Allow public product views
        if ("/product".equals(path) || "/products/detail".equals(path)) {
            return true;
        }

        // Check if path is protected
        boolean isProfileRoute = path.startsWith("/profile");
        boolean isAdminRoute = isAdminPath(path);

        if (!isProfileRoute && !isAdminRoute) {
            return true;
        }

        HttpSession session = request.getSession(false);
        boolean isLoggedIn = (session != null && session.getAttribute("authenticatedUserId") != null);

        if (!isLoggedIn) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        if (isAdminRoute) {
            boolean isAdmin = "ADMIN".equals(session.getAttribute("authenticatedUserRole"));
            if (!isAdmin) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
        }

        return true;
    }

    private boolean isAdminPath(String path) {
        if (path == null || "/products/detail".equals(path)) {
            return false;
        }
        return path.equals("/products")
                || path.startsWith("/products/")
                || path.equals("/categories")
                || path.startsWith("/categories/")
                || path.startsWith("/admin");
    }
}
