package com.hcmute.springboot.controller;

import com.hcmute.springboot.entity.Category;
import com.hcmute.springboot.entity.Product;
import com.hcmute.springboot.service.ICategoryService;
import com.hcmute.springboot.service.IProductService;
import com.hcmute.springboot.util.UploadStorage;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

@Controller
public class ProductController {

    public static final int MAX_PAGE_SIZE = 50;
    public static final int DEFAULT_PAGE_SIZE = 6;

    private static final Logger LOGGER = Logger.getLogger(ProductController.class.getName());
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final IProductService productService;
    private final ICategoryService categoryService;
    private final ServletContext servletContext;

    public ProductController(IProductService productService, ICategoryService categoryService, ServletContext servletContext) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.servletContext = servletContext;
    }

    @GetMapping({"/product", "/products"})
    public String listProducts(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "6") int size,
            HttpServletRequest request,
            Model model
    ) {
        if (page <= 0) {
            page = 1;
        }
        if (size <= 0) {
            size = DEFAULT_PAGE_SIZE;
        } else if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        long totalProducts = productService.countAllProducts();
        int totalPages = (int) Math.ceil((double) totalProducts / size);
        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
        }

        Page<Product> productPage = productService.getProductsPage(PageRequest.of(page - 1, size));
        boolean managementView = request.getRequestURI().contains("/products");

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("managementView", managementView);

        return "product-list";
    }

    @GetMapping("/products/ajax")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<String> showAjaxProductPage(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws java.io.IOException {
        String contextPath = request.getContextPath() != null ? request.getContextPath() : "";

        jakarta.servlet.http.HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute(com.hcmute.springboot.config.CsrfInterceptor.CSRF_SESSION_ATTR);
        if (token == null || token.isBlank()) {
            token = java.util.UUID.randomUUID().toString();
            session.setAttribute(com.hcmute.springboot.config.CsrfInterceptor.CSRF_SESSION_ATTR, token);
        }
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("XSRF-TOKEN", token);
        cookie.setPath(contextPath.isEmpty() ? "/" : contextPath);
        cookie.setHttpOnly(false);
        response.addCookie(cookie);

        org.springframework.core.io.ClassPathResource resource =
                new org.springframework.core.io.ClassPathResource("templates/products-ajax.html");
        String html;
        try (InputStream inputStream = resource.getInputStream()) {
            html = new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
        html = html.replace("{{CSRF_TOKEN}}", token)
                .replace("{{CONTEXT_PATH}}", contextPath);
        return org.springframework.http.ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.TEXT_HTML)
                .body(html);
    }

    @GetMapping("/products/detail")
    public String showDetail(@RequestParam(value = "id", required = false) String idStr, Model model, HttpServletResponse response) {
        if (idStr == null || idStr.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("error", "Product ID is missing.");
            return "product-detail";
        }

        int id;
        try {
            id = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("error", "Invalid Product ID format.");
            return "product-detail";
        }

        Product product = productService.getProductById(id);
        if (product == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("error", "Product not found.");
            return "product-detail";
        }

        model.addAttribute("product", product);
        return "product-detail";
    }

    @GetMapping("/products/add")
    public String showAddForm(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "product-add";
    }

    @PostMapping("/products/add")
    public String insertProduct(
            @RequestParam(value = "productname", required = false) String productname,
            @RequestParam(value = "price", required = false) String priceStr,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "categoryid", required = false) String categoryidStr,
            @RequestParam(value = "status", defaultValue = "1") int status,
            @RequestParam(value = "images", required = false) MultipartFile imageFile,
            Model model
    ) {
        if (productname == null || productname.trim().isEmpty()) {
            return forwardAddError("Product name cannot be empty.", model);
        }
        if (productname.trim().length() > 250) {
            return forwardAddError("Product name must not exceed 250 characters.", model);
        }

        double price;
        try {
            price = parsePrice(priceStr);
        } catch (IllegalArgumentException e) {
            return forwardAddError(e.getMessage(), model);
        }

        if (categoryidStr == null || categoryidStr.trim().isEmpty()) {
            return forwardAddError("Selected category does not exist.", model);
        }

        int categoryId;
        Category category;
        try {
            categoryId = Integer.parseInt(categoryidStr.trim());
            category = categoryService.findById(categoryId);
        } catch (NumberFormatException e) {
            return forwardAddError("Selected category does not exist.", model);
        }

        if (category == null) {
            return forwardAddError("Selected category does not exist.", model);
        }

        if (description != null && description.trim().length() > 500) {
            return forwardAddError("Description must not exceed 500 characters.", model);
        }

        String storedImage = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                storedImage = storeImage(imageFile);
            } catch (IllegalArgumentException e) {
                return forwardAddError(e.getMessage(), model);
            } catch (Exception e) {
                return forwardAddError("Unable to upload image file.", model);
            }
        }

        Product product = new Product();
        product.setProductname(productname.trim());
        product.setPrice(price);
        product.setDescription(description == null ? "" : description.trim());
        product.setImages(storedImage == null ? "" : storedImage);
        product.setCategory(category);
        product.setStatus(status);

        try {
            productService.createProduct(product);
            return "redirect:/products?message=add_success";
        } catch (Exception e) {
            if (storedImage != null) {
                UploadStorage.deleteFile(servletContext, storedImage);
            }
            return forwardAddError(e.getMessage() != null ? e.getMessage() : "Failed to create product.", model);
        }
    }

    @GetMapping("/products/edit")
    public String showEditForm(@RequestParam(value = "id", required = false) String idStr, Model model) {
        if (idStr == null || idStr.trim().isEmpty()) {
            return "redirect:/products";
        }
        int id;
        try {
            id = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            return "redirect:/products";
        }

        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/products";
        }

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll());
        return "product-edit";
    }

    @PostMapping("/products/edit")
    public String updateProduct(
            @RequestParam(value = "productid", required = false) String productidStr,
            @RequestParam(value = "productname", required = false) String productname,
            @RequestParam(value = "price", required = false) String priceStr,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "categoryid", required = false) String categoryidStr,
            @RequestParam(value = "status", defaultValue = "1") int status,
            @RequestParam(value = "images", required = false) MultipartFile imageFile,
            Model model
    ) {
        if (productidStr == null || productidStr.trim().isEmpty()) {
            return "redirect:/products";
        }
        int productId;
        try {
            productId = Integer.parseInt(productidStr.trim());
        } catch (NumberFormatException e) {
            return "redirect:/products";
        }

        Product existing = productService.getProductById(productId);
        if (existing == null) {
            return "redirect:/products";
        }

        if (productname == null || productname.trim().isEmpty()) {
            return forwardEditError(existing, "Product name cannot be empty.", model);
        }
        if (productname.trim().length() > 250) {
            return forwardEditError(existing, "Product name must not exceed 250 characters.", model);
        }

        double price;
        try {
            price = parsePrice(priceStr);
        } catch (IllegalArgumentException e) {
            return forwardEditError(existing, e.getMessage(), model);
        }

        if (categoryidStr == null || categoryidStr.trim().isEmpty()) {
            return forwardEditError(existing, "Selected category does not exist.", model);
        }

        int categoryId;
        Category category;
        try {
            categoryId = Integer.parseInt(categoryidStr.trim());
            category = categoryService.findById(categoryId);
        } catch (NumberFormatException e) {
            return forwardEditError(existing, "Selected category does not exist.", model);
        }

        if (category == null) {
            return forwardEditError(existing, "Selected category does not exist.", model);
        }

        if (description != null && description.trim().length() > 500) {
            return forwardEditError(existing, "Description must not exceed 500 characters.", model);
        }

        String oldImage = existing.getImages();
        String storedImage = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                storedImage = storeImage(imageFile);
            } catch (IllegalArgumentException e) {
                return forwardEditError(existing, e.getMessage(), model);
            } catch (Exception e) {
                return forwardEditError(existing, "Unable to upload image file.", model);
            }
        }

        existing.setProductname(productname.trim());
        existing.setPrice(price);
        existing.setDescription(description == null ? "" : description.trim());
        if (storedImage != null) {
            existing.setImages(storedImage);
        }
        existing.setCategory(category);
        existing.setStatus(status);

        try {
            productService.updateProduct(existing);
            if (storedImage != null && oldImage != null && !oldImage.isBlank()) {
                if (!oldImage.startsWith("http://") && !oldImage.startsWith("https://")) {
                    UploadStorage.deleteFile(servletContext, oldImage);
                }
            }
            return "redirect:/products?message=update_success";
        } catch (Exception e) {
            if (storedImage != null) {
                UploadStorage.deleteFile(servletContext, storedImage);
            }
            return forwardEditError(existing, e.getMessage() != null ? e.getMessage() : "Failed to update product.", model);
        }
    }

    @PostMapping("/products/delete")
    public String deleteProductPost(@RequestParam(value = "id", required = false) String idStr) {
        return deleteProduct(idStr);
    }

    private String deleteProduct(String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) {
            return "redirect:/products?error=invalid_id";
        }
        try {
            int id = Integer.parseInt(idStr.trim());
            productService.deleteProduct(id);
            return "redirect:/products?message=delete_success";
        } catch (Exception e) {
            return "redirect:/products?error=delete_failed";
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
            throw new IllegalArgumentException("Unsupported image type. Allowed types: JPG, JPEG, PNG, WEBP.");
        }

        String generated = UUID.randomUUID() + "." + ext;
        try (InputStream in = file.getInputStream()) {
            return UploadStorage.storeFile(servletContext, in, "products", generated);
        }
    }

    private double parsePrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Price must be a valid number.");
        }
        try {
            double price = Double.parseDouble(priceStr.trim());
            if (price <= 0 || !Double.isFinite(price)) {
                throw new IllegalArgumentException("Price must be greater than 0.");
            }
            if (price != Math.rint(price)) {
                throw new IllegalArgumentException("Price must be a whole number.");
            }
            return price;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Price must be a valid number.");
        }
    }

    private String forwardAddError(String error, Model model) {
        model.addAttribute("error", error);
        model.addAttribute("categories", categoryService.findAll());
        return "product-add";
    }

    private String forwardEditError(Product product, String error, Model model) {
        model.addAttribute("product", product);
        model.addAttribute("error", error);
        model.addAttribute("categories", categoryService.findAll());
        return "product-edit";
    }
}
