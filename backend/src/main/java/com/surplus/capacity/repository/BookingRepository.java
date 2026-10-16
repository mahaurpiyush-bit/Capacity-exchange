package com.surplus.capacity.repository;

import com.surplus.capacity.entity.Booking;
import com.surplus.capacity.entity.BookingStatus;
import com.surplus.capacity.entity.Resource;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByResourceAndStatus(Resource resource, BookingStatus status);
    List<Booking> findByResourceAndStartTimeLessThanAndEndTimeGreaterThan(Resource resource, LocalDateTime end, LocalDateTime start);
    List<Booking> findByProviderId(Long providerId);
    List<Booking> findBySeekerId(Long seekerId);
    List<Booking> findBySeekerIdOrProviderId(Long seekerId, Long providerId);
}
