package com.expensemanager.service.impl;

import com.expensemanager.dto.UserRegistrationDto;
import com.expensemanager.entity.User;
import com.expensemanager.enums.UserRole;
import com.expensemanager.repository.UserRepository;
import com.expensemanager.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User registerNewUser(UserRegistrationDto registrationDto) {
        if (existsByUsername(registrationDto.getUsername())) {
            throw new IllegalArgumentException("Username '" + registrationDto.getUsername() + "' is already taken.");
        }

        if (existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Email '" + registrationDto.getEmail() + "' is already registered.");
        }

        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        User user = User.builder()
                .username(registrationDto.getUsername().trim())
                .email(registrationDto.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .role(UserRole.ROLE_USER)
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
