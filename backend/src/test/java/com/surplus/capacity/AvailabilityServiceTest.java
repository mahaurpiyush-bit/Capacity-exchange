package com.surplus.capacity;

import static org.junit.jupiter.api.Assertions.*;

import com.surplus.capacity.entity.Booking;
import com.surplus.capacity.entity.BookingStatus;
import com.surplus.capacity.entity.Resource;
import com.surplus.capacity.entity.User;
import com.surplus.capacity.repository.BookingRepository;
import com.surplus.capacity.repository.ResourceRepository;
import com.surplus.capacity.repository.UserRepository;
import com.surplus.capacity.service.AvailabilityService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AvailabilityServiceTest {

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Test
    void validSlotAvailable() {
        User provider = new User();
        provider.setFullName("Provider");
        provider.setEmail("provider@test.com");
        provider.setPassword("pass");
        userRepository.save(provider);

        Resource resource = new Resource();
        resource.setName("CNC");
        resource.setDescription("Test");
        resource.setCategory("Machine");
        resource.setPricePerHour(new BigDecimal("150"));
        resource.setTotalCapacity(10);
        resource.setLocation("Bengaluru");
        resource.setProvider(provider);
        resourceRepository.save(resource);

        LocalDateTime start = LocalDateTime.of(2026, 10, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 20, 12, 0);
        assertTrue(availabilityService.isSlotAvailable(resource, start, end, 4));
    }

    @Test
    void conflictingReservationRejects() {
        User provider = new User();
        provider.setFullName("Provider");
        provider.setEmail("provider2@test.com");
        provider.setPassword("pass");
        userRepository.save(provider);

        Resource resource = new Resource();
        resource.setName("Workshop");
        resource.setDescription("Test");
        resource.setCategory("Workshop");
        resource.setPricePerHour(new BigDecimal("200"));
        resource.setTotalCapacity(10);
        resource.setLocation("Pune");
        resource.setProvider(provider);
        resourceRepository.save(resource);

        LocalDateTime start = LocalDateTime.of(2026, 10, 21, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 21, 11, 0);
        Booking existing = new Booking();
        existing.setResource(resource);
        existing.setSeeker(provider);
        existing.setProvider(provider);
        existing.setStartTime(start);
        existing.setEndTime(end);
        existing.setRequestedCapacity(6);
        existing.setTotalPrice(new BigDecimal("1200"));
        existing.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(existing);

        assertFalse(availabilityService.isSlotAvailable(resource, start.plusHours(1), end.plusHours(1), 5));
    }

    @Test
    void adjacentSlotsAllowed() {
        User provider = new User();
        provider.setFullName("Provider");
        provider.setEmail("provider3@test.com");
        provider.setPassword("pass");
        userRepository.save(provider);

        Resource resource = new Resource();
        resource.setName("Studio");
        resource.setDescription("Test");
        resource.setCategory("Studio");
        resource.setPricePerHour(new BigDecimal("300"));
        resource.setTotalCapacity(8);
        resource.setLocation("Delhi");
        resource.setProvider(provider);
        resourceRepository.save(resource);

        LocalDateTime existingStart = LocalDateTime.of(2026, 10, 22, 9, 0);
        LocalDateTime existingEnd = LocalDateTime.of(2026, 10, 22, 11, 0);
        Booking existing = new Booking();
        existing.setResource(resource);
        existing.setSeeker(provider);
        existing.setProvider(provider);
        existing.setStartTime(existingStart);
        existing.setEndTime(existingEnd);
        existing.setRequestedCapacity(3);
        existing.setTotalPrice(new BigDecimal("900"));
        existing.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(existing);

        assertTrue(availabilityService.isSlotAvailable(resource, existingEnd, existingEnd.plusHours(1), 5));
    }

    @Test
    void capacityExhaustionRejected() {
        User provider = new User();
        provider.setFullName("Provider");
        provider.setEmail("provider4@test.com");
        provider.setPassword("pass");
        userRepository.save(provider);

        Resource resource = new Resource();
        resource.setName("Kitchen");
        resource.setDescription("Test");
        resource.setCategory("Kitchen");
        resource.setPricePerHour(new BigDecimal("400"));
        resource.setTotalCapacity(10);
        resource.setLocation("Hyderabad");
        resource.setProvider(provider);
        resourceRepository.save(resource);

        LocalDateTime start = LocalDateTime.of(2026, 10, 23, 14, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 23, 16, 0);
        Booking bookingA = new Booking();
        bookingA.setResource(resource);
        bookingA.setSeeker(provider);
        bookingA.setProvider(provider);
        bookingA.setStartTime(start);
        bookingA.setEndTime(end);
        bookingA.setRequestedCapacity(6);
        bookingA.setTotalPrice(new BigDecimal("2400"));
        bookingA.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(bookingA);

        Booking bookingB = new Booking();
        bookingB.setResource(resource);
        bookingB.setSeeker(provider);
        bookingB.setProvider(provider);
        bookingB.setStartTime(start.plusHours(1));
        bookingB.setEndTime(end.plusHours(1));
        bookingB.setRequestedCapacity(5);
        bookingB.setTotalPrice(new BigDecimal("2000"));
        bookingB.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(bookingB);

        assertFalse(availabilityService.isSlotAvailable(resource, start, end, 5));
    }
}
