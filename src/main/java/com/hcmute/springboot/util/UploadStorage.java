package com.hcmute.springboot.util;

import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public final class UploadStorage {

    private static final Logger LOGGER = Logger.getLogger(UploadStorage.class.getName());

    public static final Set<String> ALLOWED_EXTENSIONS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "webp"))
    );

    private UploadStorage() {
    }

    public static Path getUploadRoot(ServletContext servletContext) {
        String customDir = System.getProperty("app.upload.dir");
        if (customDir == null || customDir.isBlank()) {
            customDir = System.getenv("APP_UPLOAD_DIR");
        }
        if (customDir != null && !customDir.isBlank()) {
            Path path = Paths.get(customDir.trim()).toAbsolutePath().normalize();
            ensureDirectoryExists(path);
            return path;
        }

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            Path path = Paths.get(catalinaBase, "uploads").toAbsolutePath().normalize();
            ensureDirectoryExists(path);
            return path;
        }

        String catalinaHome = System.getProperty("catalina.home");
        if (catalinaHome != null && !catalinaHome.isBlank()) {
            Path path = Paths.get(catalinaHome, "uploads").toAbsolutePath().normalize();
            ensureDirectoryExists(path);
            return path;
        }

        if (servletContext != null) {
            String realPath = servletContext.getRealPath("/uploads");
            if (realPath != null && !realPath.isBlank()) {
                Path path = Paths.get(realPath).toAbsolutePath().normalize();
                ensureDirectoryExists(path);
                return path;
            }
        }

        Path fallback = Paths.get(System.getProperty("user.home"), ".jpaexercise", "uploads").toAbsolutePath().normalize();
        ensureDirectoryExists(fallback);
        return fallback;
    }

    private static void ensureDirectoryExists(Path dir) {
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            LOGGER.warning("Could not create upload directory: " + dir + " (" + e.getMessage() + ")");
        }
    }

    public static String detectImageFormat(byte[] header) {
        if (header == null || header.length < 12) {
            return null;
        }
        // JPEG: FF D8 FF
        if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
            return "jpeg";
        }
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if ((header[0] & 0xFF) == 0x89 &&
                (header[1] & 0xFF) == 0x50 &&
                (header[2] & 0xFF) == 0x4E &&
                (header[3] & 0xFF) == 0x47 &&
                (header[4] & 0xFF) == 0x0D &&
                (header[5] & 0xFF) == 0x0A &&
                (header[6] & 0xFF) == 0x1A &&
                (header[7] & 0xFF) == 0x0A) {
            return "png";
        }
        // WEBP: RIFF....WEBP (bytes 0..3 == "RIFF", bytes 8..11 == "WEBP")
        if (header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F' &&
                header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return "webp";
        }
        return null;
    }

    public static boolean isFormatCompatibleWithExtension(String format, String extension) {
        if (format == null || extension == null) {
            return false;
        }
        String ext = extension.toLowerCase(Locale.ROOT);
        if ("jpeg".equals(format)) {
            return "jpg".equals(ext) || "jpeg".equals(ext);
        }
        return format.equalsIgnoreCase(ext);
    }

    public static boolean validateImageFile(Path file) throws IOException {
        if (file == null || !Files.isRegularFile(file)) {
            return false;
        }
        if (Files.size(file) < 12) {
            return false;
        }
        byte[] header = new byte[12];
        try (InputStream is = Files.newInputStream(file)) {
            int read = is.readNBytes(header, 0, 12);
            if (read < 12) {
                return false;
            }
        }
        String format = detectImageFormat(header);
        if (format == null) {
            return false;
        }
        String fileName = file.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        if (dot <= 0 || dot == fileName.length() - 1) {
            return false;
        }
        String ext = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return isFormatCompatibleWithExtension(format, ext);
    }

    public static String storeFile(ServletContext servletContext, InputStream inputStream, String subfolder, String fileName) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null.");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be empty.");
        }
        if (fileName.contains("/") || fileName.contains("\\") || fileName.contains("..") || fileName.contains("\0")) {
            throw new IllegalArgumentException("Invalid image filename.");
        }

        Path uploadRoot = getUploadRoot(servletContext);
        Path targetDir = (subfolder != null && !subfolder.isBlank()) ? uploadRoot.resolve(subfolder).normalize() : uploadRoot;
        if (!targetDir.startsWith(uploadRoot)) {
            throw new IOException("Upload target directory is invalid.");
        }
        Files.createDirectories(targetDir);

        Path targetFile = targetDir.resolve(fileName).normalize();
        if (!targetFile.getParent().equals(targetDir)) {
            throw new IOException("Upload target file path is invalid.");
        }

        try {
            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            if (!validateImageFile(targetFile)) {
                Files.deleteIfExists(targetFile);
                throw new IllegalArgumentException("Invalid image content: signature does not match allowed types (JPG, JPEG, PNG, WEBP).");
            }
        } catch (Exception e) {
            try {
                Files.deleteIfExists(targetFile);
            } catch (IOException ignored) {
            }
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            throw new IOException("Failed to store uploaded file: " + e.getMessage(), e);
        }

        return (subfolder != null && !subfolder.isBlank()) ? (subfolder + "/" + fileName) : fileName;
    }

    public static boolean deleteFile(ServletContext servletContext, String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return false;
        }
        if (relativePath.contains("..") || relativePath.contains("\0") || relativePath.startsWith("/") || relativePath.startsWith("\\")) {
            return false;
        }

        try {
            Path uploadRoot = getUploadRoot(servletContext);
            Path target = uploadRoot.resolve(relativePath).normalize();
            if (target.startsWith(uploadRoot) && Files.exists(target)) {
                Files.deleteIfExists(target);
                return true;
            }

            if (!relativePath.contains("/") && !relativePath.contains("\\")) {
                for (String sub : Arrays.asList("products", "avatars", "categories")) {
                    Path subTarget = uploadRoot.resolve(sub).resolve(relativePath).normalize();
                    if (subTarget.startsWith(uploadRoot) && Files.exists(subTarget)) {
                        Files.deleteIfExists(subTarget);
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.warning("Unable to remove uploaded image: " + relativePath + " (" + e.getMessage() + ")");
        }
        return false;
    }

    public static Path resolveForReading(ServletContext servletContext, String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        if (relativePath.contains("..") || relativePath.contains("\0") || relativePath.startsWith("/") || relativePath.startsWith("\\")) {
            return null;
        }

        Path uploadRoot = getUploadRoot(servletContext);
        Path direct = uploadRoot.resolve(relativePath).normalize();
        if (direct.startsWith(uploadRoot) && Files.isRegularFile(direct)) {
            return direct;
        }

        if (!relativePath.contains("/") && !relativePath.contains("\\")) {
            for (String sub : Arrays.asList("products", "avatars", "categories")) {
                Path subPath = uploadRoot.resolve(sub).resolve(relativePath).normalize();
                if (subPath.startsWith(uploadRoot) && Files.isRegularFile(subPath)) {
                    return subPath;
                }
            }
        }

        if (relativePath.contains("/") || relativePath.contains("\\")) {
            Path fileNameOnly = Paths.get(relativePath).getFileName();
            if (fileNameOnly != null) {
                Path flatUpload = uploadRoot.resolve(fileNameOnly).normalize();
                if (flatUpload.startsWith(uploadRoot) && Files.isRegularFile(flatUpload)) {
                    return flatUpload;
                }
            }
        }

        if (servletContext != null) {
            String packagedPath = servletContext.getRealPath("/uploads");
            if (packagedPath != null) {
                Path packagedRoot = Paths.get(packagedPath).toAbsolutePath().normalize();
                Path packagedFile = packagedRoot.resolve(relativePath).normalize();
                if (packagedFile.startsWith(packagedRoot) && Files.isRegularFile(packagedFile)) {
                    return packagedFile;
                }
                if (!relativePath.contains("/") && !relativePath.contains("\\")) {
                    for (String sub : Arrays.asList("products", "avatars", "categories")) {
                        Path subPath = packagedRoot.resolve(sub).resolve(relativePath).normalize();
                        if (subPath.startsWith(packagedRoot) && Files.isRegularFile(subPath)) {
                            return subPath;
                        }
                    }
                }
                if (relativePath.contains("/") || relativePath.contains("\\")) {
                    Path fileNameOnly = Paths.get(relativePath).getFileName();
                    if (fileNameOnly != null) {
                        Path flatPackaged = packagedRoot.resolve(fileNameOnly).normalize();
                        if (flatPackaged.startsWith(packagedRoot) && Files.isRegularFile(flatPackaged)) {
                            return flatPackaged;
                        }
                    }
                }
            }
        }

        return null;
    }

    public static int copyPackagedSeedImages(ServletContext servletContext) {
        if (servletContext == null) {
            return 0;
        }
        String packagedPath = servletContext.getRealPath("/uploads");
        if (packagedPath == null) {
            return 0;
        }
        Path packagedRoot = Paths.get(packagedPath).toAbsolutePath().normalize();
        Path uploadRoot = getUploadRoot(servletContext);
        if (!Files.isDirectory(packagedRoot) || packagedRoot.equals(uploadRoot) || uploadRoot.startsWith(packagedRoot)) {
            return 0;
        }

        int copied = 0;
        try (java.util.stream.Stream<Path> stream = Files.walk(packagedRoot)) {
            List<Path> files = stream.filter(Files::isRegularFile).toList();
            for (Path file : files) {
                Path relative = packagedRoot.relativize(file);
                Path dest = uploadRoot.resolve(relative).normalize();
                if (dest.startsWith(uploadRoot) && !Files.exists(dest)) {
                    Path parent = dest.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }
                    try {
                        Files.copy(file, dest);
                        copied++;
                    } catch (IOException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.warning("Could not read packaged uploads directory: " + e.getMessage());
        }
        return copied;
    }
}
