package com.ets.controller;

import com.ets.model.TripRequest;
import com.ets.repository.TripRequestRepository;
import com.ets.service.LocationTrackingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripRequestRepository tripRequestRepository;
    private final LocationTrackingService locationTrackingService;
    
    @GetMapping("/slot/{slotId}")
    public ResponseEntity<List<TripRequest>> getTripsBySlot(@PathVariable String slotId) {
        return ResponseEntity.ok(tripRequestRepository.findBySlotId(slotId));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TripRequest>> getUserTrips(
            @PathVariable String userId,
            @RequestParam(required = false) String status) {
        if (status != null) {
            return ResponseEntity.ok(tripRequestRepository.findByUserIdAndStatus(userId, status));
        }
        return ResponseEntity.ok(tripRequestRepository.findAll());
    }
    
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<TripRequest>> getDriverTrips(
            @PathVariable String driverId,
            @RequestParam(required = false) String status) {
        if (status != null) {
            return ResponseEntity.ok(tripRequestRepository.findByDriverIdAndStatus(driverId, status));
        }
        return ResponseEntity.ok(tripRequestRepository.findAll());
    }
    
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody OtpVerifyRequest request) {
        boolean verified = locationTrackingService.verifyOTP(
                request.getSlotId(), request.getUserId(), request.getDriverId(), request.getOtp());
        
        if (verified) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "OTP verified successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Invalid OTP"));
        }
    }
    
    @Data
    private static class OtpVerifyRequest {
        private String slotId;
        private String userId;
        private String driverId;
        private String otp;
    }
} 