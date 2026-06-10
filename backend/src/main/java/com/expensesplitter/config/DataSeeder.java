package com.expensesplitter.config;

import com.expensesplitter.model.User;
import com.expensesplitter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail("ammad@gmail.com")) {
            User ammad = User.builder()
                    .name("Ammad")
                    .email("ammad@gmail.com")
                    .password(passwordEncoder.encode("ammad123"))
                    .build();
            userRepository.save(ammad);
            System.out.println(">>> Seeded demo user: Ammad (ammad@gmail.com) with password 'ammad123'");
        }
    }
}
