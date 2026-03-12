package com.example.attendance_Backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // NOTE: CORS is handled by SecurityConfig via CorsConfigurationSource bean.
    // Defining it here as well caused a conflict; Security-level CORS always wins.

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploads folder as static resource
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}

