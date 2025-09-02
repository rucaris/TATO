package com.tato.service;

import com.tato.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var u = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("not found: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())      // username = email
                .password(u.getPassword())       // BCrypt
                .roles(u.getRole())              // "USER" / "ADMIN"
                .build();
    }
}
