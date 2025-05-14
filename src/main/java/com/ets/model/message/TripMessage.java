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
    
    private String status; // "REQUESTED", "ACCEPTED", "ARRIVED", "COMPLETED", "CANCELLED"
    private String otp;
    
    private String messageType = "TRIP_UPDATE";
} 