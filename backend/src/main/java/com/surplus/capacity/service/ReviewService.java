package com.surplus.capacity.service;

import com.surplus.capacity.entity.Booking;
import com.surplus.capacity.entity.BookingStatus;
import com.surplus.capacity.entity.Review;
import com.surplus.capacity.entity.User;
import com.surplus.capacity.repository.BookingRepository;
import com.surplus.capacity.repository.ReviewRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public Review createReview(Long bookingId, User reviewer, Integer rating, String comment) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getSeeker().getId().equals(reviewer.getId()) && !booking.getProvider().getId().equals(reviewer.getId())) {
            throw new RuntimeException("Reviewer is not part of this booking");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new RuntimeException("Only completed bookings can be reviewed");
        }
        if (reviewRepository.findByBookingAndReviewer(booking, reviewer).isPresent()) {
            throw new RuntimeException("Duplicate review for this booking by the same reviewer");
        }

        User reviewee = booking.getSeeker().getId().equals(reviewer.getId()) ? booking.getProvider() : booking.getSeeker();

        Review review = new Review();
        review.setBooking(booking);
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setRating(rating);
        review.setComment(comment);
        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewsForUser(User user) {
        return reviewRepository.findByReviewee(user);
    }
}
