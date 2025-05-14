package com.ets.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "trip_requests")
public class TripRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String userId;
    private String driverId;
    private String slotId;
    
    private double pickupLatitude;
    private double pickupLongitude;
    private String pickupAddress;
    
    private double dropLatitude;
    private double dropLongitude;
    private String dropAddress;
    
    private String status; // REQUESTED, ACCEPTED, ARRIVED, COMPLETED, CANCELLED
    
    private String otp;
    private boolean otpVerified = false;
    
    private Long requestTimestamp;
} 