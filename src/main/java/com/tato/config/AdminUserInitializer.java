package com.tato.config;

import com.tato.model.User;
import com.tato.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            User adminUser = new User();
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword(passwordEncoder.encode("password"));
            adminUser.setRole("ADMIN");
            adminUser.setNickname("관리자");

            userRepository.save(adminUser);
            System.out.println("Admin user 'admin@example.com' created.");
        } else {
            System.out.println("Admin user 'admin@example.com' already exists.");
        }
    }
}