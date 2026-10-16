package com.surplus.capacity.config;

import com.surplus.capacity.entity.Resource;
import com.surplus.capacity.entity.Role;
import com.surplus.capacity.entity.User;
import com.surplus.capacity.repository.ResourceRepository;
import com.surplus.capacity.repository.UserRepository;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("default")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class LocalDataInitializer {

    @Bean
    CommandLineRunner seedLocalData(
            UserRepository userRepository,
            ResourceRepository resourceRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            User provider = userRepository.findByEmail("provider@test.com").orElseGet(() -> {
                User user = new User();
                user.setFullName("Demo Provider");
                user.setEmail("provider@test.com");
                user.setPassword(passwordEncoder.encode("secret123"));
                user.setCompanyName("Demo Capacity Co.");
                user.setPhone("9999999999");
                user.setRoles(new HashSet<>(Set.of(Role.PROVIDER, Role.SEEKER)));
                return userRepository.save(user);
            });

            if (resourceRepository.findByProvider(provider).isEmpty()) {
                Resource resource = new Resource();
                resource.setName("Flexible Production Kitchen");
                resource.setDescription("A commercially equipped kitchen available during unused production windows.");
                resource.setCategory("Kitchen");
                resource.setPricePerHour(new BigDecimal("850.00"));
                resource.setTotalCapacity(12);
                resource.setLocation("Bengaluru");
                resource.setProvider(provider);
                resourceRepository.save(resource);
            }

            if (userRepository.findByEmail("seeker@test.com").isEmpty()) {
                User seeker = new User();
                seeker.setFullName("Demo Seeker");
                seeker.setEmail("seeker@test.com");
                seeker.setPassword(passwordEncoder.encode("secret123"));
                seeker.setCompanyName("Demo Seeker Co.");
                seeker.setPhone("8888888888");
                seeker.setRoles(new HashSet<>(Set.of(Role.SEEKER)));
                userRepository.save(seeker);
            }
        };
    }
}
