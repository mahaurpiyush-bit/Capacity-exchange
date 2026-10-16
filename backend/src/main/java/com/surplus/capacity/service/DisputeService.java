package com.surplus.capacity.service;

import com.surplus.capacity.entity.Booking;
import com.surplus.capacity.entity.Dispute;
import com.surplus.capacity.entity.DisputeStatus;
import com.surplus.capacity.entity.User;
import com.surplus.capacity.repository.BookingRepository;
import com.surplus.capacity.repository.DisputeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final BookingRepository bookingRepository;

    public DisputeService(DisputeRepository disputeRepository, BookingRepository bookingRepository) {
        this.disputeRepository = disputeRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public Dispute createDispute(Long bookingId, User reporter, com.surplus.capacity.entity.DisputeType type, String description) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getSeeker().getId().equals(reporter.getId()) && !booking.getProvider().getId().equals(reporter.getId())) {
            throw new RuntimeException("Reporter is not part of this booking");
        }

        Dispute dispute = new Dispute();
        dispute.setBooking(booking);
        dispute.setReporter(reporter);
        dispute.setType(type);
        dispute.setDescription(description);
        dispute.setStatus(DisputeStatus.OPEN);
        return disputeRepository.save(dispute);
    }

    @Transactional
    public Dispute resolveDispute(Long disputeId, String resolution, DisputeStatus status) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException("Dispute not found"));
        dispute.setStatus(status);
        dispute.setResolution(resolution);
        return disputeRepository.save(dispute);
    }

    @Transactional(readOnly = true)
    public List<Dispute> getOpenDisputes() {
        return disputeRepository.findByStatus(DisputeStatus.OPEN);
    }
}
