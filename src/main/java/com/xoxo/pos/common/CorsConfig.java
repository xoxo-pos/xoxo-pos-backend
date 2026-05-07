package com.xoxo.pos.common;
import org.springframework.beans.factory.annotation.Value;import org.springframework.context.annotation.*;import org.springframework.web.servlet.config.annotation.*;
@Configuration public class CorsConfig{@Value("${app.cors.allowed-origin}") private String allowedOrigin;@Bean WebMvcConfigurer corsConfigurer(){return new WebMvcConfigurer(){public void addCorsMappings(CorsRegistry r){r.addMapping("/api/**").allowedOrigins(allowedOrigin).allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS").allowedHeaders("*").allowCredentials(true);}};}}
