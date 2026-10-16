package com.surplus.capacity.controller;

import com.surplus.capacity.dto.BookingRequest;
import com.surplus.capacity.entity.Booking;
import com.surplus.capacity.entity.Payment;
import com.surplus.capacity.entity.User;
import com.surplus.capacity.repository.BookingRepository;
import com.surplus.capacity.repository.UserRepository;
import com.surplus.capacity.service.BookingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public BookingController(BookingService bookingService, UserRepository userRepository, BookingRepository bookingRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @PostMapping("/bookings")
    @PreAuthorize("hasRole('SEEKER') or hasRole('PROVIDER')")
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody BookingRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        User seeker = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(bookingService.createBooking(request, seeker));
    }

    @GetMapping("/bookings")
    @PreAuthorize("isAuthenticated()")
    public List<Booking> getMyBookings(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bookingService.getBookingsForUser(user);
    }

    @GetMapping("/bookings/{id}")
    @PreAuthorize("isAuthenticated()")
    public Booking getBooking(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getSeeker().getId().equals(user.getId()) && !booking.getProvider().getId().equals(user.getId()) && !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("Unauthorized booking access");
        }
        return booking;
    }

    @PostMapping("/bookings/{id}/approve")
    @PreAuthorize("hasRole('PROVIDER')")
    public Booking approveBooking(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User provider = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bookingService.approveBooking(id, provider);
    }

    @PostMapping("/bookings/{id}/reject")
    @PreAuthorize("hasRole('PROVIDER')")
    public Booking rejectBooking(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User provider = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bookingService.rejectBooking(id, provider);
    }

    @PostMapping("/bookings/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public Booking cancelBooking(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User actor = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bookingService.cancelBooking(id, actor);
    }

    @PostMapping("/bookings/{id}/complete")
    @PreAuthorize("hasRole('PROVIDER')")
    public Booking completeBooking(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User provider = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bookingService.completeBooking(id, provider);
    }

    @PostMapping("/bookings/{id}/payments")
    @PreAuthorize("hasRole('SEEKER')")
    public ResponseEntity<Map<String, Object>> initiatePayment(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User seeker = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Payment payment = bookingService.initiatePayment(id, seeker);
        return ResponseEntity.ok(Map.of("payment", payment, "status", payment.getStatus()));
    }
}
