package com.morningstar.old.infra.config;

import com.morningstar.old.infra.response.R;
import com.morningstar.old.infra.response.ResponseCode;
import com.morningstar.old.infra.util.WebUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    /**
     * 白名单接口(任意方法)
     */
    private static final List<String> API_WHITE_LIST_ALL_METHOD = Arrays.asList(
            "/docs/**",
            "/error",
            "/static/**",
            "/resources/**",
            "/favicon.ico"
    );
    /**
     * 白名单接口(GET方法)
     */
    private static final List<String> API_WHITE_LIST_GET_METHOD = Collections.singletonList(
            "/demo/white"
    );
    /**
     * 匿名访问接口
     */
    private static final List<String> API_ANONYMOUS_LIST = Arrays.asList(
            "/user/auth/login",
            "/user/auth/register"
    );
    private final OncePerRequestFilter jwtAuthenticationFilter;

    /**
     * 定义密码加密、匹配器
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> WebUtil.renderJson(R.error(ResponseCode.NO_PERMISSION), response);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            R<Object> result = R.error(ResponseCode.AUTHENTICATION_FAILED);
            WebUtil.renderJson(result, response);
        };
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(API_WHITE_LIST_ALL_METHOD.toArray(new String[0])).permitAll()
                .antMatchers(HttpMethod.GET, API_WHITE_LIST_GET_METHOD.toArray(new String[0])).permitAll()
                .antMatchers(API_ANONYMOUS_LIST.toArray(new String[0])).anonymous()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
                .and()
                .cors().configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(Collections.singletonList("*"));
                    configuration.setAllowedMethods(Collections.singletonList("*"));
                    configuration.setAllowedHeaders(Collections.singletonList("*"));
                    return configuration;
                });

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        log.info(http.toString());
    }
}