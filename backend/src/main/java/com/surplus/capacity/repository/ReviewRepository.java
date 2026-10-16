package com.surplus.capacity.repository;

import com.surplus.capacity.entity.Booking;
import com.surplus.capacity.entity.Review;
import com.surplus.capacity.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByReviewee(User reviewee);
    Optional<Review> findByBookingAndReviewer(Booking booking, User reviewer);
}
