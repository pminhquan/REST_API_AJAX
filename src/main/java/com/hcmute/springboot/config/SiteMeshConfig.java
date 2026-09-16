package com.hcmute.springboot.config;

import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SiteMeshConfig {

    @Bean
    public FilterRegistrationBean<ConfigurableSiteMeshFilter> siteMeshFilter() {
        FilterRegistrationBean<ConfigurableSiteMeshFilter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new ConfigurableSiteMeshFilter() {
            @Override
            protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
                builder.addExcludedPath("/assets/*")
                       .addExcludedPath("/assets/**")
                       .addExcludedPath("/uploads/*")
                       .addExcludedPath("/uploads/**")
                       // Admin decorator
                       .addDecoratorPath("/admin", "admin.jsp")
                       .addDecoratorPath("/admin/*", "admin.jsp")
                       .addDecoratorPath("/admin/**", "admin.jsp")
                       .addDecoratorPath("/categories", "admin.jsp")
                       .addDecoratorPath("/categories/*", "admin.jsp")
                       .addDecoratorPath("/categories/**", "admin.jsp")
                       // Profile decorator
                       .addDecoratorPath("/profile", "profile.jsp")
                       .addDecoratorPath("/profile/*", "profile.jsp")
                       .addDecoratorPath("/profile/**", "profile.jsp")
                       // Main decorator for customer & public pages
                       .addDecoratorPath("/", "main.jsp")
                       .addDecoratorPath("/home", "main.jsp")
                       .addDecoratorPath("/product", "main.jsp")
                       .addDecoratorPath("/products", "admin.jsp")
                       .addDecoratorPath("/products/**", "admin.jsp");
            }
        });
        filter.addUrlPatterns("/*");
        filter.setOrder(1);
        return filter;
    }
}
