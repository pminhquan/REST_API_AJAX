package com.hcmute.springboot.service;

import com.hcmute.springboot.entity.Category;
import com.hcmute.springboot.entity.Product;
import com.hcmute.springboot.repository.CategoryRepository;
import com.hcmute.springboot.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void createProduct(Product product) {
        validateProductForCreate(product);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(int id) {
        return productRepository.findByIdWithCategory(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public void updateProduct(Product product) {
        validateProductForUpdate(product);
        productRepository.save(product);
    }

    @Override
    public void deleteProduct(int id) {
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getNewestProducts(int limit) {
        return productRepository.findNewest(PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsPage(int offset, int limit) {
        int pageIndex = limit > 0 ? offset / limit : 0;
        Page<Product> page = productRepository.findAllWithCategory(PageRequest.of(pageIndex, limit));
        return page.getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getProductsPage(Pageable pageable) {
        return productRepository.findAllWithCategory(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAllWithCategory(pageable);
        }
        return productRepository.searchByKeyword(keyword.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAllProducts() {
        return productRepository.count();
    }

    private void validateProductForCreate(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (product.getProductname() == null || product.getProductname().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (product.getPrice() <= 0 || !Double.isFinite(product.getPrice())) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if (product.getPrice() != Math.rint(product.getPrice())) {
            throw new IllegalArgumentException("Price must be a whole number");
        }
        if (product.getCategory() == null) {
            throw new IllegalArgumentException("Category is required");
        }
        int categoryId = product.getCategory().getCategoryid();
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Category must already exist");
        }
        Category existingCategory = categoryRepository.findById(categoryId).orElse(null);
        if (existingCategory == null) {
            throw new IllegalArgumentException("Category does not exist");
        }
        product.setCategory(existingCategory);
    }

    private void validateProductForUpdate(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (product.getProductname() == null || product.getProductname().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (product.getPrice() <= 0 || !Double.isFinite(product.getPrice())) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if (product.getPrice() != Math.rint(product.getPrice())) {
            throw new IllegalArgumentException("Price must be a whole number");
        }

        Product existingProduct = productRepository.findById(product.getProductid()).orElse(null);
        if (existingProduct == null) {
            throw new IllegalArgumentException("Product to update does not exist");
        }

        if (product.getCategory() == null) {
            product.setCategory(existingProduct.getCategory());
        } else {
            int categoryId = product.getCategory().getCategoryid();
            if (categoryId <= 0) {
                throw new IllegalArgumentException("Category must already exist");
            }
            Category existingCategory = categoryRepository.findById(categoryId).orElse(null);
            if (existingCategory == null) {
                throw new IllegalArgumentException("Category does not exist");
            }
            product.setCategory(existingCategory);
        }
    }
}
