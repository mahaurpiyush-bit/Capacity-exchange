package com.surplus.capacity.dto;

import com.surplus.capacity.entity.DisputeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DisputeRequest {

    @NotNull
    private Long bookingId;

    @NotNull
    private DisputeType type;

    @NotBlank
    private String description;

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public DisputeType getType() {
        return type;
    }

    public void setType(DisputeType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
