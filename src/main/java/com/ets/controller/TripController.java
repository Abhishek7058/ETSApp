package com.ets.controller;

import com.ets.model.TripRequest;
import com.ets.model.message.TripMessage;
import com.ets.repository.TripRequestRepository;
import com.ets.service.LocationTrackingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripRequestRepository tripRequestRepository;
    private final LocationTrackingService locationTrackingService;
    private final SimpMessagingTemplate messagingTemplate;
    
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
        System.out.println("Verifying OTP: " + request.getOtp() + " for user: " + request.getUserId());
        
        // First, check if we have a trip for this user and driver
        List<TripRequest> trips = tripRequestRepository.findBySlotId(request.getSlotId());
        TripRequest matchingTrip = null;
        
        for (TripRequest trip : trips) {
            if (trip.getUserId().equals(request.getUserId()) && 
                trip.getDriverId().equals(request.getDriverId())) {
                matchingTrip = trip;
                break;
            }
        }
        
        if (matchingTrip == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error", 
                "message", "No active trip found"
            ));
        }
        
        // Verify the OTP
        if (matchingTrip.getOtp() != null && matchingTrip.getOtp().equals(request.getOtp())) {
            // Update trip status
            matchingTrip.setOtpVerified(true);
            tripRequestRepository.save(matchingTrip);
            
            // Send verification message
            TripMessage tripMessage = new TripMessage();
            tripMessage.setSlotId(request.getSlotId());
            tripMessage.setUserId(request.getUserId());
            tripMessage.setDriverId(request.getDriverId());
            tripMessage.setStatus("OTP_VERIFIED");
            
            // Send to specific user
            messagingTemplate.convertAndSendToUser(request.getUserId(), "/queue/trips", tripMessage);
            
            // Send to specific driver
            messagingTemplate.convertAndSendToUser(request.getDriverId(), "/queue/trips", tripMessage);
            
            // Broadcast to slot
            messagingTemplate.convertAndSend("/topic/slot/" + request.getSlotId() + "/trips", tripMessage);
            
            return ResponseEntity.ok(Map.of("status", "success", "message", "OTP verified successfully"));
        } else {
            System.out.println("OTP mismatch. Expected: " + matchingTrip.getOtp() + ", Received: " + request.getOtp());
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Invalid OTP"));
        }
    }
    
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtpToUser(@RequestBody OtpVerifyRequest request) {
        String otp = request.getOtp();
        
        // If OTP is provided in the request, use it
        if (otp != null && !otp.isEmpty()) {
            // Find the trip and set the OTP
            List<TripRequest> trips = tripRequestRepository.findBySlotId(request.getSlotId());
            TripRequest matchingTrip = null;
            
            // Find the trip for this specific user and driver
            for (TripRequest trip : trips) {
                if (trip.getUserId().equals(request.getUserId()) && 
                    trip.getDriverId().equals(request.getDriverId())) {
                    matchingTrip = trip;
                    break;
                }
            }
            
            if (matchingTrip != null) {
                // Update the trip with the OTP
                matchingTrip.setOtp(otp);
                tripRequestRepository.save(matchingTrip);
                
                // Send the OTP to the user via the location tracking service
                locationTrackingService.sendOtpToUser(
                    request.getSlotId(), request.getUserId(), request.getDriverId());
                
                return ResponseEntity.ok(Map.of(
                    "status", "success", 
                    "message", "OTP sent to user",
                    "otp", otp
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error", 
                    "message", "No active trip found"
                ));
            }
        } else {
            // Generate an OTP and send it
            String generatedOtp = null;
            
            // Find the trip and generate an OTP
            List<TripRequest> trips = tripRequestRepository.findBySlotId(request.getSlotId());
            for (TripRequest trip : trips) {
                if (trip.getUserId().equals(request.getUserId()) && 
                    trip.getDriverId().equals(request.getDriverId())) {
                    // Generate a 6-digit OTP
                    generatedOtp = String.format("%06d", (int)(Math.random() * 1000000));
                    trip.setOtp(generatedOtp);
                    tripRequestRepository.save(trip);
                    break;
                }
            }
            
            if (generatedOtp != null) {
                // Send the OTP to the user
                locationTrackingService.sendOtpToUser(
                    request.getSlotId(), request.getUserId(), request.getDriverId());
                
                return ResponseEntity.ok(Map.of(
                    "status", "success", 
                    "message", "OTP sent to user",
                    "otp", generatedOtp
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error", 
                    "message", "No active trip found"
                ));
            }
        }
    }
    
    @Data
    private static class OtpVerifyRequest {
        private String slotId;
        private String userId;
        private String driverId;
        private String otp;
    }

    @GetMapping("/test-websocket/{slotId}/{userId}")
    public ResponseEntity<?> testWebSocket(
            @PathVariable String slotId,
            @PathVariable String userId,
            @RequestParam(required = false) String message) {
        
        if (message == null) {
            message = "Test message from server at " + new java.util.Date();
        }
        
        System.out.println("Sending test message to user: " + userId);
        
        // Send message via user's queue
        messagingTemplate.convertAndSendToUser(
            userId, 
            "/queue/trips", 
            Map.of(
                "slotId", slotId,
                "userId", userId,
                "status", "TEST_MESSAGE",
                "message", message,
                "timestamp", System.currentTimeMillis()
            )
        );
        
        // Also send via topic
        messagingTemplate.convertAndSend(
            "/topic/slot/" + slotId + "/trips",
            Map.of(
                "slotId", slotId,
                "userId", userId,
                "status", "TEST_MESSAGE",
                "message", message + " (via topic)",
                "timestamp", System.currentTimeMillis()
            )
        );
        
        return ResponseEntity.ok(Map.of(
            "status", "success", 
            "message", "Test messages sent to user: " + userId
        ));
    }
} 