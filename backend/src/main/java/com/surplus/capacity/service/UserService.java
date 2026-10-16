package com.surplus.capacity.service;

import com.surplus.capacity.dto.RegisterRequest;
import com.surplus.capacity.dto.UserDto;
import com.surplus.capacity.entity.Role;
import com.surplus.capacity.entity.User;
import com.surplus.capacity.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCompanyName(request.getCompanyName());
        user.setPhone(request.getPhone());

        Set<Role> roles = new HashSet<>();
        roles.add(Role.SEEKER);
        roles.add(Role.PROVIDER);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserDto getCurrentUser(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setCompanyName(user.getCompanyName());
        dto.setPhone(user.getPhone());
        dto.setRoles(user.getRoles());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
