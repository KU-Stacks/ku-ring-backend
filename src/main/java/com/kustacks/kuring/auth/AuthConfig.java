package com.kustacks.kuring.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kustacks.kuring.admin.business.AdminDetailsService;
import com.kustacks.kuring.auth.authorization.AuthenticationPrincipalArgumentResolver;
import com.kustacks.kuring.auth.context.SecurityContextPersistenceFilter;
import com.kustacks.kuring.auth.interceptor.AdminTokenAuthenticationFilter;
import com.kustacks.kuring.auth.interceptor.BearerTokenAuthenticationFilter;
import com.kustacks.kuring.auth.token.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AuthConfig implements WebMvcConfigurer {

    private final AdminDetailsService adminDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SecurityContextPersistenceFilter());
        registry.addInterceptor(new AdminTokenAuthenticationFilter(adminDetailsService, jwtTokenProvider, objectMapper)).addPathPatterns("/api/v2/admin/login");
        registry.addInterceptor(new BearerTokenAuthenticationFilter(jwtTokenProvider)).addPathPatterns("/api/v2/admin/login/request");
    }

    @Override
    public void addArgumentResolvers(List argumentResolvers) {
        argumentResolvers.add(new AuthenticationPrincipalArgumentResolver());
    }
}