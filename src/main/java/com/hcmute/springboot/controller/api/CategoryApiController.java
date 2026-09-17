package com.hcmute.springboot.controller.api;

import com.hcmute.springboot.entity.Category;
import com.hcmute.springboot.service.ICategoryService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryApiController {

    private final ICategoryService categoryService;

    public CategoryApiController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories(
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(categoryService.searchCategories(keyword.trim(), Pageable.unpaged()).getContent());
        }
        return ResponseEntity.ok(categoryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable("id") int id) {
        Category category = categoryService.findById(id);
        if (category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Category not found"));
        }
        return ResponseEntity.ok(category);
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        if (category == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Request body cannot be null."));
        }

        String name = category.getCategoryname();
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Category name cannot be empty."));
        }
        if (name.trim().length() > 100) {
            return ResponseEntity.badRequest().body(Map.of("message", "Category name must not exceed 100 characters."));
        }

        String images = category.getImages();
        if (images != null) {
            if (images.trim().length() > 500) {
                return ResponseEntity.badRequest().body(Map.of("message", "Image path must not exceed 500 characters."));
            }
            if (images.contains("..")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid image reference."));
            }
            category.setImages(images.trim());
        } else {
            category.setImages("");
        }

        category.setCategoryname(name.trim());

        if (categoryService.findByName(category.getCategoryname()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Category name already exists."));
        }

        category.setCategoryid(0);
        categoryService.insert(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable("id") int id,
            @RequestBody Category categoryDetails
    ) {
        Category existingCategory = categoryService.findById(id);
        if (existingCategory == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Category not found"));
        }

        if (categoryDetails == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Request body cannot be null."));
        }

        String name = categoryDetails.getCategoryname();
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Category name cannot be empty."));
        }
        if (name.trim().length() > 100) {
            return ResponseEntity.badRequest().body(Map.of("message", "Category name must not exceed 100 characters."));
        }

        String images = categoryDetails.getImages();
        if (images != null) {
            if (images.trim().length() > 500) {
                return ResponseEntity.badRequest().body(Map.of("message", "Image path must not exceed 500 characters."));
            }
            if (images.contains("..")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid image reference."));
            }
            existingCategory.setImages(images.trim());
        }

        Category duplicate = categoryService.findByName(name.trim());
        if (duplicate != null && duplicate.getCategoryid() != id) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Category name already exists."));
        }

        existingCategory.setCategoryname(name.trim());
        existingCategory.setStatus(categoryDetails.getStatus());

        categoryService.update(existingCategory);
        return ResponseEntity.ok(existingCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable("id") int id) {
        Category existingCategory = categoryService.findById(id);
        if (existingCategory == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Category not found"));
        }

        if (categoryService.isCategoryInUse(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message", "Category is currently in use by products and cannot be deleted.")
            );
        }

        boolean deleted = categoryService.delete(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Unable to delete category.")
            );
        }
    }
}
