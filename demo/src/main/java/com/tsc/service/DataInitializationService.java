package com.tsc.service;

import com.tsc.model.Role;
import com.tsc.model.User;
import com.tsc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Profile("!prod") // Only run this in non-production environments
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultUsers();
    }

    private void initializeDefaultUsers() {
        // Create default admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", passwordEncoder.encode("admin"), Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Default admin user created with username: admin, password: admin");
        }

        // Create default regular user if not exists
        if (!userRepository.existsByUsername("user")) {
            User user = new User("user", passwordEncoder.encode("user"), Role.USER);
            userRepository.save(user);
            System.out.println("Default user created with username: user, password: user");
        }
    }
}