package com.surplus.capacity.service;

import com.surplus.capacity.dto.BookingRequest;
import com.surplus.capacity.entity.Booking;
import com.surplus.capacity.entity.BookingStatus;
import com.surplus.capacity.entity.Payment;
import com.surplus.capacity.entity.PaymentStatus;
import com.surplus.capacity.entity.Resource;
import com.surplus.capacity.entity.User;
import com.surplus.capacity.repository.BookingRepository;
import com.surplus.capacity.repository.PaymentRepository;
import com.surplus.capacity.repository.ResourceRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final PaymentRepository paymentRepository;
    private final AvailabilityService availabilityService;

    public BookingService(
            BookingRepository bookingRepository,
            ResourceRepository resourceRepository,
            PaymentRepository paymentRepository,
            AvailabilityService availabilityService) {
        this.bookingRepository = bookingRepository;
        this.resourceRepository = resourceRepository;
        this.paymentRepository = paymentRepository;
        this.availabilityService = availabilityService;
    }

    @Transactional
    public Booking createBooking(BookingRequest request, User seeker) {
        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!resource.isActive()) {
            throw new RuntimeException("Resource is not active");
        }
        if (resource.getProvider().getId().equals(seeker.getId())) {
            throw new RuntimeException("Provider cannot book their own resource");
        }
        if (!availabilityService.isSlotAvailable(resource, request.getStartTime(), request.getEndTime(), request.getRequestedCapacity())) {
            throw new RuntimeException("Requested slot exceeds available capacity");
        }

        BigDecimal hours = BigDecimal.valueOf(Duration.between(request.getStartTime(), request.getEndTime()).toMinutes() / 60.0)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPrice = resource.getPricePerHour().multiply(hours);

        Booking booking = new Booking();
        booking.setResource(resource);
        booking.setSeeker(seeker);
        booking.setProvider(resource.getProvider());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setRequestedCapacity(request.getRequestedCapacity());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.REQUESTED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking approveBooking(Long bookingId, User provider) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized booking approval");
        }
        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new RuntimeException("Only requested bookings can be approved");
        }
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking rejectBooking(Long bookingId, User provider) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized booking rejection");
        }
        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new RuntimeException("Only requested bookings can be rejected");
        }
        booking.setStatus(BookingStatus.REJECTED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, User actor) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        boolean isSeeker = booking.getSeeker().getId().equals(actor.getId());
        boolean isProvider = booking.getProvider().getId().equals(actor.getId());
        if (!isSeeker && !isProvider) {
            throw new RuntimeException("Unauthorized booking cancellation");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.REJECTED || booking.getStatus() == BookingStatus.EXPIRED) {
            throw new RuntimeException("Booking already inactive");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking completeBooking(Long bookingId, User provider) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized booking completion");
        }
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Only paid or confirmed bookings can be completed");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsForUser(User user) {
        return bookingRepository.findBySeekerIdOrProviderId(user.getId(), user.getId());
    }

    @Transactional
    public Payment initiatePayment(Long bookingId, User seeker) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getSeeker().getId().equals(seeker.getId())) {
            throw new RuntimeException("Only the seeker can pay for this booking");
        }

        Payment existing = paymentRepository.findByBooking(booking).orElse(null);
        if (existing != null && existing.getStatus() == PaymentStatus.SUCCEEDED) {
            throw new RuntimeException("Payment already succeeded for this booking");
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setProviderRef("SIM-" + bookingId + "-" + System.currentTimeMillis());
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment markPaymentSucceeded(Long bookingId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        Booking booking = payment.getBooking();
        if (!booking.getId().equals(bookingId)) {
            throw new RuntimeException("Payment does not match booking");
        }
        payment.setStatus(PaymentStatus.SUCCEEDED);
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment markPaymentFailed(Long bookingId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        Booking booking = payment.getBooking();
        if (!booking.getId().equals(bookingId)) {
            throw new RuntimeException("Payment does not match booking");
        }
        payment.setStatus(PaymentStatus.FAILED);
        booking.setStatus(BookingStatus.PAYMENT_FAILED);
        bookingRepository.save(booking);
        return paymentRepository.save(payment);
    }
}
