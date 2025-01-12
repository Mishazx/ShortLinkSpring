package ru.mishazx.shortlinkspring.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import ru.mishazx.shortlinkspring.service.AuthenticationService;
import ru.mishazx.shortlinkspring.service.OAuth2UserProcessingService;
import ru.mishazx.shortlinkspring.security.VkOAuth2AccessTokenResponseConverter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import ru.mishazx.shortlinkspring.security.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationService authenticationService;
    private final OAuth2UserProcessingService oAuth2UserProcessingService;
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient tokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
        
        OAuth2AccessTokenResponseHttpMessageConverter tokenResponseHttpMessageConverter = 
            new OAuth2AccessTokenResponseHttpMessageConverter();
        tokenResponseHttpMessageConverter.setAccessTokenResponseConverter(new VkOAuth2AccessTokenResponseConverter());
        
        RestTemplate restTemplate = new RestTemplate(Arrays.asList(
            new FormHttpMessageConverter(),
            tokenResponseHttpMessageConverter
        ));
        
        tokenResponseClient.setRestOperations(restTemplate);
        
        return tokenResponseClient;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/css/**", 
                    "/js/**", 
                    "/images/**", 
                    "/webjars/**",
                    "/favicon.ico",
                    "/error"
                ).permitAll()
                .requestMatchers("/", "/auth/**", "/oauth2/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/auth/login")
                .defaultSuccessUrl("/", true)
                .tokenEndpoint(token -> token
                    .accessTokenResponseClient(accessTokenResponseClient())
                )
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .failureHandler((request, response, exception) -> {
                    exception.printStackTrace();
                    String errorMessage = "Authentication failed";
                    if (exception != null && exception.getMessage() != null) {
                        errorMessage = exception.getMessage();
                    }
                    response.sendRedirect("/auth/login?error=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
                })
                .successHandler((request, response, authentication) -> {
                    try {
                        UserDetails userDetails = authenticationService.authenticateUser(authentication);
                        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(newAuth);
                        response.sendRedirect("/");
                    } catch (Exception e) {
                        response.sendRedirect("/auth/login?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                    }
                })
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/")
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .expiredUrl("/auth/login?expired")
            )
            .rememberMe(remember -> remember
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400)
            );

        return http.build();
    }
}