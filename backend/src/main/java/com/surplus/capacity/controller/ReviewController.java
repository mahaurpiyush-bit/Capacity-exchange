package com.surplus.capacity.controller;

import com.surplus.capacity.dto.ReviewRequest;
import com.surplus.capacity.entity.Review;
import com.surplus.capacity.entity.User;
import com.surplus.capacity.repository.UserRepository;
import com.surplus.capacity.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    public ReviewController(ReviewService reviewService, UserRepository userRepository) {
        this.reviewService = reviewService;
        this.userRepository = userRepository;
    }

    @PostMapping("/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Review> createReview(@Valid @RequestBody ReviewRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        User reviewer = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Review review = reviewService.createReview(request.getBookingId(), reviewer, request.getRating(), request.getComment());
        return ResponseEntity.ok(review);
    }

    @GetMapping("/users/{userId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public List<Review> getReviews(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reviewService.getReviewsForUser(user);
    }
}
