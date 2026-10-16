package com.surplus.capacity.repository;

import com.surplus.capacity.entity.OperatingSchedule;
import com.surplus.capacity.entity.Resource;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperatingScheduleRepository extends JpaRepository<OperatingSchedule, Long> {
    List<OperatingSchedule> findByResource(Resource resource);
}
