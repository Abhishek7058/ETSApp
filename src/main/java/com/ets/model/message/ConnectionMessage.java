package com.ets.model.message;

import lombok.Data;

@Data
public class ConnectionMessage {
    private String slotId;
    private String userId;
    private String driverId;
    private String status; // "CONNECTED", "DISCONNECTED"
    private String messageType = "CONNECTION_UPDATE";
} 