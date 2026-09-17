package com.hcmute.springboot.controller.api;

import com.hcmute.springboot.entity.Category;
import com.hcmute.springboot.entity.Product;
import com.hcmute.springboot.service.ICategoryService;
import com.hcmute.springboot.service.IProductService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {

    private final IProductService productService;
    private final ICategoryService categoryService;

    public ProductApiController(IProductService productService, ICategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(productService.searchProducts(keyword.trim(), Pageable.unpaged()).getContent());
        }
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") int id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Product not found"));
        }
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        if (product == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Request body cannot be null."));
        }

        String name = product.getProductname();
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Product name cannot be empty."));
        }
        if (name.trim().length() > 250) {
            return ResponseEntity.badRequest().body(Map.of("message", "Product name must not exceed 250 characters."));
        }

        if (product.getDescription() != null && product.getDescription().trim().length() > 500) {
            return ResponseEntity.badRequest().body(Map.of("message", "Description must not exceed 500 characters."));
        }

        double price = product.getPrice();
        if (price <= 0 || !Double.isFinite(price)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Price must be greater than 0."));
        }
        if (price != Math.rint(price)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Price must be a whole number."));
        }

        String images = product.getImages();
        if (images != null) {
            if (images.trim().length() > 500) {
                return ResponseEntity.badRequest().body(Map.of("message", "Image path must not exceed 500 characters."));
            }
            if (images.contains("..")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid image reference."));
            }
            product.setImages(images.trim());
        } else {
            product.setImages("");
        }

        if (product.getCategory() == null || product.getCategory().getCategoryid() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Category is required."));
        }

        Category existingCategory = categoryService.findById(product.getCategory().getCategoryid());
        if (existingCategory == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Category does not exist."));
        }

        product.setProductid(0);
        product.setProductname(name.trim());
        product.setDescription(product.getDescription() != null ? product.getDescription().trim() : "");
        product.setCategory(existingCategory);

        productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable("id") int id,
            @RequestBody Product productDetails
    ) {
        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Product not found"));
        }

        if (productDetails == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Request body cannot be null."));
        }

        String name = productDetails.getProductname();
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Product name cannot be empty."));
        }
        if (name.trim().length() > 250) {
            return ResponseEntity.badRequest().body(Map.of("message", "Product name must not exceed 250 characters."));
        }

        if (productDetails.getDescription() != null && productDetails.getDescription().trim().length() > 500) {
            return ResponseEntity.badRequest().body(Map.of("message", "Description must not exceed 500 characters."));
        }

        double price = productDetails.getPrice();
        if (price <= 0 || !Double.isFinite(price)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Price must be greater than 0."));
        }
        if (price != Math.rint(price)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Price must be a whole number."));
        }

        String images = productDetails.getImages();
        if (images != null) {
            if (images.trim().length() > 500) {
                return ResponseEntity.badRequest().body(Map.of("message", "Image path must not exceed 500 characters."));
            }
            if (images.contains("..")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid image reference."));
            }
            existingProduct.setImages(images.trim());
        }

        if (productDetails.getCategory() != null && productDetails.getCategory().getCategoryid() > 0) {
            Category existingCategory = categoryService.findById(productDetails.getCategory().getCategoryid());
            if (existingCategory == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Category does not exist."));
            }
            existingProduct.setCategory(existingCategory);
        }

        existingProduct.setProductname(name.trim());
        if (productDetails.getDescription() != null) {
            existingProduct.setDescription(productDetails.getDescription().trim());
        }
        existingProduct.setPrice(price);
        existingProduct.setStatus(productDetails.getStatus());

        productService.updateProduct(existingProduct);
        return ResponseEntity.ok(existingProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable("id") int id) {
        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Product not found"));
        }

        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
