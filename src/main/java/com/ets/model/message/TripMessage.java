package com.ets.model.message;

import lombok.Data;

@Data
public class TripMessage {
    private String slotId;
    private String userId;
    private String driverId;
    
    private double pickupLatitude;
    private double pickupLongitude;
    private String pickupAddress;
    
    private double dropLatitude;
    private double dropLongitude;
    private String dropAddress;
    
    private String status; // "REQUESTED", "ACCEPTED", "ARRIVED", "PICKEDUP", "COMPLETED", "CANCELLED", "OTP_REQUESTED"
    private String otp;
    
    private String rideType = "INSTANT"; // "INSTANT", "SCHEDULED"
    private String scheduledTime;
    
    private Long requestTimestamp;
    private Long pickupTimestamp;
    private Long dropTimestamp;
    
    private String messageType = "TRIP_UPDATE";
} 