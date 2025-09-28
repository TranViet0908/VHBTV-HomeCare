// src/main/java/Project/HouseService/Config/MultipartConfig.java
package Project.HouseService.Config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import jakarta.servlet.MultipartConfigElement;

@Configuration
public class MultipartConfig {
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory f = new MultipartConfigFactory();
        f.setMaxFileSize(DataSize.ofMegabytes(10));
        f.setMaxRequestSize(DataSize.ofMegabytes(10));
        f.setFileSizeThreshold(DataSize.ofMegabytes(1));
        return f.createMultipartConfig();
    }
}
