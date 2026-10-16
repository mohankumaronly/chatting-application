package com.rockranger.chat.attachment.infrastructure;

import com.cloudinary.Cloudinary;
import com.rockranger.chat.common.config.AppProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(AppProperties appProperties) {
        AppProperties.CloudinaryProperties props = appProperties.getCloudinary();
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", props.getCloudName() != null ? props.getCloudName() : "");
        config.put("api_key", props.getApiKey() != null ? props.getApiKey() : "");
        config.put("api_secret", props.getApiSecret() != null ? props.getApiSecret() : "");
        return new Cloudinary(config);
    }
}
