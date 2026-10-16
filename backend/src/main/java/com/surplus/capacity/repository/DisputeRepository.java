package com.surplus.capacity.repository;

import com.surplus.capacity.entity.Dispute;
import com.surplus.capacity.entity.DisputeStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    List<Dispute> findByStatus(DisputeStatus status);
}
