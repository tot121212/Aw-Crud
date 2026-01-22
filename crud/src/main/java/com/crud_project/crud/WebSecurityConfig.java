package com.crud_project.crud;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.crud_project.crud.controller.SessionKeys;
import com.crud_project.crud.entity.PageState;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final UserDetailsServiceImpl userDetailsService;

    private void setupInitialState(Authentication authentication, HttpSession session){
        session.setAttribute(SessionKeys.CUR_USER_NAME, authentication.getName());
        session.setAttribute(SessionKeys.CUR_USER_PAGE_STATE, new PageState());
    }

    @Bean
    AuthenticationSuccessHandler loginSuccessHandler() {
        return new SimpleUrlAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(
                HttpServletRequest request, 
                HttpServletResponse response, 
                Authentication authentication) throws IOException 
            {
                HttpSession session = request.getSession();
                setupInitialState(authentication, session);
                getRedirectStrategy().sendRedirect(request, response, "/crud");
            }
        };
    }

    @Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests((requests) -> requests
                .requestMatchers("/", "/home", "/auth/register", "/auth/login", "/css/**", "/data/**", "/js/**", "/svg/**")
                .permitAll()
                .anyRequest().authenticated())
			.formLogin((form) -> form
				.loginPage("/auth/login")
                .successHandler(loginSuccessHandler())
                .failureUrl("/auth/login?error=true")
				.permitAll()
			)
			.logout(LogoutConfigurer::permitAll)
            .userDetailsService(userDetailsService);
		return http.build();
	}

    @Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}