package com.hcmute.springboot.controller;

import com.hcmute.springboot.entity.Product;
import com.hcmute.springboot.service.IProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final IProductService productService;

    public HomeController(IProductService productService) {
        this.productService = productService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        List<Product> newestProducts = productService.getNewestProducts(10);
        model.addAttribute("newestProducts", newestProducts);
        return "home";
    }
}
