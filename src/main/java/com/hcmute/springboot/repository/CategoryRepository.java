package com.hcmute.springboot.repository;

import com.hcmute.springboot.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findByCategorynameIgnoreCase(String categoryname);

    boolean existsByCategorynameIgnoreCase(String categoryname);

    Page<Category> findByCategorynameContainingIgnoreCase(String keyword, Pageable pageable);
}
