package com.eventhall.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * General Spring MVC configuration.
 *
 * Note: CORS configuration now lives in {@link SecurityConfig#corsConfigurationSource()}
 * so that Spring Security and Spring MVC agree on a single set of CORS rules.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
}