package com.ets.service;

import com.ets.model.Driver;
import com.ets.model.TripRequest;
import com.ets.model.User;
import com.ets.model.message.ConnectionMessage;
import com.ets.model.message.LocationMessage;
import com.ets.model.message.TripMessage;
import com.ets.repository.DriverRepository;
import com.ets.repository.TripRequestRepository;
import com.ets.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class LocationTrackingService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final TripRequestRepository tripRequestRepository;
    
    // Maps to track connections by slotId
    private final Map<String, Map<String, Boolean>> slotUserMap = new HashMap<>();
    private final Map<String, String> slotDriverMap = new HashMap<>();
    
    public void handleUserConnection(ConnectionMessage connectionMessage) {
        String slotId = connectionMessage.getSlotId();
        String userId = connectionMessage.getUserId();
        
        if (connectionMessage.getStatus().equals("CONNECTED")) {
            // Update user status in DB
            User user = userRepository.findById(userId)
                    .orElse(new User());
            user.setUserId(userId);
            user.setIsOnline(true);
            userRepository.save(user);
            
            // Update in-memory tracking
            slotUserMap.computeIfAbsent(slotId, k -> new HashMap<>()).put(userId, true);
            
            // Check if driver is already online
            String driverId = slotDriverMap.get(slotId);
            if (driverId != null) {
                // Notify user that driver is online
                ConnectionMessage notifyMessage = new ConnectionMessage();
                notifyMessage.setSlotId(slotId);
                notifyMessage.setDriverId(driverId);
                notifyMessage.setStatus("CONNECTED");
                
                messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notifyMessage);
            }
            
            // Broadcast to slot that user is connected
            messagingTemplate.convertAndSend("/topic/slot/" + slotId, connectionMessage);
        } else {
            // Handle disconnect
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setIsOnline(false);
                userRepository.save(user);
            }
            
            // Update in-memory tracking
            if (slotUserMap.containsKey(slotId)) {
                slotUserMap.get(slotId).remove(userId);
                if (slotUserMap.get(slotId).isEmpty()) {
                    slotUserMap.remove(slotId);
                }
            }
            
            // Broadcast to slot that user is disconnected
            messagingTemplate.convertAndSend("/topic/slot/" + slotId, connectionMessage);
        }
    }
    
    public void handleDriverConnection(ConnectionMessage connectionMessage) {
        String slotId = connectionMessage.getSlotId();
        String driverId = connectionMessage.getDriverId();
        
        if (connectionMessage.getStatus().equals("CONNECTED")) {
            // Update driver status in DB
            Driver driver = driverRepository.findById(driverId)
                    .orElse(new Driver());
            driver.setDriverId(driverId);
            driver.setIsOnline(true);
            driverRepository.save(driver);
            
            // Update in-memory tracking
            slotDriverMap.put(slotId, driverId);
            
            // Notify all connected users in this slot that driver is online
            Map<String, Boolean> users = slotUserMap.getOrDefault(slotId, new HashMap<>());
            for (String userId : users.keySet()) {
                messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", connectionMessage);
            }
            
            // Broadcast to slot that driver is connected
            messagingTemplate.convertAndSend("/topic/slot/" + slotId, connectionMessage);
        } else {
            // Handle disconnect
            Driver driver = driverRepository.findById(driverId).orElse(null);
            if (driver != null) {
                driver.setIsOnline(false);
                driverRepository.save(driver);
            }
            
            // Update in-memory tracking
            slotDriverMap.remove(slotId);
            
            // Notify all users that driver is offline
            Map<String, Boolean> users = slotUserMap.getOrDefault(slotId, new HashMap<>());
            for (String userId : users.keySet()) {
                messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", connectionMessage);
            }
            
            // Broadcast to slot that driver is disconnected
            messagingTemplate.convertAndSend("/topic/slot/" + slotId, connectionMessage);
        }
    }
    
    public void handleLocationUpdate(LocationMessage locationMessage) {
        String slotId = locationMessage.getSlotId();
        
        // If it's a driver location update, send to all users in the slot
        if (locationMessage.getDriverId() != null) {
            Map<String, Boolean> users = slotUserMap.getOrDefault(slotId, new HashMap<>());
            for (String userId : users.keySet()) {
                messagingTemplate.convertAndSendToUser(userId, "/queue/location", locationMessage);
            }
        }
        
        // If it's a user location update, send to the driver of the slot
        if (locationMessage.getUserId() != null) {
            String driverId = slotDriverMap.get(slotId);
            if (driverId != null) {
                messagingTemplate.convertAndSendToUser(driverId, "/queue/location", locationMessage);
            }
        }
        
        // Also broadcast to the slot topic for all subscribers
        messagingTemplate.convertAndSend("/topic/slot/" + slotId + "/location", locationMessage);
    }
    
    public void createTripRequest(TripMessage tripMessage) {
        String slotId = tripMessage.getSlotId();
        String userId = tripMessage.getUserId();
        String driverId = slotDriverMap.get(slotId);
        
        if (driverId != null) {
            // Create trip request in DB
            TripRequest tripRequest = new TripRequest();
            tripRequest.setUserId(userId);
            tripRequest.setDriverId(driverId);
            tripRequest.setSlotId(slotId);
            tripRequest.setPickupLatitude(tripMessage.getPickupLatitude());
            tripRequest.setPickupLongitude(tripMessage.getPickupLongitude());
            tripRequest.setPickupAddress(tripMessage.getPickupAddress());
            tripRequest.setDropLatitude(tripMessage.getDropLatitude());
            tripRequest.setDropLongitude(tripMessage.getDropLongitude());
            tripRequest.setDropAddress(tripMessage.getDropAddress());
            tripRequest.setStatus("REQUESTED");
            tripRequest.setRequestTimestamp(System.currentTimeMillis());
            
            tripRequestRepository.save(tripRequest);
            
            tripMessage.setStatus("REQUESTED");
            
            // Send to driver
            messagingTemplate.convertAndSendToUser(driverId, "/queue/trips", tripMessage);
            
            // Confirm to user
            messagingTemplate.convertAndSendToUser(userId, "/queue/trips", tripMessage);
            
            // Broadcast to slot
            messagingTemplate.convertAndSend("/topic/slot/" + slotId + "/trips", tripMessage);
        }
    }
    
    public void updateTripStatus(TripMessage tripMessage) {
        String slotId = tripMessage.getSlotId();
        String userId = tripMessage.getUserId();
        String driverId = tripMessage.getDriverId();
        
        // Update trip in DB
        List<TripRequest> tripRequests = tripRequestRepository.findBySlotId(slotId);
        for (TripRequest trip : tripRequests) {
            if (trip.getUserId().equals(userId) && trip.getDriverId().equals(driverId)) {
                trip.setStatus(tripMessage.getStatus());
                
                // Generate OTP if driver has arrived
                if (tripMessage.getStatus().equals("ARRIVED")) {
                    String otp = generateOTP();
                    trip.setOtp(otp);
                    tripMessage.setOtp(otp);
                }
                
                tripRequestRepository.save(trip);
                break;
            }
        }
        
        // Notify user
        messagingTemplate.convertAndSendToUser(userId, "/queue/trips", tripMessage);
        
        // Notify driver
        messagingTemplate.convertAndSendToUser(driverId, "/queue/trips", tripMessage);
        
        // Broadcast to slot
        messagingTemplate.convertAndSend("/topic/slot/" + slotId + "/trips", tripMessage);
    }
    
    public boolean verifyOTP(String slotId, String userId, String driverId, String otp) {
        List<TripRequest> tripRequests = tripRequestRepository.findBySlotId(slotId);
        for (TripRequest trip : tripRequests) {
            if (trip.getUserId().equals(userId) && 
                trip.getDriverId().equals(driverId) && 
                trip.getOtp().equals(otp)) {
                
                trip.setOtpVerified(true);
                tripRequestRepository.save(trip);
                
                // Notify driver
                TripMessage tripMessage = new TripMessage();
                tripMessage.setSlotId(slotId);
                tripMessage.setUserId(userId);
                tripMessage.setDriverId(driverId);
                tripMessage.setStatus("OTP_VERIFIED");
                
                messagingTemplate.convertAndSendToUser(driverId, "/queue/trips", tripMessage);
                
                return true;
            }
        }
        return false;
    }
    
    private String generateOTP() {
        Random random = new Random();
        int number = random.nextInt(999999);
        return String.format("%06d", number);
    }
} 