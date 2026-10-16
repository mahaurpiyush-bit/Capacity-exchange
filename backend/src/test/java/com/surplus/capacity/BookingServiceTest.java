package com.surplus.capacity;

import static org.junit.jupiter.api.Assertions.*;

import com.surplus.capacity.dto.BookingRequest;
import com.surplus.capacity.entity.Booking;
import com.surplus.capacity.entity.BookingStatus;
import com.surplus.capacity.entity.Role;
import com.surplus.capacity.entity.Resource;
import com.surplus.capacity.entity.User;
import com.surplus.capacity.repository.BookingRepository;
import com.surplus.capacity.repository.ResourceRepository;
import com.surplus.capacity.repository.UserRepository;
import com.surplus.capacity.service.BookingService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void createBookingSucceedsWhenCapacityIsAvailable() {
        User provider = saveUser("provider.booking@test.com", "Provider One");
        User seeker = saveUser("seeker.booking@test.com", "Seeker One");
        Resource resource = saveResource(provider, "CNC Machine", 10, new BigDecimal("500"));

        BookingRequest request = new BookingRequest();
        request.setResourceId(resource.getId());
        request.setStartTime(LocalDateTime.of(2026, 11, 10, 9, 0));
        request.setEndTime(LocalDateTime.of(2026, 11, 10, 12, 0));
        request.setRequestedCapacity(4);

        Booking booking = bookingService.createBooking(request, seeker);

        assertNotNull(booking.getId());
        assertEquals(BookingStatus.REQUESTED, booking.getStatus());
        assertEquals(new BigDecimal("1500.00").setScale(2), booking.getTotalPrice().setScale(2));
    }

    @Test
    void conflictingBookingRejectsOverCapacity() {
        User provider = saveUser("provider.conflict@test.com", "Provider Two");
        User seeker = saveUser("seeker.conflict@test.com", "Seeker Two");
        Resource resource = saveResource(provider, "Laser Cutter", 10, new BigDecimal("600"));

        Booking current = new Booking();
        current.setResource(resource);
        current.setProvider(provider);
        current.setSeeker(seeker);
        current.setStartTime(LocalDateTime.of(2026, 11, 11, 10, 0));
        current.setEndTime(LocalDateTime.of(2026, 11, 11, 12, 0));
        current.setRequestedCapacity(8);
        current.setTotalPrice(new BigDecimal("1200"));
        current.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(current);

        BookingRequest request = new BookingRequest();
        request.setResourceId(resource.getId());
        request.setStartTime(LocalDateTime.of(2026, 11, 11, 11, 0));
        request.setEndTime(LocalDateTime.of(2026, 11, 11, 13, 0));
        request.setRequestedCapacity(5);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> bookingService.createBooking(request, seeker));

        assertTrue(ex.getMessage().contains("Requested slot exceeds available capacity"));
    }

    private User saveUser(String email, String fullName) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword("secret123");
        user.setRoles(Set.of(Role.SEEKER, Role.PROVIDER));
        return userRepository.save(user);
    }

    private Resource saveResource(User provider, String name, int totalCapacity, BigDecimal price) {
        Resource resource = new Resource();
        resource.setName(name);
        resource.setDescription("Test desc");
        resource.setCategory("Machine");
        resource.setTotalCapacity(totalCapacity);
        resource.setPricePerHour(price);
        resource.setLocation("Bengaluru");
        resource.setProvider(provider);
        resource.setActive(true);
        return resourceRepository.save(resource);
    }
}
