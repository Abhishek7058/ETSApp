package com.ets.repository;

import com.ets.model.TripRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRequestRepository extends JpaRepository<TripRequest, Long> {
    List<TripRequest> findBySlotId(String slotId);
    List<TripRequest> findByUserIdAndStatus(String userId, String status);
    List<TripRequest> findByDriverIdAndStatus(String driverId, String status);
    List<TripRequest> findBySlotIdAndUserIdAndDriverId(String slotId, String userId, String driverId);
} 