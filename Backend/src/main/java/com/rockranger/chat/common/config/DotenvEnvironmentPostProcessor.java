package com.rockranger.chat.common.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> envMap = DotenvLoader.loadToMap();
        if (!envMap.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", envMap));
        }
    }
}
