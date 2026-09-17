package com.hcmute.springboot.controller;

import com.hcmute.springboot.entity.Category;
import com.hcmute.springboot.service.ICategoryService;
import com.hcmute.springboot.util.UploadStorage;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
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
public class CategoryController {

    public static final int MAX_PAGE_SIZE = 50;
    public static final int DEFAULT_PAGE_SIZE = 5;

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Object CATEGORY_WRITE_LOCK = new Object();

    private final ICategoryService categoryService;
    private final ServletContext servletContext;

    public CategoryController(ICategoryService categoryService, ServletContext servletContext) {
        this.categoryService = categoryService;
        this.servletContext = servletContext;
    }

    @GetMapping("/categories")
    public String handleGet(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "id", required = false) String idStr,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            Model model
    ) {
        if ("add".equalsIgnoreCase(action)) {
            return "category-add";
        }
        if ("edit".equalsIgnoreCase(action)) {
            return showEditForm(idStr, model);
        }

        if (page <= 0) page = 1;
        if (size <= 0) {
            size = DEFAULT_PAGE_SIZE;
        } else if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        Page<Category> categoryPage = categoryService.searchCategories(
                keyword, PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "categoryid"))
        );

        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("totalCategories", categoryPage.getTotalElements());
        model.addAttribute("keyword", keyword != null ? keyword : "");

        return "category-list";
    }

    @GetMapping("/categories/add")
    public String showAddPage() {
        return "category-add";
    }

    @GetMapping({"/categories/ajax", "/categories-ajax"})
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<String> showAjaxCategoryPage(
            HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response
    ) throws java.io.IOException {
        jakarta.servlet.http.HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute(com.hcmute.springboot.config.CsrfInterceptor.CSRF_SESSION_ATTR);
        if (token == null || token.isBlank()) {
            token = java.util.UUID.randomUUID().toString();
            session.setAttribute(com.hcmute.springboot.config.CsrfInterceptor.CSRF_SESSION_ATTR, token);
        }
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("XSRF-TOKEN", token);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        response.addCookie(cookie);

        org.springframework.core.io.ClassPathResource resource =
                new org.springframework.core.io.ClassPathResource("templates/categories-ajax.html");
        String html;
        try (InputStream inputStream = resource.getInputStream()) {
            html = new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
        html = html.replace("{{CSRF_TOKEN}}", token);
        return org.springframework.http.ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.TEXT_HTML)
                .body(html);
    }

    @GetMapping("/categories/edit")
    public String showEditPage(@RequestParam(value = "id", required = false) String idStr, Model model) {
        return showEditForm(idStr, model);
    }

    private String showEditForm(String idStr, Model model) {
        if (idStr == null || idStr.trim().isEmpty()) {
            return "redirect:/categories";
        }
        try {
            int id = Integer.parseInt(idStr.trim());
            Category category = categoryService.findById(id);
            if (category == null) {
                return "redirect:/categories";
            }
            model.addAttribute("category", category);
            return "category-edit";
        } catch (NumberFormatException e) {
            return "redirect:/categories";
        }
    }

    @PostMapping("/categories")
    public String handlePost(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "categoryid", required = false) String categoryidStr,
            @RequestParam(value = "id", required = false) String idStr,
            @RequestParam(value = "categoryname", required = false) String categoryname,
            @RequestParam(value = "status", required = false) String statusStr,
            @RequestParam(value = "images", required = false) String imagesParam,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            Model model
    ) {
        if ("insert".equalsIgnoreCase(action)) {
            return insertCategory(categoryname, statusStr, imagesParam, imageFile, model);
        } else if ("update".equalsIgnoreCase(action)) {
            String targetId = categoryidStr != null ? categoryidStr : idStr;
            return updateCategory(targetId, categoryname, statusStr, imagesParam, imageFile, model);
        } else if ("delete".equalsIgnoreCase(action)) {
            String targetId = idStr != null ? idStr : categoryidStr;
            return deleteCategory(targetId);
        }
        return "redirect:/categories";
    }

    @PostMapping("/categories/add")
    public String insertCategoryDirect(
            @RequestParam(value = "categoryname", required = false) String categoryname,
            @RequestParam(value = "status", required = false) String statusStr,
            @RequestParam(value = "images", required = false) String imagesParam,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            Model model
    ) {
        return insertCategory(categoryname, statusStr, imagesParam, imageFile, model);
    }

    @PostMapping("/categories/edit")
    public String updateCategoryDirect(
            @RequestParam(value = "categoryid", required = false) String categoryidStr,
            @RequestParam(value = "id", required = false) String idStr,
            @RequestParam(value = "categoryname", required = false) String categoryname,
            @RequestParam(value = "status", required = false) String statusStr,
            @RequestParam(value = "images", required = false) String imagesParam,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            Model model
    ) {
        String targetId = categoryidStr != null ? categoryidStr : idStr;
        return updateCategory(targetId, categoryname, statusStr, imagesParam, imageFile, model);
    }

    @PostMapping("/categories/delete")
    public String deleteCategoryDirect(@RequestParam(value = "id", required = false) String idStr) {
        return deleteCategory(idStr);
    }

    private String insertCategory(
            String categoryname, String statusStr, String imagesParam, MultipartFile imageFile, Model model
    ) {
        int status;
        try {
            status = Integer.parseInt(statusStr);
        } catch (Exception e) {
            model.addAttribute("error", "Invalid status value.");
            return "category-add";
        }

        if (categoryname == null || categoryname.trim().isEmpty()) {
            model.addAttribute("error", "Category name cannot be empty.");
            return "category-add";
        }

        if (categoryname.trim().length() > 100) {
            model.addAttribute("error", "Category name must not exceed 100 characters.");
            return "category-add";
        }

        String storedImage = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                storedImage = storeImage(imageFile);
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", e.getMessage());
                return "category-add";
            } catch (Exception e) {
                model.addAttribute("error", "Unable to upload image file.");
                return "category-add";
            }
        }

        String finalImage;
        if (storedImage != null) {
            finalImage = storedImage;
        } else if (imagesParam != null && !imagesParam.trim().isEmpty()) {
            if (imagesParam.trim().length() > 500) {
                model.addAttribute("error", "Image path must not exceed 500 characters.");
                return "category-add";
            }
            if (imagesParam.contains("..")) {
                model.addAttribute("error", "Invalid image reference.");
                return "category-add";
            }
            finalImage = imagesParam.trim();
        } else {
            finalImage = "";
        }

        Category category = new Category();
        category.setCategoryname(categoryname.trim());
        category.setImages(finalImage);
        category.setStatus(status);

        boolean duplicateFound = false;
        synchronized (CATEGORY_WRITE_LOCK) {
            Category existing = categoryService.findByName(categoryname.trim());
            if (existing != null) {
                duplicateFound = true;
            } else {
                categoryService.insert(category);
            }
        }

        if (duplicateFound) {
            if (storedImage != null) {
                UploadStorage.deleteFile(servletContext, storedImage);
            }
            model.addAttribute("error", "Category name already exists.");
            return "category-add";
        }

        return "redirect:/categories?message=add_success";
    }

    private String updateCategory(
            String idStr, String categoryname, String statusStr, String imagesParam, MultipartFile imageFile, Model model
    ) {
        if (idStr == null || idStr.trim().isEmpty()) {
            return "redirect:/categories";
        }
        int id;
        try {
            id = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            return "redirect:/categories";
        }

        Category existingCategory = categoryService.findById(id);
        if (existingCategory == null) {
            return "redirect:/categories";
        }

        int status;
        try {
            status = Integer.parseInt(statusStr);
        } catch (Exception e) {
            model.addAttribute("category", existingCategory);
            model.addAttribute("error", "Invalid status value.");
            return "category-edit";
        }

        if (categoryname == null || categoryname.trim().isEmpty()) {
            model.addAttribute("category", existingCategory);
            model.addAttribute("error", "Category name cannot be empty.");
            return "category-edit";
        }

        if (categoryname.trim().length() > 100) {
            model.addAttribute("category", existingCategory);
            model.addAttribute("error", "Category name must not exceed 100 characters.");
            return "category-edit";
        }

        String storedImage = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                storedImage = storeImage(imageFile);
            } catch (IllegalArgumentException e) {
                model.addAttribute("category", existingCategory);
                model.addAttribute("error", e.getMessage());
                return "category-edit";
            } catch (Exception e) {
                model.addAttribute("category", existingCategory);
                model.addAttribute("error", "Unable to upload image file.");
                return "category-edit";
            }
        }

        String finalImage;
        if (storedImage != null) {
            finalImage = storedImage;
        } else if (imagesParam != null && !imagesParam.trim().isEmpty()) {
            if (imagesParam.trim().length() > 500) {
                model.addAttribute("category", existingCategory);
                model.addAttribute("error", "Image path must not exceed 500 characters.");
                return "category-edit";
            }
            if (imagesParam.contains("..")) {
                model.addAttribute("category", existingCategory);
                model.addAttribute("error", "Invalid image reference.");
                return "category-edit";
            }
            finalImage = imagesParam.trim();
        } else {
            finalImage = existingCategory.getImages() != null ? existingCategory.getImages() : "";
        }

        boolean duplicateFound = false;
        String oldImageToDelete = null;

        synchronized (CATEGORY_WRITE_LOCK) {
            Category dupCheck = categoryService.findByName(categoryname.trim());
            if (dupCheck != null && dupCheck.getCategoryid() != id) {
                duplicateFound = true;
            } else {
                String oldImage = existingCategory.getImages();
                existingCategory.setCategoryname(categoryname.trim());
                existingCategory.setImages(finalImage);
                existingCategory.setStatus(status);
                categoryService.update(existingCategory);

                if (storedImage != null && oldImage != null && !oldImage.isBlank() && !oldImage.equals(storedImage)) {
                    oldImageToDelete = oldImage;
                }
            }
        }

        if (duplicateFound) {
            if (storedImage != null) {
                UploadStorage.deleteFile(servletContext, storedImage);
            }
            model.addAttribute("category", existingCategory);
            model.addAttribute("error", "Category name already exists.");
            return "category-edit";
        }

        if (oldImageToDelete != null && !oldImageToDelete.isBlank()) {
            if (!oldImageToDelete.startsWith("http://") && !oldImageToDelete.startsWith("https://")) {
                UploadStorage.deleteFile(servletContext, oldImageToDelete);
            }
        }

        return "redirect:/categories?message=update_success";
    }

    private String deleteCategory(String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) {
            return "redirect:/categories?error=invalid_id";
        }
        int id;
        try {
            id = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            return "redirect:/categories?error=invalid_id";
        }

        Category category = categoryService.findById(id);
        if (category == null) {
            return "redirect:/categories?error=not_found";
        }

        if (categoryService.isCategoryInUse(id)) {
            return "redirect:/categories?error=in_use";
        }

        boolean success = categoryService.delete(id);
        if (success) {
            return "redirect:/categories?message=delete_success";
        } else {
            return "redirect:/categories?error=delete_failed";
        }
    }

    private String storeImage(MultipartFile file) throws Exception {
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
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Only JPG, JPEG, PNG and WEBP images are allowed.");
        }

        String generated = UUID.randomUUID() + "." + ext;
        try (InputStream in = file.getInputStream()) {
            return UploadStorage.storeFile(servletContext, in, "categories", generated);
        }
    }
}
