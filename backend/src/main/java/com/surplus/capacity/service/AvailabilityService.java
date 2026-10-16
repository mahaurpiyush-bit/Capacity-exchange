package com.surplus.capacity.service;

import com.surplus.capacity.entity.Booking;
import com.surplus.capacity.entity.BookingStatus;
import com.surplus.capacity.entity.Resource;
import com.surplus.capacity.repository.BookingRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

    private final BookingRepository bookingRepository;

    public AvailabilityService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public boolean isSlotAvailable(Resource resource, LocalDateTime start, LocalDateTime end, Integer requestedCapacity) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new IllegalArgumentException("Invalid booking time range");
        }
        if (requestedCapacity == null || requestedCapacity <= 0 || requestedCapacity > resource.getTotalCapacity()) {
            throw new IllegalArgumentException("Requested capacity exceeds resource limit");
        }

        List<Booking> overlapping = bookingRepository.findByResourceAndStartTimeLessThanAndEndTimeGreaterThan(resource, end, start);
        int usedCapacity = 0;
        for (Booking booking : overlapping) {
            if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.ACTIVE || booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
                usedCapacity += booking.getRequestedCapacity();
            }
        }

        return usedCapacity + requestedCapacity <= resource.getTotalCapacity();
    }
}
