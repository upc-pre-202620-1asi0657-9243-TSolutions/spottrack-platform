package com.spottrack.platform.shared.infrastructure.cors;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                    "http://localhost:*",
                    "https://lively-ground-08011af0f.7.azurestaticapps.net",
                    "https://ashy-meadow-0d9e60a10.7.azurestaticapps.net",
                    "https://upc-pre-202610-1asi0729-11881-spottrack.github.io"
                )
                .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
