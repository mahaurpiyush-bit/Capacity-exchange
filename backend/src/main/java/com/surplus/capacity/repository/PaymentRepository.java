package com.surplus.capacity.repository;

import com.surplus.capacity.entity.Booking;
import com.surplus.capacity.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBooking(Booking booking);
}
