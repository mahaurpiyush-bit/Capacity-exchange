package com.surplus.capacity.controller;

import com.surplus.capacity.dto.DisputeRequest;
import com.surplus.capacity.entity.Dispute;
import com.surplus.capacity.entity.DisputeStatus;
import com.surplus.capacity.entity.User;
import com.surplus.capacity.repository.UserRepository;
import com.surplus.capacity.service.DisputeService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DisputeController {

    private final DisputeService disputeService;
    private final UserRepository userRepository;

    public DisputeController(DisputeService disputeService, UserRepository userRepository) {
        this.disputeService = disputeService;
        this.userRepository = userRepository;
    }

    @PostMapping("/disputes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Dispute> createDispute(@Valid @RequestBody DisputeRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        User reporter = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Dispute dispute = disputeService.createDispute(request.getBookingId(), reporter, request.getType(), request.getDescription());
        return ResponseEntity.ok(dispute);
    }

    @PostMapping("/admin/disputes/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> resolveDispute(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        DisputeStatus status = DisputeStatus.valueOf(payload.getOrDefault("status", "RESOLVED"));
        Dispute dispute = disputeService.resolveDispute(id, payload.getOrDefault("resolution", "Resolved by admin."), status);
        return ResponseEntity.ok(Map.of("dispute", dispute));
    }

    @GetMapping("/admin/disputes")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Dispute> getOpenDisputes() {
        return disputeService.getOpenDisputes();
    }
}
