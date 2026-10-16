package com.surplus.capacity.service;

import com.surplus.capacity.entity.Booking;
import com.surplus.capacity.entity.BookingStatus;
import com.surplus.capacity.entity.Resource;
import com.surplus.capacity.repository.BookingRepository;
import com.surplus.capacity.repository.DisputeRepository;
import com.surplus.capacity.repository.ResourceRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private final ResourceRepository resourceRepository;
    private final BookingRepository bookingRepository;
    private final DisputeRepository disputeRepository;

    public AnalyticsService(
            ResourceRepository resourceRepository,
            BookingRepository bookingRepository,
            DisputeRepository disputeRepository) {
        this.resourceRepository = resourceRepository;
        this.bookingRepository = bookingRepository;
        this.disputeRepository = disputeRepository;
    }

    public Map<String, Object> providerSummary(Long providerId) {
        List<Resource> resources = resourceRepository.findByProviderId(providerId);
        List<Booking> bookings = bookingRepository.findByProviderId(providerId);

        BigDecimal revenue = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED || b.getStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalResources", resources.size());
        summary.put("activeResources", resources.stream().filter(Resource::isActive).count());
        summary.put("upcomingBookings", bookings.stream().filter(b -> b.getStartTime().isAfter(LocalDateTime.now())).count());
        summary.put("completedBookings", bookings.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count());
        summary.put("revenue", revenue);
        return summary;
    }

    public Map<String, Object> adminSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalResources", resourceRepository.count());
        summary.put("totalBookings", bookingRepository.count());
        summary.put("openDisputes", disputeRepository.findByStatus(com.surplus.capacity.entity.DisputeStatus.OPEN).size());
        summary.put("confirmedBookings", bookingRepository.findAll().stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count());
        return summary;
    }
}
