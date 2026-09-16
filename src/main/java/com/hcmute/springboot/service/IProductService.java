package com.hcmute.springboot.service;

import com.hcmute.springboot.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductService {

    void createProduct(Product product);

    Product getProductById(int id);

    List<Product> getAllProducts();

    void updateProduct(Product product);

    void deleteProduct(int id);

    List<Product> getNewestProducts(int limit);

    List<Product> getProductsPage(int offset, int limit);

    Page<Product> getProductsPage(Pageable pageable);

    Page<Product> searchProducts(String keyword, Pageable pageable);

    long countAllProducts();
}
