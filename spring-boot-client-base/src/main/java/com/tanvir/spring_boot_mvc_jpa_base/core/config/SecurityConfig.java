package com.tanvir.spring_boot_mvc_jpa_base.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**", "/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/client/", true)
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/logout"));

        return http.build();
    }

    @Bean
    public LogoutSuccessHandler oidcLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            String redirectUrl = "http://localhost:8000/auth/connect/logout";
            if (authentication instanceof OAuth2AuthenticationToken oauth2Auth) {
                OidcUser oidcUser = (OidcUser) oauth2Auth.getPrincipal();
                String idToken = oidcUser.getIdToken().getTokenValue();
                redirectUrl += "?id_token_hint=" + idToken;
            }
            response.sendRedirect(redirectUrl);
        };
    }
}
