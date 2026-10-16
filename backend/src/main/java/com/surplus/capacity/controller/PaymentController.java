package com.surplus.capacity.controller;

import com.surplus.capacity.entity.Payment;
import com.surplus.capacity.repository.PaymentRepository;
import com.surplus.capacity.service.BookingService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;

    public PaymentController(PaymentRepository paymentRepository, BookingService bookingService) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
    }

    @GetMapping("/payments/{paymentId}")
    @PreAuthorize("isAuthenticated()")
    public Payment getPayment(@PathVariable Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    @PostMapping("/bookings/{bookingId}/payments/{paymentId}/success")
    @PreAuthorize("hasRole('SEEKER')")
    public ResponseEntity<Map<String, Object>> markSuccess(@PathVariable Long bookingId, @PathVariable Long paymentId) {
        return ResponseEntity.ok(Map.of("payment", bookingService.markPaymentSucceeded(bookingId, paymentId)));
    }

    @PostMapping("/bookings/{bookingId}/payments/{paymentId}/fail")
    @PreAuthorize("hasRole('SEEKER')")
    public ResponseEntity<Map<String, Object>> markFailure(@PathVariable Long bookingId, @PathVariable Long paymentId) {
        return ResponseEntity.ok(Map.of("payment", bookingService.markPaymentFailed(bookingId, paymentId)));
    }
}
