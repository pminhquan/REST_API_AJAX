package com.hcmute.springboot.service;

import com.hcmute.springboot.entity.Category;
import com.hcmute.springboot.repository.CategoryRepository;
import com.hcmute.springboot.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void insert(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        categoryRepository.save(category);
    }

    @Override
    public void update(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        categoryRepository.save(category);
    }

    @Override
    public boolean delete(int id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Category findById(int id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCategoryInUse(int categoryId) {
        return productRepository.countByCategory_Categoryid(categoryId) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Category findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return categoryRepository.findByCategorynameIgnoreCase(name.trim()).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Category> searchCategories(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return categoryRepository.findAll(pageable);
        }
        return categoryRepository.findByCategorynameContainingIgnoreCase(keyword.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Category> getCategoriesPage(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }
}
