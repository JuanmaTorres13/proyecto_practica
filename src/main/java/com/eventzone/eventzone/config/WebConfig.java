package com.eventzone.eventzone.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:C:\\Users\\wul4p\\Desktop\\proyecto_practicas\\eventzone\\uploads")
                .setCachePeriod(0); // <--- desactiva caché
    }
}
