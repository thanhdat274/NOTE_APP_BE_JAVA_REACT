package com.noteappapi.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // Thay thế "*" bằng các nguồn gốc mà bạn muốn cho phép
        corsConfiguration.addAllowedOrigin("http://127.0.0.1:4000");

        // Cho phép sử dụng Cookie
        corsConfiguration.setAllowCredentials(true);

        // Cấu hình các phương thức HTTP cho phép (GET, POST, PUT, DELETE, vv.)
        corsConfiguration.addAllowedMethod("*");

        // Cấu hình các tiêu đề yêu cầu được phép
        corsConfiguration.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(source);
    }
}
