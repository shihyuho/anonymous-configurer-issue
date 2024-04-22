package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class WorkaroundTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void shouldReturnMyCustomAnonymousConfig() throws Exception {
        this.mockMvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(content().string("myAnonymousUser"));
    }

    @EnableWebSecurity
    @TestConfiguration
    static class MvcMatcherServletPathConfig {

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .with(new MyCustomDsl(), withDefaults())
                    .build();
        }

        @RestController
        static class PathController {

            @RequestMapping("/api")
            String api() {
                return SecurityContextHolder.getContext().getAuthentication().getName();
            }
        }
    }

    /**
     * https://docs.spring.io/spring-security/reference/servlet/configuration/java.html#jc-custom-dsls
     */
    public static class MyCustomDsl extends AbstractHttpConfigurer<MyCustomDsl, HttpSecurity> {

        @Override
        public void init(HttpSecurity http) throws Exception {
            // any method that adds another configurer
            // must be done in the init method
            http
                    .anonymous(anonymous -> anonymous.authenticationFilter(null)
                            .principal("myAnonymousUser")
                            .init(http));
        }
    }
}
