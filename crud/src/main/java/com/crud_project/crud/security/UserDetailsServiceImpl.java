package com.crud_project.crud.security;

import java.util.Set;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.crud_project.crud.entity.User;
import com.crud_project.crud.repository.UserRepo;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService{
    private final UserRepo userRepo;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final User jpaUser = userRepo
            .findByUserName(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        return CustomUserDetails.builder()
            .username(jpaUser.getUserName())
            .password(jpaUser.getHashedPassword())
            .authorities(Set.of("USER"))
            .id(jpaUser.getId())
            .build();
    }
}
