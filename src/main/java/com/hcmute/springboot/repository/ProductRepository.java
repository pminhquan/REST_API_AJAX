package com.hcmute.springboot.repository;

import com.hcmute.springboot.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.productid = :id")
    Optional<Product> findByIdWithCategory(@Param("id") int id);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category ORDER BY p.productid DESC",
           countQuery = "SELECT COUNT(p) FROM Product p")
    Page<Product> findAllWithCategory(Pageable pageable);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category ORDER BY p.createdAt DESC, p.productid DESC")
    List<Product> findNewest(Pageable pageable);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category WHERE LOWER(p.productname) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY p.productid DESC",
           countQuery = "SELECT COUNT(p) FROM Product p WHERE LOWER(p.productname) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    long countByCategory_Categoryid(int categoryId);
}
