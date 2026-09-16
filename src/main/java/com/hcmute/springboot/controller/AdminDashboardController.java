package com.hcmute.springboot.controller;

import com.hcmute.springboot.entity.Category;
import com.hcmute.springboot.entity.Product;
import com.hcmute.springboot.service.ICategoryService;
import com.hcmute.springboot.service.IProductService;
import com.hcmute.springboot.service.IUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AdminDashboardController {

    private static final int DEFAULT_RECENT_PRODUCTS_LIMIT = 5;

    private final IUserService userService;
    private final IProductService productService;
    private final ICategoryService categoryService;

    public AdminDashboardController(IUserService userService, IProductService productService, ICategoryService categoryService) {
        this.userService = userService;
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping({"/admin/dashboard", "/admin"})
    public String showDashboard(Model model) {
        long totalUsers = userService.countAllUsers();
        long totalProducts = productService.countAllProducts();
        List<Category> categories = categoryService.findAll();
        long totalCategories = categories != null ? categories.size() : 0L;
        List<Product> recentProducts = productService.getNewestProducts(DEFAULT_RECENT_PRODUCTS_LIMIT);

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("recentProducts", recentProducts);

        return "admin/dashboard";
    }
}
