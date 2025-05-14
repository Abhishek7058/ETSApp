package com.ets.model;

import lombok.Data;

@Data
public class Location {
    private double latitude;
    private double longitude;
    private String userId;
    private String driverId;
    private String slotId;
    private Long timestamp;
} 