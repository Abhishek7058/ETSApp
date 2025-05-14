package com.ets.model.message;

import lombok.Data;

@Data
public class LocationMessage {
    private String slotId;
    private String userId;
    private String driverId;
    private double latitude;
    private double longitude;
    private Long timestamp;
    private String messageType = "LOCATION_UPDATE";
} 