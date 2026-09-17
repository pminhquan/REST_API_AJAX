package com.hcmute.springboot.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmute.springboot.entity.Category;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer categoryJsonCustomizer() {
        return builder -> builder.addMixIn(Category.class, CategoryMixin.class);
    }

    @JsonIgnoreProperties({"products", "hibernateLazyInitializer", "handler"})
    static abstract class CategoryMixin {
    }
}
