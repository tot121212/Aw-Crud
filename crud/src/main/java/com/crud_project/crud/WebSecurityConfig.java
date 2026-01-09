package com.crud_project.crud;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.crud_project.crud.repository.UserRepo;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final UserRepo userRepo;

    @Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests((requests) -> requests
                .requestMatchers("/", "/home", "/auth/register", "/auth/login", "/css/**", "/data/**").permitAll()
                .anyRequest().authenticated())
			.formLogin((form) -> form
				.loginPage("/auth/login")
                .defaultSuccessUrl("/crud", true)
                .failureUrl("/auth/login?error=true")
				.permitAll()
			)
			.logout(LogoutConfigurer::permitAll);
		return http.build();
	}

    @Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

    // Purpose: load user data from db for Spring Security
    @Bean
    @SuppressWarnings("Convert2Lambda") // fuck that
    UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) {
                com.crud_project.crud.entity.User user = userRepo
                    .findByUserName(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
                
                return User.builder()
                    .username(user.getUserName())
                    .password(user.getHashedPassword())
                    .roles("USER")
                    .build();
            }
        };
    }
}