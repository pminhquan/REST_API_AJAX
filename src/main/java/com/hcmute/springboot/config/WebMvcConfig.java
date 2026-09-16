package com.hcmute.springboot.config;

import org.apache.catalina.core.StandardContext;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addContextCustomizers(context -> {
            if (context instanceof StandardContext standardContext) {
                standardContext.setSuspendWrappedResponseAfterForward(false);
            }
        });
    }

    @Bean
    public InternalResourceViewResolver jspViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        resolver.setAlwaysInclude(true);
        return resolver;
    }

    private final AuthenticationInterceptor authenticationInterceptor;
    private final CsrfInterceptor csrfInterceptor;

    public WebMvcConfig(AuthenticationInterceptor authenticationInterceptor, CsrfInterceptor csrfInterceptor) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.csrfInterceptor = csrfInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(csrfInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/assets/**", "/uploads/**");

        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns(
                        "/profile", "/profile/**",
                        "/categories", "/categories/**",
                        "/products", "/products/**",
                        "/admin/**"
                )
                .excludePathPatterns(
                        "/product",
                        "/products/detail",
                        "/assets/**",
                        "/uploads/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("/assets/");
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("/uploads/");
    }
}
