package com.surplus.capacity.controller;

import com.surplus.capacity.entity.Resource;
import com.surplus.capacity.entity.User;
import com.surplus.capacity.repository.ResourceRepository;
import com.surplus.capacity.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ResourceController {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ResourceController(ResourceRepository resourceRepository, UserRepository userRepository) {
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/resources")
    public List<Resource> getResources() {
        return resourceRepository.findByActiveTrue();
    }

    @GetMapping("/resources/mine")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public List<Resource> getMyResources(@AuthenticationPrincipal UserDetails userDetails) {
        User provider = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return resourceRepository.findByProviderId(provider.getId());
    }

    @GetMapping("/resources/{id}")
    public Resource getResource(@PathVariable Long id) {
        return resourceRepository.findById(id).orElseThrow(() -> new RuntimeException("Resource not found"));
    }

    @PostMapping("/resources")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<Resource> createResource(@Valid @RequestBody Resource resource, @AuthenticationPrincipal UserDetails userDetails) {
        User provider = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        resource.setProvider(provider);
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceRepository.save(resource));
    }

    @PutMapping("/resources/{id}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public Resource updateResource(@PathVariable Long id, @RequestBody Resource updated, @AuthenticationPrincipal UserDetails userDetails) {
        Resource existing = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        User provider = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!provider.getId().equals(existing.getProvider().getId()) && !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("Not authorized to update this resource");
        }
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setCategory(updated.getCategory());
        existing.setPricePerHour(updated.getPricePerHour());
        existing.setTotalCapacity(updated.getTotalCapacity());
        existing.setLocation(updated.getLocation());
        existing.setActive(updated.isActive());
        return resourceRepository.save(existing);
    }

    @DeleteMapping("/resources/{id}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        User provider = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!provider.getId().equals(resource.getProvider().getId()) && !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("Not authorized to delete this resource");
        }
        resourceRepository.delete(resource);
        return ResponseEntity.noContent().build();
    }
}
