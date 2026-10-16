package com.surplus.capacity.repository;

import com.surplus.capacity.entity.Resource;
import com.surplus.capacity.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByProvider(User provider);
    List<Resource> findByProviderId(Long providerId);
    List<Resource> findByActiveTrue();
}
