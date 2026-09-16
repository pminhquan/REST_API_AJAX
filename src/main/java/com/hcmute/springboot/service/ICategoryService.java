package com.hcmute.springboot.service;

import com.hcmute.springboot.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICategoryService {

    void insert(Category category);

    void update(Category category);

    boolean delete(int id);

    Category findById(int id);

    List<Category> findAll();

    boolean isCategoryInUse(int categoryId);

    Category findByName(String name);

    Page<Category> searchCategories(String keyword, Pageable pageable);

    Page<Category> getCategoriesPage(Pageable pageable);
}
