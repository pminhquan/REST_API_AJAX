package com.hcmute.springboot.controller;

import com.hcmute.springboot.util.UploadStorage;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Controller
public class UploadController {

    private final ServletContext servletContext;

    public UploadController(ServletContext servletContext) {
        this.servletContext = servletContext;
        try {
            UploadStorage.copyPackagedSeedImages(servletContext);
        } catch (Exception ignored) {
        }
    }

    @GetMapping("/uploads/**")
    public ResponseEntity<Resource> serveUploadFile(HttpServletRequest request) throws IOException {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        if (path.startsWith("/uploads/")) {
            path = path.substring("/uploads/".length());
        }

        if (path.isEmpty() || path.contains("..") || path.contains("\0") || path.contains("\\")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Path file = UploadStorage.resolveForReading(servletContext, path);
        if (file == null || !Files.isRegularFile(file)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String mimeType = servletContext.getMimeType(file.getFileName().toString());
        if (mimeType == null) {
            String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                mimeType = "image/jpeg";
            } else if (name.endsWith(".png")) {
                mimeType = "image/png";
            } else if (name.endsWith(".webp")) {
                mimeType = "image/webp";
            } else if (name.endsWith(".gif")) {
                mimeType = "image/gif";
            } else {
                mimeType = "application/octet-stream";
            }
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .contentType(MediaType.parseMediaType(mimeType))
                .contentLength(Files.size(file))
                .body(resource);
    }
}
